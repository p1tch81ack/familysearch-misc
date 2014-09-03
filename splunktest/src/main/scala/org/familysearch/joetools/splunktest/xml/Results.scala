package org.familysearch.joetools.splunktest.xml

import scala.xml.{Node, NodeSeq}

class Results(node: Node) {
  val meta = {
    val metaNodes: NodeSeq = node \ "meta"
    new Meta(metaNodes.head)
  }
  val results = {
    val resultNodes: NodeSeq = node \ "result"
    (for(resultNode <- resultNodes) yield {
      val result = new Result(resultNode)
      result.id -> result
    }).toMap
  }
}
