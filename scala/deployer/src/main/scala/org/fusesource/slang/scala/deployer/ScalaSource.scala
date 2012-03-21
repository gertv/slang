/**
 * Copyright (C) Crossing-Tech SA, 2012.
 * Contact: <guillaume.yziquel@crossing-tech.com>
 *
 * Copyright (C) FuseSource, Inc.
 * http://fusesource.com
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
import java.net.URL
import org.apache.commons.logging.LogFactory
import org.osgi.framework.BundleContext
import tools.nsc.io.AbstractFile
import tools.nsc.io.PlainFile
import compiler.Bundles
import compiler.ScalaCompiler
import archiver.ScalaArchiver

class ScalaSource (val url : URL, val libraries : List[AbstractFile]) extends AbstractFile {

	final val LOG = LogFactory.getLog(classOf[ScalaSource])

	if ((libraries == null) || libraries.exists(_ == null))
		throw new Exception ("Invalid libraries for ScalaSource constructor.")

	if (url == null)
		throw new Exception ("Invalid URL for ScalaSource constructor.")

	def this (url : URL, context : BundleContext) = this (url,
		Option(context) match {
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
	)

	/**********************************************************************/

	/* It should be noted that the Scala compiler has the following piece of
	   code to read so-called AbstractFiles. See SourceReader.scala, lines
	   48 to 59 in the compiler source code.

	   def read(file: AbstractFile): Array[Char] = {
	     try file match {
	       case p: PlainFile        => read(p.file)
	       case z: ZipArchive#Entry => read(Channels.newChannel(z.input))
	       case _                   => read(ByteBuffer.wrap(file.toByteArray))
	     }
	     catch {
	       case e: Exception => reportEncodingError("" + file) ; Array()
	     }
	   }

	   The big problem about this pattern-matching is that the principle of
	   a common interface using the "toByteArray" method is invalidated. In
	   our specific case, this means that it useless to override the input(),
	   toCharArray() or toByteArray() methods to intercept and rewrite on the
	   fly what is read from the file.

	   We therefore chose not to inherit from the PlainFile, but to prefer
	   composition over inheritance. Which the reason why we have a plainFile
	   field: fooling this pattern-matching. */

	val plainFile : PlainFile = new PlainFile (new File (url.toURI))

	override def name : String = plainFile.name

	override def path : String = plainFile.path

	override def absolute : AbstractFile = plainFile.absolute

	override def container : AbstractFile = plainFile.container

	override def file : File = plainFile.file

	override def create () : Unit = plainFile.create ()

	override def delete () : Unit = plainFile.delete ()

	override def isDirectory : Boolean = plainFile.isDirectory

	override def lastModified : Long = plainFile.lastModified

	override def input : InputStream = plainFile.input

	override def toByteArray : Array[Byte] =

		/* NOTE: If the sizeOption is not properly overriden, the AbstractFile
		   default implementation of sizeOption will blow up. Hence the 'catch'.

		   These composition vs. inheritance bugs are hard to find. In case this
		   happens, continue delegating calls to plainFile. */

		try {super.toByteArray} catch {case e => e.printStackTrace(); throw e}
	
	override def output : OutputStream = plainFile.output

	override def sizeOption = plainFile.sizeOption

	override def iterator : Iterator[AbstractFile] = plainFile.iterator

	override def lookupName (name : String, directory : Boolean) : AbstractFile =
		plainFile.lookupName (name, directory)

	override def lookupNameUnchecked (name : String, directory : Boolean) : AbstractFile =
		plainFile.lookupNameUnchecked (name, directory)

	override def toString () =
		/* This call needs to be delegated to ensure that the referenced file is
		   named properly. The implementation in PlainFile removes the file: URI
		   prefix that is fed to a ScalaSource instance at construct-time. */
		plainFile.toString

	/**********************************************************************/

	def compile () : AbstractFile = {
		LOG.debug ("Compiling " + this + " using embedded Scala compiler.")
		(new ScalaCompiler (libraries)).compile (this)
	}

	def archive (dir : AbstractFile) = {
		LOG.debug ("Archiving compiled " + this + " into an OSGi bundle.")
		(new ScalaArchiver (libraries)).archive (dir, this)
	}

	def transform () = {
		LOG.info ("Transforming " + this + " into an OSGi bundle.")
		manifest ()
		archive (compile ())
	}

	def manifest () {

		val source = io.Source.fromInputStream(input).getLines().mkString("\n")
		//LOG.info ("Manifest: \n" + source)

		import parser.ScriptParser._
		import scala.util.parsing.combinator._

		val c : ParseResult[List[parser.Item]] = parse(source)
		LOG.info("Comment:\n" + c)
		/* TODO: Extract manifest from this abstract file. */
	}
}
