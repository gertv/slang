/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.scaraf.deployer.compiler {

import tools.nsc.io.{VirtualFile, AbstractFile}
import scala.collection.{mutable=>mut}

/**
 * Temporary workaround for a bug in scala.tools.nsc.io.VirtualDirecotry
 * This file can be removed as soon as Scala 2.8.0 is out
 *
 * NSC -- new Scala compiler
 * Copyright 2005-2009 LAMP/EPFL
 */
class VirtualDirectory(val name: String, maybeContainer: Option[VirtualDirectory]) extends AbstractFile {

  def path: String =
    maybeContainer match {
      case None => name
      case Some(parent) => parent.path+'/'+ name
    }
  def container = maybeContainer.get
  def isDirectory = true
  var lastModified: Long = System.currentTimeMillis
  private def updateLastModified {
    lastModified = System.currentTimeMillis
  }
  override def file = null
  override def input = error("directories cannot be read")
  override def output = error("directories cannot be written")

  private val files = mut.Map.empty[String, AbstractFile]

  // the toList is so that the directory may continue to be
  // modified while its elements are iterated
  def elements = files.values.toList.elements

  override def lookupName(name: String, directory: Boolean): AbstractFile = {
    files.get(name) match {
      case None => null
      case Some(file) =>
        if (file.isDirectory == directory)
          file
        else
          null
    }
  }

  override def fileNamed(name: String): AbstractFile = {
    val existing = lookupName(name, false)
    if (existing == null) {
      val newFile = new VirtualFile(name, path+'/'+name)
      files(name) = newFile
      newFile
    } else {
      existing
    }
  }

  override def subdirectoryNamed(name: String): AbstractFile = {
    val existing = lookupName(name, true)
    if (existing == null) {
      val dir = new VirtualDirectory(name, Some(this))
      files(name) = dir
      dir
    } else {
      existing
    }
  }
}

}
