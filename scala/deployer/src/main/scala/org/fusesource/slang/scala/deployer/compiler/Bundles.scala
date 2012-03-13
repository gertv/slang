/**
 * Copyright (C) FuseSource, Inc.
 * http://fusesource.com
 *
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
package org.fusesource.slang.scala.deployer.compiler

import org.osgi.framework.Bundle
import java.io.{InputStream, IOException, File}
import scala.tools.nsc.io.{PlainFile, AbstractFile}
import java.net.{URISyntaxException, URL}
import org.apache.commons.logging.LogFactory
import java.lang.String

/**
 * Helper methods to transform OSGi bundles into {@link AbstractFile} implementations
 * suitable for use with the Scala compiler
 */
object Bundles {

  val LOG = LogFactory.getLog(Bundles.getClass)

  abstract class BundleEntry(url: URL, parent: DirEntry) extends AbstractFile {
    require(url != null, "url must not be null")

    val bundle : Bundle

    lazy val (path: String, name: String) = getPathAndName(url)
    lazy val fullName: String = (path::name::Nil).filter(!_.isEmpty).mkString("/")

    /**
     * @return null
     */
    def file: File = null

    /**
     * @return last modification time or 0 if not known
     */
    def lastModified: Long =
      try { url.openConnection.getLastModified }
      catch { case _ => 0 }

    @throws(classOf[IOException])
    def container: AbstractFile =
      valueOrElse(parent) {
        throw new IOException("No container")
      }

    @throws(classOf[IOException])
    def input: InputStream = url.openStream()

    /**
     * Not supported. Always throws an IOException.
     * @throws IOException
     */
    @throws(classOf[IOException])
    def output = throw new IOException("not supported: output")

    private def getPathAndName(url: URL): (String, String) = {
      val u = url.getPath
      var k = u.length
      while( (k > 0) && (u(k - 1) == '/') )
        k = k - 1

      var j = k
      while( (j > 0) && (u(j - 1) != '/') )
        j = j - 1

      (u.substring(if (j > 0) 1 else 0, if (j > 1) j - 1 else j), u.substring(j, k))
    }

    override def toString = fullName
  }

  class DirEntry(val bundle: Bundle, url: URL, parent: DirEntry) extends BundleEntry(url, parent) {

    /**
     * @return true
     */
    def isDirectory: Boolean = true

    override def elements: Iterator[AbstractFile] = {
      new Iterator[AbstractFile]() {
        val dirs = bundle.getEntryPaths(fullName)
        var nextEntry = prefetch()

        def hasNext() = {
          if (nextEntry == null)
            nextEntry = prefetch()

          nextEntry != null
        }

        def next() = {
          if (hasNext()) {
              val entry = nextEntry
              nextEntry = null
              entry
          }
          else {
            throw new NoSuchElementException()
          }
        }

        private def prefetch() = {
          if (dirs.hasMoreElements) {
            val entry = dirs.nextElement.asInstanceOf[String]
            var entryUrl = bundle.getResource("/" + entry)

            // Bundle.getResource seems to be inconsistent with respect to requiring
            // a trailing slash
            if (entryUrl == null)
              entryUrl = bundle.getResource("/" + removeTralingSlash(entry))

            // If still null OSGi wont let use load that resource for some reason
            if (entryUrl == null) {
              null
            }
            else {
              if (entry.endsWith(".class"))
                new FileEntry(bundle, entryUrl, DirEntry.this)
              else
                new DirEntry(bundle, entryUrl, DirEntry.this)
            }
          }
          else
            null
        }

        private def removeTralingSlash(s: String): String =
          if (s == null || s.length == 0)
            s
          else if (s.last == '/')
            removeTralingSlash(s.substring(0, s.length - 1))
          else
            s
      }
    }

    def lookupName(name: String, directory: Boolean): AbstractFile = {
      val entry = bundle.getEntry(fullName + "/" + name)
      nullOrElse(entry) { entry =>
        if (directory)
          new DirEntry(bundle, entry, DirEntry.this)
        else
          new FileEntry(bundle, entry, DirEntry.this)
      }
    }

    override def lookupPathUnchecked(path: String, directory: Boolean) = lookupPath(path, directory)
    def lookupNameUnchecked(name: String, directory: Boolean) = lookupName(path, directory)

    def iterator = elements

    def absolute = unsupported("absolute() is unsupported")
    def create = unsupported("create() is unsupported")
    def delete = unsupported("create() is unsupported")
  }

  class FileEntry(val bundle: Bundle, url: URL, parent: DirEntry) extends BundleEntry(url, parent) {

    /**
     * @return false
     */
    def isDirectory: Boolean = false
    override def sizeOption: Option[Int] = Some(bundle.getEntry(fullName).openConnection().getContentLength())
    override def elements: Iterator[AbstractFile] = Iterator.empty
    def lookupName(name: String, directory: Boolean): AbstractFile = null

    override def lookupPathUnchecked(path: String, directory: Boolean) = lookupPath(path, directory)
    def lookupNameUnchecked(name: String, directory: Boolean) = lookupName(path, directory)

    def iterator = elements

    def absolute = unsupported("absolute() is unsupported")
    def create = unsupported("create() is unsupported")
    def delete = unsupported("create() is unsupported")
  }

  /**
   * Create an array of {@link AbstractFile}s for a given array of bundles.
   * If a bundle has a file: URL, a {@PlainFile} is being used, otherwise w
   */
  def create(bundles: Array[Bundle]) : List[AbstractFile] = {
    var result : List[AbstractFile] = List()
    for (bundle <- bundles; val index = bundles.indexOf(bundle)) {
        var url = bundle.getResource("/");
        if (url == null) {
            url = bundle.getResource("");
        }

        if (url != null) {
            if ("file" == url.getProtocol()) {
                try {
                    result = new PlainFile(new File(url.toURI())) :: result;
                }
                catch {
                  case e: URISyntaxException => throw new IllegalArgumentException("Can't determine url of bundle " + bundle, e);
                }
            }
            else {
                result = Bundles.create(bundle) :: result;
            }
        }
        else {
            LOG.warn("Cannot retreive resources from Bundle. Skipping " + bundle.getSymbolicName());
        }
    }
    result;
  }


  /**
   *  Create a new  { @link AbstractFile } instance representing an
   * { @link org.osgi.framework.Bundle }
   *
   * @param bundle the bundle
   */
  def create(bundle: Bundle): AbstractFile = {
    require(bundle != null, "bundle must not be null")

    new DirEntry(bundle, bundle.getResource("/"), null)
  }

  /**
   * Evaluate <code>f</code> on <code>s</code> if <code>s</code> is not null.
   * @param s
   * @param f
   * @return <code>f(s)</code> if s is not <code>null</code>, <code>null</code> otherwise.
   */
  def nullOrElse[S, T](s: S)(f: S => T): T =
    if (s == null) null.asInstanceOf[T]
    else f(s)

  /**
   * @param t
   * @param default
   * @return <code>t</code> or <code>default</code> if <code>null</code>.
   */
  def valueOrElse[T](t: T)(default: => T) =
    if (t == null) default
    else t  
}

