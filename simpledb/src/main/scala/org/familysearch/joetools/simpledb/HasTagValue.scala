package org.familysearch.joetools.simpledb

class HasTagValue(private var tag: String, private var value: String) extends Test {
  def evaluate(rowIndexEntry: RowIndexEntry): Boolean = value == rowIndexEntry.getSpecifierValue(tag)
}
