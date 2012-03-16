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
import tools.nsc.io.AbstractFile
import tools.nsc.io.PlainFile
import compiler.ScalaCompiler
import archiver.ScalaArchiver

trait ScalaSource extends AbstractFile {

	val url : URL

	val libs : List[AbstractFile]

	override def toString = url.toString

	def compile () : AbstractFile =
		(new ScalaCompiler (libs)).compile (this)

	def archive (dir : AbstractFile) =
		(new ScalaArchiver (libs)).archive (dir, this)

	def transform () = archive(compile())

}

object ScalaSource {

	def apply (url : URL, libs : List[AbstractFile]) = (Option(url), Option(libs)) match {
	case (Some(u), Some(l)) if u.getProtocol == "file" =>
		new PlainFile (new File (url.toURI)) with ScalaSource {
			val url = u
			val libs = l
		}
	case _ =>
		throw new Exception ("Invalid URL or BundleContext for ScalaSource construction.")
		// TODO: We should perhaps use AbstractFile.getURL(u) here.
	}

}
