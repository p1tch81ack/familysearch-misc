package org.familysearch.joetools.simpledb


class RowSpecifier(private val test: Test) {

  def this(tag: String, value: String) {
    this(new HasTagValue(tag, value))
  }

  private def this(parent: RowSpecifier, test: Test) {
    this(new And(parent.test, test))
  }

  private def this(parent: RowSpecifier, tag: String, value: String) {
    this(parent, new HasTagValue(tag, value))
  }

  def this() {
    this(new True)
  }

  def `with`(tag: String, value: String): RowSpecifier = {
    if (value != null) {
      new RowSpecifier(this, tag, value)
    }
    else {
      this
    }
  }

  def and(test: Test): RowSpecifier = {
    new RowSpecifier(this, test)
  }

  def andNot(test: Test): RowSpecifier = {
    new RowSpecifier(this, new Not(test))
  }

  def without(tag: String, value: String): RowSpecifier = {
    andNot(new HasTagValue(tag, value))
  }

  def matches(target: RowIndexEntry): Boolean = {
    test.evaluate(target)
  }
}
