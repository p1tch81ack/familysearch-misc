package org.familysearch.joetools.splunktest.xml

import scala.xml.{NodeSeq, Node}

class Meta(node: Node) {
  val fieldOrder = {
    val fieldOrderNodes = node \ "fieldOrder"
    val fieldNodes: NodeSeq = fieldOrderNodes.head \ "field"
    for(fieldNode <- fieldNodes) yield fieldNode.text
  }
}
