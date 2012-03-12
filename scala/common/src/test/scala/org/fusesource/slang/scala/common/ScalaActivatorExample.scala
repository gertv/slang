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