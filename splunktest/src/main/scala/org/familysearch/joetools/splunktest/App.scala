package org.familysearch.joetools.splunktest

import com.splunk.{Service, ServiceArgs}

/**
 * @author ${user.name}
 */
object App {
  
  def foo(x : Array[String]) = x.foldLeft("")((a,b) => a + b)
  
  def main(args : Array[String]) {
    // Create a map of arguments and add login parameters
    val loginArgs: ServiceArgs = new ServiceArgs()
    loginArgs.setUsername("shullja")
    loginArgs.setPassword("N3verGuess2014")
    //https://splunk.vip.fsglobal.net
//    loginArgs.setHost("localhost")
    loginArgs.setHost("ut01-splunksch04.i.fsglobal.net")
//    loginArgs.setScheme("HTTPS")
    loginArgs.setPort(8089)
//    loginArgs.setPort(443)
//    loginArgs.setApp()

    // Create a Service instance and log in with the argument map
    val service: Service = Service.connect(loginArgs)
  }


}
