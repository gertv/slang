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

import org.apache.felix.fileinstall.ArtifactUrlTransformer
import java.io.File
import java.net.URL
import org.apache.commons.logging.LogFactory;

/**
 * Artifact deployment listener - whenever a .scala file gets deployed, this
 * will transform the URL into a scala: URL suitable for {@link ScalaURLHandler}
 */
class ScalaDeploymentListener extends ArtifactUrlTransformer {

  val LOG = LogFactory.getLog(classOf[ScalaDeploymentListener])

  def canHandle(artifact: File) = {
    artifact.isFile() && artifact.getName().endsWith(".scala")
  }

  def transform(artifact: URL) : URL = {
    try {
        new URL("scala", null, artifact.toString());
    } catch {
      case e: Exception => {
        LOG.error("Unable to build scala bundle", e);
        return null;
      }
    }
  }
}