package org.familysearch.joetools.splunktest.xml

case class SplunkId(splunkServer: String, index: String, cd: String){
  def this(result: Result) = this(result.splunkServer, result.index, result.cd)
}
