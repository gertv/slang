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
package org.fusesource.scaraf.deployer.archiver

import tools.nsc.io.AbstractFile
import java.io.OutputStream
import java.util.jar.{JarEntry, JarOutputStream}

/**
 * Helper class that stores the contents of a Scala compile {@link AbstractFile}
 * to an output (packed as a JAR file)
 */
class ScalaArchiver {

  def archive(dir: AbstractFile, stream: OutputStream) {
    val jar = new JarOutputStream(stream)
    archiveDir(dir, jar)
    jar.close
  }
    
  def archiveDir(dir: AbstractFile, jar: JarOutputStream) : Unit = archiveDir(dir, jar, "")

  def archiveDir(dir: AbstractFile, jar: JarOutputStream, path:String) : Unit = {
    dir.foreach { (file: AbstractFile) =>
      val name = if (path.length == 0) { file.name } else { path + "/" + file.name }
      if (file.isDirectory) {
        archiveDir(file, jar, name)
      } else {
        archiveFile(file, jar, name)
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
      jar.write(bytes)
      read = is.read(bytes)
    }
    jar.closeEntry
  }
}
