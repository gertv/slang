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
package org.fusesource.slang.scala.deployer.archiver

import tools.nsc.io.AbstractFile
import tools.nsc.interpreter.AbstractFileClassLoader
import org.osgi.framework.BundleActivator
import java.util.jar.JarFile.MANIFEST_NAME
import java.util.jar.{Attributes, Manifest, JarEntry, JarOutputStream}
import java.io.{ByteArrayOutputStream, ByteArrayInputStream, InputStream, OutputStream}
import org.ops4j.pax.swissbox.bnd.BndUtils.createBundle

import java.util.Properties
import org.apache.commons.logging.LogFactory

/**
 * Helper class that stores the contents of a Scala compile {@link AbstractFile}
 * to an output (packed as a JAR file)
 */
class ScalaArchiver {

  val LOG = LogFactory.getLog(classOf[ScalaArchiver])

  def archive(dir: AbstractFile) : InputStream = {
    val classloader = new AbstractFileClassLoader(dir, getClass().getClassLoader)

    val props = new Properties

    val bytes = new ByteArrayOutputStream
    val jar = new JarOutputStream(bytes)
    entries(dir) { (name: String, file: AbstractFile) =>
      archiveFile(file, jar, name)
      try {
        val theType = classloader.loadClass(name.replaceAll(".class", "").replaceAll("/", "."))
        if (classOf[BundleActivator].isAssignableFrom(theType)) {
          LOG.debug("Discovered bundle activator " + theType.getName)
          props.put("Bundle-Activator", theType.getName)
        }
      } catch {
        case e: Exception => e.printStackTrace
      }

    }

    jar.close
    bytes.close

    createBundle(new ByteArrayInputStream(bytes.toByteArray),
                 props,
                 "some name")
  }
    
  def entries(dir: AbstractFile)(action: (String, AbstractFile) => Unit) : Unit = 
    entries(dir, "")(action)

  def entries(dir: AbstractFile, path:String)(action: (String, AbstractFile) => Unit) : Unit = {
    dir.foreach { (file: AbstractFile) =>
      val name = if (path.length == 0) { file.name } else { path + "/" + file.name }
      if (file.isDirectory) {
        entries(file, name)(action)
      } else {
        action(name, file)
      }
    }    
  }

  def archiveFile(file: AbstractFile, jar: JarOutputStream, name: String) = {
    val entry = new JarEntry(name)
    jar.putNextEntry(entry)
    val bytes = new Array[Byte](1024)
    val is = file.input
    var read = is.read(bytes)
    while (read > 0) {
      jar.write(bytes, 0, read)
      read = is.read(bytes)
    }
    jar.closeEntry
  }
}
