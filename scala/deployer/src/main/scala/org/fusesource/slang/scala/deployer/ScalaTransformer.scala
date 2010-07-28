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
package org.fusesource.slang.scala.deployer

import archiver.ScalaArchiver
import compiler.{Bundles, ScalaCompiler}
import org.apache.commons.logging.LogFactory
import tools.nsc.io.{PlainFile, AbstractFile}
import java.net.URL
import org.osgi.framework.{BundleContext, Bundle}
import java.io.{InputStream, File, OutputStream}

/**
 * 
 */
class ScalaTransformer(val bundles: List[AbstractFile]) {

  final val LOG = LogFactory.getLog(classOf[ScalaTransformer])

  val compiler = new ScalaCompiler(bundles)

  val archiver = new ScalaArchiver(bundles) 

  def transform(url: URL, stream: OutputStream) : Unit = {
    val result = transform(url)
    val bytes = new Array[Byte](1024)
    var read = result.read(bytes)
    while (read > 0) {
      stream.write(bytes, 0, read)
      read = result.read(bytes)
    }
    result.close
  }

  def transform(url: URL) : InputStream = {
    LOG.info("Transforming " + url + " into an OSGi bundle")
    archiver.archive(compile(url), url)
  }

  def compile(url: URL) = compiler.compile(files(url))

  def files(url: URL) : List[AbstractFile] = {
    if ("file" == url.getProtocol) {
      List(new PlainFile(new File(url.toURI)))
    } else {
      List(AbstractFile.getURL(url))
    }
  }
}

object ScalaTransformer {

  def create(context: BundleContext) = {
    val bundles : List[AbstractFile] = if (context == null) {
      List()
    } else {
      val framework = context.getProperty("karaf.framework")
      val jar = new File(context.getProperty("karaf.base"), context.getProperty("karaf.framework." + framework))
      AbstractFile.getDirectory(jar) :: Bundles.create(context.getBundles)
    }
    new ScalaTransformer(bundles)
  }

  def create(libraries: List[AbstractFile]) = new ScalaTransformer(libraries)

}