/**

 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.scaraf.deployer

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.osgi.service.url.AbstractURLStreamHandlerService

/**
 * A URL handler that will transform a Scala source file to an OSGi bundle
 * on the fly.  Needs to be registered in the OSGi registry.
 */
class ScalaURLHandler extends AbstractURLStreamHandlerService {
  
  private var LOG: Log = LogFactory.getLog(classOf[ScalaURLHandler])
  private var SYNTAX: String = "scala:<scala-source-uri>"

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
    return new Connection(url)
  }

  class Connection(val source: URL) extends URLConnection(source) {

    override def getInputStream: InputStream = {
      try {
        var os: ByteArrayOutputStream = new ByteArrayOutputStream
        ScalaTransformer.transform(source, os)
        os.close
        return new ByteArrayInputStream(os.toByteArray)
      }
      catch {
        case e: Exception => {
          LOG.error("Error opening spring xml url", e)
          throw new IOException("Error opening spring xml url").initCause(e).asInstanceOf[IOException]
        }
      }
    }

    def connect: Unit = {
    }
  }
}
