package org.familysearch.joetools.splunktest.xml

import org.familysearch.joetools.splunktest.SplunkConnection

import scala.xml.Node

class Result(node: Node) {
  val offset = node.attributes("offset").text.toInt
  val fields = {
    val fieldNodes = node \ "field"
    (for(fieldNode <- fieldNodes) yield {
      val key = fieldNode.attributes("k").text
      val valueNodes = fieldNode \ "value"
      if(valueNodes.length>0) {
        val textNodes = valueNodes.head \ "text"
        (key, textNodes.head.text)
      } else {
        val vNodes = fieldNode \ "v"
        (key, vNodes.head.text)
      }
    }).toMap
  }
  def splunkServer = fields("splunk_server")
  def index = fields("index")
  def cd = fields("_cd")
  def time = SplunkConnection.splunkDateTimeFormat.parse(fields("_time"))
  def id = new SplunkId(this)
}
