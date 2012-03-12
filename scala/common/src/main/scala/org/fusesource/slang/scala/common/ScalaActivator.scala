/**
 * Copyright (C) FuseSource, Inc.
 * http://fusesource.com
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
import org.osgi.framework.{BundleContext, BundleActivator}
import scala.reflect.Manifest
import scala.collection.mutable.ListBuffer

package org.fusesource.slang.scala.common {
import java.util.{Hashtable, Dictionary}

class ScalaActivator extends BundleActivator {

  val services = new ListBuffer[Service]

  def start(context: BundleContext): Unit = {
    services.foreach(service => service.register(context))
  }

  def stop(context: BundleContext) = {}

  def publish(block: => AnyRef) = {
    val service = new Service(block)
    services += service
    service
  }

  class Service(val service: AnyRef) {

    val types = new ListBuffer[String]

    def register(context: BundleContext) = {
      println(service + "-> " + types)
      context.registerService(types.toArray, service, new Hashtable())
    }

    def as[T](implicit manifest: Manifest[T]) = types += manifest.erasure.getName
  }
}

}