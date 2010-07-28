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
package org.fusesource.slang.scala.itests

import org.junit.runner.RunWith
import org.osgi.framework.BundleContext
import org.junit.Test
import org.junit.Assert.{assertNotNull}
import org.ops4j.pax.exam.Inject
import org.ops4j.pax.exam.CoreOptions.{options, mavenBundle, systemProperties, systemProperty, bootDelegationPackage, provision}
import org.ops4j.pax.exam.container.`def`.PaxRunnerOptions.{scanFeatures}
import org.ops4j.pax.exam.junit.{Configuration, JUnit4TestRunner}
import java.util.jar.JarInputStream
import java.io.{FileOutputStream, ByteArrayInputStream, ByteArrayOutputStream}
import org.ops4j.pax.exam.options.ProvisionOption
import org.ops4j.pax.exam.container.`def`.options.FeaturesScannerProvisionOption

/**
 * Skeleton class for starting to build more extensive integration tests
 */
@RunWith(classOf[JUnit4TestRunner])
class ScalaTransformerIntegration {

    @Inject
    var context: BundleContext = null 

    @Test
    def testMethod()
    {
        assertNotNull(context)

    }


  @Configuration
  def configuration: Array[org.ops4j.pax.exam.Option] = {
    println("Configuring")
    options(
      provision(
        "mvn:org.apache.servicemix.bundles/org.apache.servicemix.bundles.scala-library/2.8.0_1-SNAPSHOT"
      ),
      systemProperties(
        systemProperty("org.ops4j.pax.url.mvn.repositories").value("http://www.scala-tools.com/repo-releases"),
        systemProperty("org.ops4j.pax.url.mvn.defaultRepositories").value("http://www.scala-tools.com/repo-releases")
      )
  )}
}