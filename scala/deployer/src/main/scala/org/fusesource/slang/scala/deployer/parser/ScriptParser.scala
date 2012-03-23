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

abstract class Commented extends Item
case class Comment (code : String) extends Commented
case class Manifest () extends Commented

object ManifestParser extends RegexParsers {

	override def skipWhitespace = false

	private def spaces = regex("""([ \t\n]|\n[ \t]*\*[ \t]*)*""".r)

	private def manifestHeader = spaces ~> regex("""OSGI-MANIFEST:""".r) <~ spaces ^^ { case _ : String => {}}

	private def manifest = regex("""\**""".r) ~> manifestHeader

	def parse (comment : Comment) : Boolean = comment match {
	case Comment (c) => parse (manifest, c) match {
		case Success ((), _) => true
		case Failure (msg, _) => false
		case Error (msg, _) => throw new Exception (
			"Slang deployer parsing error: " + msg)
	}}

}

object ScriptParser extends RegexParsers {

	override def skipWhitespace = false

	private def spaces = regex("""[ \t\n]*""".r)

	private def openComment = spaces ~> regex("""/\*""".r)

	private def closeComment = regex("""\*/""".r) <~ spaces

	private def textComment = regex("""^((?!\*/).|\n)*""".r) ^^ (Comment(_))

	private def comment : Parser[Commented] = openComment ~> textComment <~ closeComment ^^ {
	case c : Comment => ManifestParser.parse(c) match {
		case true => Manifest ()
		case false => c
	}}

	/* NOTE: Use + instead of * in the following regexp
	   to avoid infinite loops while parsing. */
	private def code : Parser[Code] = regex("""^((?!/\*).|\n)+""".r) ^^ (Code(_))

	private def items : Parser[List[Item]] = (( comment | code ) * )

	def parse (s : String) : ParseResult[List[Item]] = parse (items, s)

}
