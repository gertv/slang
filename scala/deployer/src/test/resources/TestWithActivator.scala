import org.osgi.framework.{BundleContext, BundleActivator}

package org.test {

class TestWithActivator extends BundleActivator {

  def start (context: BundleContext): Unit = {
    println("Starting")
  }

  def stop (context: BundleContext): Unit = {
    println("Stopping")
  }
}

}