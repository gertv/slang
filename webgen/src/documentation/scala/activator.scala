import org.osgi.framework.{BundleContext,BundleActivator}

package org.fusesource.slang.scala.activator {

  class MyActivator extends BundleActivator {
  
    def start(context: BundleContext) = println("Starting the bundle")
    
    def stop(context: BundleContext) = println("Stopping the bundle")
  
  }

}