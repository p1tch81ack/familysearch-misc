package org.familysearch.joetools.splunktest

import java.io.FileInputStream
import java.util.{Calendar, Date}
import org.apache.commons.lang3.time.DateUtils

import org.familysearch.joetools.splunktest.xml.{SplunkId, Results}

import scala.collection.SortedSet
import scala.collection.immutable.TreeMap

object App {

  val testStart: String = "2012-06-19T12:00:00.000-07:00"
  val testEnd: String = "2012-06-20T12:00:00.000-07:00"

  def main(args : Array[String]) {
    /*
    println("Original: " + testStart)
    val parsedOriginal = SplunkConnection.splunkDateTimeFormat.parse(testStart)
    println("Parsed Original: " + parsedOriginal)
    val formatted = SplunkConnection.splunkDateTimeFormat.format(parsedOriginal)
    println("Formatted: " + formatted)
    */
    val savedSearchName: String = "ReportAbuse-Pingdom-Healthcheck"
    val connection = SplunkConnection("ut01-splunksch04.i.fsglobal.net", "shullja", "***REMOVED***")

//    val endpoint: String = "/reportabuse/ "
//    val endpoint: String = "/watch/ "
    val endpoint = "/links/healthcheck/vitals "
    val rawSearch = generateProductionLoadBalancerQuery(endpoint)
    //    val targetDate = DateUtils.parseDateStrictly("2014-08-26 15:10", "yyyy-MM-dd HH:mm")
//    val targetDate = DateUtils.parseDateStrictly("2014-08-26 15:14", "yyyy-MM-dd HH:mm")
//    val targetDate = DateUtils.parseDateStrictly("2014-08-09 16:41", "yyyy-MM-dd HH:mm")
    val targetDate = DateUtils.parseDateStrictly("2014-08-22 19:11", "yyyy-MM-dd HH:mm")
    /*
    for (user <- connection.getUsers) {
      println("User: " + user.getName)
    }

    for (app: Application <- connection.getApps) {
      println("App: " + app.getName)
    }

    for (savedSearch <- connection.getSavedSearches("fs-lynx").values().asScala) {
      println("Saved Search: " + savedSearch.getName)
    }

*/

//    val appName: String = "fs-lynx"
//    val results = connection.executeSavedSearch(appName, savedSearchName)
//    val testFormat = new SimpleDateFormat("yyyy'-'MM'-'DD HH':'mm")
//    val testFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm")
//    val testString = testFormat.format(new Date)
//    val testDate = testFormat.parse("2014-12-26 15:10")
    val startDate = DateUtils.addMinutes(targetDate, -10)
    val endDate = DateUtils.addMinutes(targetDate, 10)
    val results = connection.executeOneShotSearch(rawSearch, startDate, endDate)
//    val results = loadFile("results.xml")
    val resultsMap = groupByTimeStampWithMinuteResolution(results)

    println("Fields: " + results.meta.fieldOrder)
    for(time<-resultsMap.keys){
      val resultList = resultsMap(time)
      for(resultId<-resultList) {
        val result = results.results(resultId)
        println("" + time + "  " + result.offset + ": " + result.fields)
      }
    }

    val missingMinutes = findMissingMinutes(results)
    println("Missing Minutes: " + missingMinutes)
  }

  def generateProductionLoadBalancerQuery(endpoint: String): String = {
    "search index=infrastructure sourcetype=haproxy rqst=\"GET " +
      endpoint +
      "*\" name_f=\"www.familysearch.org\" OR name_f=\"api.familysearch.org\"  name_s=\"*prod*\" hdrs=\"*Pingdom*\""
  }

  def loadFile(fileName: String): Results = {
    SplunkConnection.parseInput(new FileInputStream(fileName))
  }

  def groupByTimeStampWithMinuteResolution(results: Results): TreeMap[Date, List[SplunkId]] = {
    var resultMap = TreeMap[Date, List[SplunkId]]()
    for(resultId <- results.results.keySet){
      val result = results.results(resultId)
      val roundedTime = DateUtils.round(result.time, Calendar.MINUTE)
      val resultList  = {
        if (resultMap.contains(roundedTime)) {
          resultMap(roundedTime)
        } else {
          List[SplunkId]()
        }
      }
      resultMap = resultMap + (roundedTime -> resultList.+:(result.id))
    }
    resultMap
  }


  def findMissingMinutes(results: Results): SortedSet[Date] = {
    val mappedResults = groupByTimeStampWithMinuteResolution(results)
    val missingMinutes = new scala.collection.mutable.TreeSet[Date]
    val minDate = mappedResults.firstKey
    val maxDate = mappedResults.lastKey
    val differenceMillis = maxDate.getTime - minDate.getTime
    val minuteCount = ((differenceMillis/60000) + 1).asInstanceOf[Int]
    for(i <- 0 until minuteCount){
      val candidateMinute = DateUtils.addMinutes(minDate, i)
      if(!mappedResults.contains(candidateMinute)){
        missingMinutes.add(candidateMinute)
      }
    }
    missingMinutes
  }

}
