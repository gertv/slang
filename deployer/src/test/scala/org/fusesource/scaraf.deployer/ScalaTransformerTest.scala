/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.scaraf.deployer

import org.junit.{Before, Test}
import org.junit.Assert.{assertNotNull,assertTrue,assertEquals}
import java.util.jar.JarInputStream
import java.io.{ByteArrayInputStream, FileOutputStream, ByteArrayOutputStream, File}

/**
 * Test cases for {@link ScalaTransformer}
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

  @Test
  def testTransformWithActivator = {
    val source = this.getClass.getClassLoader.getResource("TestWithActivator.scala")
    val result = new ByteArrayOutputStream()

    transformer.transform(source, result)

    var jar = new JarInputStream(new ByteArrayInputStream(result.toByteArray))
    var entries = getEntries(jar)
    assertTrue(entries.contains("org/test/TestWithActivator.class"))

    val manifest = jar.getManifest;
    assertEquals("BundleActivator class should have been automatically detected",
                 "org.test.TestWithActivator", manifest.getMainAttributes.getValue("Bundle-Activator"))
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
