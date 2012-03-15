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

trait ScalaSource extends AbstractFile {

	val url : URL

	override def toString = url.toString

}

object ScalaSource {

	def apply (u : URL) = u.getProtocol match {
	case "file" =>
		new PlainFile (new File (u.toURI)) with ScalaSource { val url = u }
	case _ =>
		throw new Exception ("Protocol of a ScalaSource must be a file.")
		//AbstractFile.getURL (url) with ScalaSource
	}

}
