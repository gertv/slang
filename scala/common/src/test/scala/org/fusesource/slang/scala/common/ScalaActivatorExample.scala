package org.fusesource.slang.scala.common

/**
 * Created by IntelliJ IDEA.
 * User: gert
 * Date: Jan 25, 2010
 * Time: 9:22:25 PM
 * To change this template use File | Settings | File Templates.
 */

class ScalaActivatorExample extends ScalaActivator {

  publish {
    new Hello() {
      def hello = println("Hello")
    }
  }.as[Hello]

  trait Hello {

    def hello

  }
}