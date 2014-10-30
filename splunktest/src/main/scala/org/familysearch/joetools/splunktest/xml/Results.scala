package org.familysearch.joetools.splunktest.xml

import scala.xml.{Node, NodeSeq}

class Results(node: Node) {
  val metas = {
    val metaNodes: NodeSeq = node \ "meta"
    (for(metaNode<-metaNodes) yield new Meta(metaNode)).toList
  }
  val results = {
    val resultNodes: NodeSeq = node \ "result"
    (for(resultNode <- resultNodes) yield {
      val result = new Result(resultNode)
      result.id -> result
    }).toMap
  }
}
