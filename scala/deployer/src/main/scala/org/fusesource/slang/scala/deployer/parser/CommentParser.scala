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

sealed abstract class Item
case class Code (code : String) extends Item
case class Comment (code : String) extends Item

object CommentParser extends RegexParsers {

	override def skipWhitespace = false

	private def spaces = regex("""[ \t\n]*""".r)

	private def openComment = spaces ~> regex("""/\*""".r)

	private def closeComment = regex("""\*/""".r) <~ spaces

	private def textComment = regex("""^((?!\*/).|\n)*""".r)

	private def comment : Parser[Comment] = openComment ~> textComment <~ closeComment ^^ { case s : String => println("Found comment"); Comment(s) }

	/* NOTE: Use + instead of * in the following regexp
	   to avoid infinite loops while parsing. */
	private def code : Parser[Code] = regex("""^((?!/\*).|\n)+""".r) ^^ { case s : String => println("Found code"); Code(s) }

	private def items : Parser[List[Item]] = (( comment | code ) * )

	def parse (s : String) : ParseResult[List[Item]] = {
		println("Parsing")
		val res = parse (items, s)
		println("Parsed")
		res
	}

}
