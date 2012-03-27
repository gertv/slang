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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.slang.scala.deployer.archiver

import java.io._
import java.net.URL
import java.util.Properties
import java.util.jar._
import org.apache.commons.logging.LogFactory
import tools.nsc.io.AbstractFile
import tools.nsc.interpreter.AbstractFileClassLoader
import org.osgi.framework.BundleActivator
import org.ops4j.pax.swissbox.bnd.BndUtils.createBundle
import org.fusesource.slang.scala.deployer.ScalaSource

/**
 * Helper class that stores the contents of a Scala compile {@link AbstractFile}
 * to an output (packed as a JAR file)
 */
class ScalaArchiver(bundles: List[AbstractFile]) {

  val LOG = LogFactory.getLog(classOf[ScalaArchiver])

  // classloaders contains a classloader supposed to be able to load classes
  // from a given bundles. In fact, one classloader for each bundle.
  val classloaders = bundles.map(new AbstractFileClassLoader(_, getClass.getClassLoader))

  def archive(dir: AbstractFile, source: ScalaSource): InputStream = {

    val classloader: AbstractFileClassLoader = createClassLoader(dir)

    val props = new Properties

    val bytes = new ByteArrayOutputStream
    val jar = new JarOutputStream(bytes)
    entries(dir) {
      (name: String, file: AbstractFile) =>
        archiveFile(file, jar, name)
        try {
          val renaming = name.replaceAll(".class", "").replaceAll("/", ".")
          // TODO: Name processing is a bit crude... should be cleaned up.
          val theType = classloader.loadClass(renaming)
          if (classOf[BundleActivator].isAssignableFrom(theType)) {
            LOG.debug("Discovered bundle activator " + theType.getName)
            props.put("Bundle-Activator", theType.getName)
          }
        } catch {
          case e: Exception => e.printStackTrace()
        }
    }

    jar.close()
    bytes.close()

    createBundle(new ByteArrayInputStream(bytes.toByteArray), props, bsn(source.url))
  }

  def entries(dir: AbstractFile)(action: (String, AbstractFile) => Unit): Unit =
    entries(dir, "")(action)

  def entries(dir: AbstractFile, path: String)(action: (String, AbstractFile) => Unit): Unit = {
    dir.foreach {
      (file: AbstractFile) =>
        val name = if (path.length == 0) file.name else path + "/" + file.name
        if (file.isDirectory) entries(file, name)(action) else action(name, file)
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
    jar.closeEntry()
  }

  def bsn(url: URL) = {
    var result = url.getPath
    if (result.endsWith(".scala")) {
      result = result.substring(0, result.length - 6)
    }
    if (result.startsWith("/")) {
      result = result.substring(1)
    }
    result.replaceAll("/", ".")
  }

  def createClassLoader(dir: AbstractFile) = new AbstractFileClassLoader(dir, getClass.getClassLoader) {

    // Set to true to trace classloader activity.
    //override protected def trace = true

    override def findClass(name: String): Class[_] = try {
      // let's try the bundle we're generating first
      super.findClass(name)
    } catch {
      case e: ClassNotFoundException =>
        // and then fall back to the rest of the bundles
        findClassInBundles(name)
    }

    def findClassInBundles(name: String): Class[_] =
      classloaders.map(cl => findClass(name, cl)).find(cls => cls.isDefined) match {
        case Some(cls) => cls.get
        case None => throw new ClassNotFoundException(name)
      }

    def findClass(name: String, loader: AbstractFileClassLoader) =
      try Some(loader.findClass(name)) catch {
        case e: /* ClassNotFoundException */ Exception =>
          /* TODO: Original code was catching the ClassNotFoundException,
          and an inadequate exception was falling through. So I widened
          the range of caught exceptions, but this is only a workaround. */
          None
      }
  }
}
