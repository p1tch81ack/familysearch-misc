package org.familysearch.joetools.simpledb


class SimpleTable[T<:SimpleRow] {
  private val rows = new java.util.HashMap[RowIndexEntry, java.util.List[T]]

  def addRow(row: T) {
    val indexEntry: RowIndexEntry = row.getRowIndexEntry
    var rowList: java.util.List[T] = rows.get(indexEntry)
    if (rowList == null) {
      rowList = new java.util.LinkedList[T]
      rows.put(indexEntry, rowList)
    }
    rowList.add(row)
  }

  def getMatchingRows(matchSpecifier: RowSpecifier): java.util.List[T] = {
    val matchingRows: java.util.List[T] = new java.util.LinkedList[T]
    import scala.collection.JavaConversions._
    for (rowIndexEntry <- rows.keySet) {
      if (matchSpecifier.matches(rowIndexEntry)) {
        matchingRows.addAll(rows.get(rowIndexEntry))
      }
    }
    matchingRows
  }

  def getMapOfMatchingRows(matchSpecifier: RowSpecifier): java.util.Map[RowIndexEntry, java.util.List[T]] = {
    val matchingRowMap: java.util.Map[RowIndexEntry, java.util.List[T]] = new java.util.HashMap[RowIndexEntry, java.util.List[T]]
    import scala.collection.JavaConversions._
    for (rowIndexEntry <- rows.keySet) {
      if ((matchSpecifier eq null) || matchSpecifier.matches(rowIndexEntry)) {
        matchingRowMap.put(rowIndexEntry, rows.get(rowIndexEntry))
      }
    }
    matchingRowMap
  }

  def getSpecifierValuesForMatchingRows(rowSpecifier: RowSpecifier, specifierTag: String): java.util.Set[String] = {
    val specifierValues: java.util.Set[String] = new java.util.TreeSet[String]
    import scala.collection.JavaConversions._
    for (rowIndexEntry <- getMapOfMatchingRows(rowSpecifier).keySet) {
      val specifierValue: String = rowIndexEntry.getSpecifierValue(specifierTag)
      if (!(specifierValue eq null)) {
        specifierValues.add(specifierValue)
      }
    }
    specifierValues
  }

  def getMappedListOfValuesMatchingSpecifierGrupedByConcatinatedUniqueValues(rowSpecifier: RowSpecifier, function: Function[T, String], separator: String): java.util.Map[String, _ <: java.util.List[String]] = {
    val out: java.util.Map[String, java.util.LinkedList[String]] = new java.util.TreeMap[String, java.util.LinkedList[String]]
    val mappedRows: java.util.Map[RowIndexEntry, java.util.List[T]] = getMapOfMatchingRows(rowSpecifier)
    import scala.collection.JavaConversions._
    for (rowIndexEntry <- mappedRows.keySet) {
      val key: String = concatinateUnspecifiedRowSpecifierValues(rowSpecifier, rowIndexEntry, separator)
      import scala.collection.JavaConversions._
      for (row <- mappedRows.get(rowIndexEntry)) {
        val stringValue: String = function.get(row)
        if (stringValue != null) {
          var lines: java.util.LinkedList[String] = out.get(key)
          if (lines == null) {
            lines = new java.util.LinkedList[String]
            out.put(key, lines)
          }
          lines.add(stringValue)
        }
      }
    }
    out
  }

  private def concatinateUnspecifiedRowSpecifierValues(rowSpecifier: RowSpecifier, rowIndexEntry: RowIndexEntry, separator: String): String = {
    var key: String = ""
    import scala.collection.JavaConversions._
    for (tag <- rowIndexEntry.getSpecifierTags) {
      if (rowSpecifier.matches(rowIndexEntry.remove(tag))) {
        if (key.length > 0) {
          key += separator
        }
        key += rowIndexEntry.getSpecifierValue(tag)
      }
    }
    key
  }

  def getAverageValueForMatchingRows(rowSpecifier: RowSpecifier, function: Function[T, java.lang.Double]): Double = {
    var total: Double = 0.0
    var count: Int = 0
    import scala.collection.JavaConversions._
    for (row <- getMatchingRows(rowSpecifier)) {
      val item: java.lang.Double = function.get(row)
      if (!(item eq null)) {
        total += item.doubleValue()
        count += 1
      }
    }
    if (count > 0) {
      total / count.asInstanceOf[Double]
    }
    else {
      0.0
    }
  }
}
