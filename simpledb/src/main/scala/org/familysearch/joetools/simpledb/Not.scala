package org.familysearch.joetools.simpledb

class Not(private val test: Test) extends Test {
  def evaluate(rowIndexEntry: RowIndexEntry): Boolean = !test.evaluate(rowIndexEntry)
}
