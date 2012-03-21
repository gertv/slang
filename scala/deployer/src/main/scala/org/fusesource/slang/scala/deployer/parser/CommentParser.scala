/**
 * Copyright (C) Crossing-Tech SA, 2012.
 * Contact: <guillaume.yziquel@crossing-tech.com>
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
package org.fusesource.slang.scala.deployer.parser

import scala.util.parsing.combinator._

object CommentParser extends RegexParsers {

	override def skipWhitespace = false

	private def openComment = regex("""/\*""".r)

	private def closeComment = regex("""\*/""".r)

	private def textComment = regex("""^(((?!\*/).|\n))*""".r)

	private def comment = openComment ~> textComment <~ closeComment

	def parseComment (s : String) : ParseResult[Any] = parse (comment, s)

}
