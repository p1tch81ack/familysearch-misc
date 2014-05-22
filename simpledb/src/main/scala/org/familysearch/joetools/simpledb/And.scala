package org.familysearch.joetools.simpledb

class And(private var left: Test, private var right: Test) extends Test {
  def evaluate(rowIndexEntry: RowIndexEntry): Boolean = left.evaluate(rowIndexEntry) && right.evaluate(rowIndexEntry)
}
