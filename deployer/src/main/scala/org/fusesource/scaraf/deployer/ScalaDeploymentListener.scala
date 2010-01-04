package org.fusesource.scaraf.deployer

import org.apache.felix.fileinstall.ArtifactUrlTransformer
import java.io.File
import java.net.URL
import org.apache.commons.logging.LogFactory;

/**
 * Created by IntelliJ IDEA.
 * User: gert
 * Date: Jan 4, 2010
 * Time: 11:47:08 AM
 * To change this template use File | Settings | File Templates.
 */
class ScalaDeploymentListener extends ArtifactUrlTransformer {

  val LOG = LogFactory.getLog(classOf[ScalaDeploymentListener])

  def canHandle(artifact: File) = {
    LOG.info("Can we handle " + artifact.getAbsolutePath)
    artifact.isFile() && artifact.getName().endsWith(".scala")
  }

  def transform(artifact: URL) : URL = {
    try {
        return new URL("scala", null, artifact.toString());
    } catch {
      case e: Exception => {
        LOG.error("Unable to build scala bundle", e);
        return null;
      }
    }
  }
}