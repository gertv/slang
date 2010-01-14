import org.osgi.framework.{BundleContext,BundleActivator}

package org.test {

class MyBase {
  
  println("Hello from MyBean!")
  
}

package demo {

  class MyDemo extends MyBase
  
  class TestWithActivator extends BundleActivator {

  def start (context: BundleContext): Unit = {
    println("Welcome from Activator")
  }

  def stop (context: BundleContext): Unit = {
    println("Bye bye from Activator")
  }
}

}

}