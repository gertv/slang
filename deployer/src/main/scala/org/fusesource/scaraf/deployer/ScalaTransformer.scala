package org.fusesource.scaraf.deployer

import java.io.OutputStream
import java.net.URL
import org.apache.commons.logging.LogFactory

/**
 * Created by IntelliJ IDEA.
 * User: gert
 * Date: Jan 4, 2010
 * Time: 1:39:17 PM
 * To change this template use File | Settings | File Templates.
 */
class ScalaTransformer

object ScalaTransformer {

  val LOG = LogFactory.getLog(classOf[ScalaTransformer])

  def transform(url: URL, stream: OutputStream) {
    LOG.info("Compiling " + url + " now...") 
  }

}