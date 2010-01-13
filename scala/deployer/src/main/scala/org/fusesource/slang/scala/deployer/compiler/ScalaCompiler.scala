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

import tools.nsc.{Global, Settings}
import tools.nsc.reporters.{AbstractReporter, Reporter}
import tools.nsc.util.{Position, ClassPath}
import org.apache.commons.logging.LogFactory
import java.net.URL
import java.io.File
import tools.nsc.io.{PlainFile, AbstractFile}

/**
 * Scala compiler that uses a provided list of bundles as the compiler
 * classpath
 */
class ScalaCompiler(bundles: List[AbstractFile]) {

  final val LOG = LogFactory.getLog(classOf[ScalaCompiler])

  def compile(sources: List[AbstractFile]) : AbstractFile = {
    LOG.info("Compiling " + sources)
    val dir = new VirtualDirectory("memory", None)

    compiler.genJVM.outputDir = dir
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

    override lazy val classPath0 = new ScalaClasspath(false && onlyPresentation)

    override lazy val classPath = {
      require(!forMSIL, "MSIL not supported")
      new classPath0.BuildClasspath(settings.classpath.value, settings.sourcepath.value,
        settings.outdir.value, settings.bootclasspath.value, settings.extdirs.value,
        settings.Xcodebase.value, bundles.toArray[AbstractFile])
    }
  }

  class ScalaClasspath(onlyPresentation: Boolean) extends ClassPath(onlyPresentation) {

    class BuildClasspath(classpath: String, source: String, output: String, boot: String, extdirs: String,
                         codebase: String, classes: Array[AbstractFile])
            extends Build(classpath, source, output, boot, extdirs, codebase) {

      if (bundles != null) {
        for (file <- bundles if file != null) {
          val lib = new Library(file)
          entries += lib
        }
      }
    }
  }
}