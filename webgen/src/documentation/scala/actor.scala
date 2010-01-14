import scala.actors.Actor
import scala.actors.Actor._

import org.osgi.framework.{BundleContext, BundleActivator}

package org.fusesource.slang.scala.actor {

  // the messages we're going going to send
  case object Ping
  case object Pong
  case object Stop

  class Ping(count: int, pong: Actor) extends Actor {
    def act() {
      var pingsLeft = count - 1
      pong ! Ping
      while (true) {
        receive {
          case Pong =>
            if (pingsLeft % 1000 == 0)
              Console.println("Ping: pong")
            if (pingsLeft > 0) {  
              pong ! Ping
              pingsLeft -= 1
            } else {
              Console.println("Ping: stop")
              pong ! Stop
              exit
            }
          case Stop => 
            Console.println("Ping: stop")
            pong ! Stop
            exit
        }
      }
    }
  }

  class Pong extends Actor {
    def act() {
      var pongCount = 0
      while (true) {
        receive {
          case Ping =>
            if (pongCount % 1000 == 0)
              Console.println("Pong: ping "+pongCount)
            sender ! Pong
            pongCount = pongCount + 1
          case Stop =>
            Console.println("Pong: stop")
            exit
        }
      }
    }
  }

  class Activator extends BundleActivator {

    val pong = new Pong
    val ping = new Ping(10000, pong)

    def start(context: BundleContext) = {
      // start both actors
      ping.start
      pong.start
    }

    def stop(context: BundleContext) = {
      // send the Stop message to the ping actor, who will forward it to pong
      ping ! Stop
    }
  }
}