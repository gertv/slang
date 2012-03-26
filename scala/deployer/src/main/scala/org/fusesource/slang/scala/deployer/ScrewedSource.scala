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

import java.io._
import org.apache.commons.logging.LogFactory
import tools.nsc.io.AbstractFile

class ScrewedSource (var f : AbstractFile) extends AbstractFile {

	final val LOG = LogFactory.getLog(classOf[ScrewedSource])

	LOG.debug ("ScrewedSource for " + f.path)

	def fail(msg : String) : Nothing = {
		val s = "ScrewedSource: " + msg + " unimplemented."
		println(s); LOG.debug(s); throw new Exception (s)
	}

	override def lookupNameUnchecked (name : String, directory : Boolean) = fail("lookupNameUnchecked")

	override def lookupName (name : String, directory : Boolean) = fail("lookupName")

	override def iterator = fail("iterator")

	override def output = fail("output")

	override def sizeOption = Some(code.length)

	def code = """

                import org.osgi.framework.{BundleContext, BundleActivator}

                package org.fusesource.slang.scala.deployer.failure {

                        class MyActivator extends BundleActivator {

                                def start (context : BundleContext) { throw new Exception ("Failed to compile Scala code") }

                                def stop (context : BundleContext) { }

                        }

                }

        """.getBytes("UTF-8")	// TODO: Check if global.settings.encoding.value is not better.
				// see line 77 of AbstractFile.scala in the compiler's source code.

	override def input = new ByteArrayInputStream(code)

	override def lastModified = fail("lastModified")

	override def isDirectory = fail("isDirectory")

	override def delete = fail("delete")

	override def create = fail("create")

	override def file = fail("file")

	override def container = fail("container")

	override def absolute = fail("absolute")

	override def path = f.path

	override def name = f.name

}
