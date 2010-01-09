package org.fusesource.scaraf.deployer

import org.junit.{Before, Test}
import org.junit.Assert.{assertNotNull,assertTrue}
import java.util.jar.JarInputStream
import java.io.{ByteArrayInputStream, FileOutputStream, ByteArrayOutputStream, File}

/**
 * Created by IntelliJ IDEA.
 * User: gert
 * Date: Jan 5, 2010
 * Time: 6:04:22 AM
 * To change this template use File | Settings | File Templates.
 */

class ScalaTransformerTest {

  var transformer : ScalaTransformer = null

  @Before
  def createTransformer = {
    transformer = ScalaTransformer.create(null)
  }

  @Test
  def testCompile = {
    val source = this.getClass.getClassLoader.getResource("SimpleTest.scala")
    val result = transformer.compile(source)

    assertNotNull(result.lookupName("SimpleTest.class", false))
    assertNotNull(result.lookupPath("org/test/AnotherSimpleTest.class", false))
  }

  @Test
  def testTransform = {
    val source = this.getClass.getClassLoader.getResource("SimpleTest.scala")
    val result = new ByteArrayOutputStream

    transformer.transform(source, result)

    var jar = new JarInputStream(new ByteArrayInputStream(result.toByteArray))
    var entries = getEntries(jar)
    assertTrue(entries.contains("SimpleTest.class"))
    assertTrue(entries.contains("org/test/AnotherSimpleTest.class"))
  }

  def getEntries(jar: JarInputStream) = {
    var result = List[String]()
    var entry = jar.getNextEntry
    while (entry != null) {
      result += entry.getName
      entry = jar.getNextEntry
    }
    result
  }

}
