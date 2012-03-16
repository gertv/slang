/**
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

import java.io.File
import java.net.URL
import org.osgi.framework.BundleContext
import tools.nsc.io.AbstractFile
import tools.nsc.io.PlainFile
import compiler.Bundles

trait ScalaSource extends AbstractFile {

	val url : URL

	val context : BundleContext

	override def toString = url.toString

	val bundles = Option (context) match {
	case None =>
		throw new Exception ("No BundleContext available to search for OSGi bundles.")
		//List()
	case Some (ctxt) =>
		val framework = ctxt.getProperty("karaf.framework")
		val jar = new File(
			ctxt.getProperty("karaf.base"),
			ctxt.getProperty("karaf.framework." + framework))
		AbstractFile.getDirectory(jar) :: Bundles.create(ctxt.getBundles)
	}

}

object ScalaSource {

	def apply (url : URL, ctxt : BundleContext) = (Option(url), Option(ctxt)) match {
	case (Some(u), Some(c)) if u.getProtocol == "file" =>
		new PlainFile (new File (url.toURI)) with ScalaSource {
			val url = u
			val context = c
		}
	case _ =>
		throw new Exception ("Invalid URL or BundleContext for ScalaSource construction.")
		// TODO: We should perhaps use AbstractFile.getURL(u) here.
	}

}
