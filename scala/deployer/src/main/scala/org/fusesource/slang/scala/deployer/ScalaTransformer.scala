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

import archiver.ScalaArchiver
import compiler.{Bundles, ScalaCompiler}
import org.apache.commons.logging.LogFactory
import tools.nsc.io.AbstractFile
import java.net.URL
import org.osgi.framework.BundleContext
import java.io.{File, InputStream, OutputStream}

class ScalaTransformer(val bundles: List[AbstractFile]) {

  final val LOG = LogFactory.getLog(classOf[ScalaTransformer])

  val compiler = new ScalaCompiler (bundles)

  val archiver = new ScalaArchiver (bundles) 

//  def transform(source: ScalaSource, stream: OutputStream) {
//    val result = transform(source)
//    val bytes = new Array[Byte](1024)
//    var read = result.read(bytes)
//    while (read > 0) {
//      stream.write(bytes, 0, read)
//      read = result.read(bytes)
//    }
//    result.close
//  }

  def transform (source: ScalaSource) : InputStream = {
    LOG.info("Transforming " + source + " into an OSGi bundle")
    archiver.archive(compile(source), source)
  }

  def compile (source: AbstractFile) = compiler.compile (source)
}

object ScalaTransformer {

//  def create (context: BundleContext) : ScalaTransformer = {
//    val bundles : List[AbstractFile] = if (context == null) {
//      List()
//    } else {
//      val framework = context.getProperty("karaf.framework")
//      val jar = new File(context.getProperty("karaf.base"), context.getProperty("karaf.framework." + framework))
//      AbstractFile.getDirectory(jar) :: Bundles.create(context.getBundles)
//    }
//    create (bundles)
//  }

  def create (libraries: List[AbstractFile]) : ScalaTransformer =
    new ScalaTransformer(libraries)

  def transform (context: BundleContext, url: URL) : InputStream = {
	val bundles = Option(context) match {
	case None =>
		throw new Exception ("No BundleContext available to search for OSGi bundles.")
		// TODO: Why not List()?
	case Some (ctxt) =>
		val framework = ctxt.getProperty ("karaf.framework")
		val jar = new File (
			ctxt.getProperty ("karaf.base"),
			ctxt.getProperty ("karaf.framework." + framework))
		AbstractFile.getDirectory(jar) :: Bundles.create (ctxt.getBundles)
	}
	transform (bundles, url)
  }

  def transform (libraries: List[AbstractFile], url: URL) = {
	val source = ScalaSource (url, libraries)
	// val manifest = manifest (source)
	create(source.libs).transform(source)
  }

  def transform (libraries: List[AbstractFile], url: URL, stream: OutputStream) {
	val result = transform (libraries, url)
	val bytes = new Array[Byte] (1024)
	var read = result.read(bytes)
	while (read > 0) {
		stream.write(bytes, 0, read)
		read = result.read (bytes)
	}
	result.close
  }
}
