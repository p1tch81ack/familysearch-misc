package org.familysearch.joetools.simpledb

trait Function[T<:SimpleRow, R] {
  def get(row: T): R
}
