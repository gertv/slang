/**
 * Copyright (C) FuseSource, Inc.
 * http://fusesource.com
 *
 * Copyright (C) Crossing-Tech SA, 2012.
 * Contact: <guillaume.yziquel@crossing-tech.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.slang.scala.deployer

import org.junit.{Before, Test}
import org.junit.Assert.{assertNotNull,assertTrue,assertEquals}
import java.util.jar.JarInputStream
import java.io.File
import tools.nsc.io.AbstractFile

/**
 * Test cases for {@link ScalaTransformer}
 */
class ScalaTransformerTest {

  //var transformer : ScalaTransformer = null
  var libraries : List[AbstractFile] = Nil
  lazy val repository =
    if (System.getProperty ("maven.local.repo") != null) {
      new File (System.getProperty ("maven.local.repo"))
    } else {
      new File (new File (System.getProperty ("user.home"), ".m2"), "repository") 
    }

  @Before
  def createTransformer() {
    val scalaLib = new File (repository, "org/scala-lang/scala-library/2.9.1/scala-library-2.9.1.jar")
    libraries = List (AbstractFile.getFile(scalaLib))
  }

  @Test
  def testCompile() {
    val source = new ScalaSource (this.getClass.getClassLoader.getResource ("SimpleTest.scala"), libraries)
    val result = source.compile ()

    assertNotNull (result.lookupName ("SimpleTest.class", false))
    assertNotNull (result.lookupPath ("org/test/AnotherSimpleTest.class", false))
  }

  @Test
  def testTransform() {
    val source = new ScalaSource (this.getClass.getClassLoader.getResource("SimpleTest.scala"), libraries)
    val result = source.transform ()
    var jar = new JarInputStream (result)
    var entries = getEntries (jar)
    assertTrue (entries.contains ("SimpleTest.class"))
    assertTrue (entries.contains ("org/test/AnotherSimpleTest.class"))
  }

  @Test
  def testTransformWithActivator() {
    val source = new ScalaSource (this.getClass.getClassLoader.getResource ("TestWithActivator.scala"), libraries)
    val result = source.transform ()
    var jar = new JarInputStream (result)
    var entries = getEntries (jar)
    assertTrue (entries.contains ("org/test/TestWithActivator.class"))

    val manifest = jar.getManifest;
    assertEquals ("BundleActivator class should have been automatically detected",
                  "org.test.TestWithActivator", manifest.getMainAttributes.getValue ("Bundle-Activator"))
    assertTrue ("Bundle-SymbolicName should have a decent value",
                manifest.getMainAttributes.getValue("Bundle-SymbolicName").endsWith("TestWithActivator"));
  }


  def getEntries(jar: JarInputStream) = {
    var result = List[String]()
    var entry = jar.getNextEntry
    while (entry != null) {
      result = entry.getName :: result
      entry = jar.getNextEntry
    }
    result
  }
}
