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
package org.fusesource.slang.scala.deployer.compiler

import tools.nsc.reporters.{AbstractReporter, Reporter}
import org.apache.commons.logging.LogFactory
import tools.nsc.io.{VirtualDirectory, PlainFile, AbstractFile}
import tools.nsc.{Interpreter, Global, Settings}
import tools.nsc.util._

/**
 * Scala compiler that uses a provided list of bundles as the compiler
 * classpath
 */
class ScalaCompiler(bundles: List[AbstractFile]) {

  final val LOG = LogFactory.getLog(classOf[ScalaCompiler])

  def compile(sources: List[AbstractFile]) : AbstractFile = {
    LOG.info("Compiling " + sources)
    val dir = new VirtualDirectory("memory", None)

    settings.outputDirs.setSingleOutput(dir)
    settings.verbose.value = true
    settings.debug.value = false        /* Set to true for logging debugging info. */
    settings.Ylogcp.value = false       /* Set to true for classpath informations. */
    /* Yno-predefs and Yno-imports may be useful */
    settings.usejavacp.value = true

    /* Other values related to classpath handling:
                settings.bootclaspath.value
                settings.classpath.value
                settings.extdirs.value
                settings.javabootclasspath.value
                settings.javaextdirs.value
                settings.sourcepath.value
    */

    /* The instantiation of compiler.Run is wrapped between a try/catch
       construct in order to avoid the exception to be swallowed up by the
       rest of code: we display the exception message in the logs and the
       stack trace to stdout. */
    val run = try { new compiler.Run } catch {case e : Throwable =>
      LOG.debug ("Failed to instantiate internal compiler.Run framework: " + e.getMessage)
      e.printStackTrace()
      throw e
    }
    run.compileFiles(sources)

    dir

  }

  lazy val settings = new Settings

  lazy val reporter = new AbstractReporter {
    def displayPrompt = println("compiler:")

    def display(position: Position, msg: String, severity: Severity): Unit = {
      LOG.warn(position + ":" + msg)
    }

    val settings = ScalaCompiler.this.settings
  }

    lazy val compiler = new Global(settings, reporter) {

    override def classPath = {
      require(!forMSIL, "MSIL not supported")
      createClassPath(super.classPath)
    }

    /* The crucial thing to do: We need to override rootLoader, because that's
       what is responsible for the proper loading of the classpath and the bundles.
       This method in turn calls the overriden classpath method which is responsible
       for creating a representation of the classpath internal to the Scala compiler
       and injecting our OSGi bundles into this internal classpath. */
    override def rootLoader = {
      val cp = classpath.asInstanceOf[ClassPath[AbstractFile]]
      (new loaders.JavaPacakageLoader (cp)).asInstanceOf[LazyType]
    }

    def createClassPath [T] (original: ClassPath[T]) = {
      var result =  List(original)
      bundles.foreach(bundle => {
        result = original.context.newClassPath(bundle) :: result
      })
      new MergedClassPath(result, original.context)
    }
  }
}
