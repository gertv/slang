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
package org.fusesource.slang.scala.deployer.compiler

import tools.nsc.reporters.{AbstractReporter, Reporter}
import org.apache.commons.logging.LogFactory
import java.net.URL
import java.io.File
import tools.nsc.util.ClassPath.ClassPathContext
import tools.nsc.io.{VirtualDirectory, PlainFile, AbstractFile}
import tools.nsc.backend.JavaPlatform
import tools.util.PathResolver
import tools.nsc.{Interpreter, Global, Settings}
import tools.nsc.interpreter.AbstractFileClassLoader
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
    settings.parseParams(settings.splitParams("-verbose"))
    settings.usejavacp.value = true

    val run = new compiler.Run
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


//    override def rootLoader = new loaders.JavaPackageLoader(classPath.asInstanceOf[ClassPath[AbstractFile]])

    def createClassPath[T](original: ClassPath[T]) = {
      var result =  List(original)
      bundles.foreach(bundle => {
        result = original.context.newClassPath(bundle) :: result
      })
      new MergedClassPath(result, original.context)
    }
  }
}