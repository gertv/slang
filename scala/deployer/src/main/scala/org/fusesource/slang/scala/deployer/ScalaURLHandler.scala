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

import reflect.BeanProperty
import java.io._
import java.net._
import org.apache.commons.logging._
import org.osgi.service.url.AbstractURLStreamHandlerService
import org.osgi.framework.BundleContext

/**
 * A URL handler that will transform a Scala source file into an OSGi bundle
 * on the fly.  Needs to be registered in the OSGi registry.
 */
class ScalaURLHandler extends AbstractURLStreamHandlerService {
  
  private var LOG: Log = LogFactory.getLog(classOf[ScalaURLHandler])
  private var PREFIX: String = "scala:"
  private var SYNTAX: String = PREFIX + "<scala-source-uri>"

  @BeanProperty var bundleContext : BundleContext = null

  /**
   * Open the connection for the given URL.
   *
   * @param url the url from which to open a connection.
   * @return a connection on the specified URL.
   * @throws IOException if an error occurs or if the URL is malformed.
   */
  def openConnection(url: URL): URLConnection = {
    if (url.getPath == null || url.getPath.trim.length == 0) {
      throw new MalformedURLException("Path can not be null or empty. Syntax: " + SYNTAX)
    }
    LOG.debug("Scala source URL is: [" + url.getPath + "]")
    new Connection(url)
  }

  class Connection(val source: URL) extends URLConnection(source) {

    override def getInputStream: InputStream = {
      try {
        val url = if (source.toExternalForm.startsWith(PREFIX)) {
          new URL(source.toExternalForm.substring(PREFIX.length))
        } else {
          source
        }

        new ScalaSource (url, bundleContext).transform()

      }
      catch {
        case e: Exception => {
          LOG.error("Error creating bundle from Scala source code", e)
          throw new IOException("Error opening spring xml url").initCause(e).asInstanceOf[IOException]
        }
      }
    }

    def connect {}
  }
}
