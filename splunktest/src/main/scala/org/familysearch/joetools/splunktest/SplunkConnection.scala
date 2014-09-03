package org.familysearch.joetools.splunktest

import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date

import com.splunk._
import org.familysearch.joetools.splunktest.xml.Results

import scala.collection.JavaConverters._
import scala.xml.{Elem, XML}

object SplunkConnection {
  val splunkDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  def apply(host: String, userName: String, password: String): SplunkConnection = new SplunkConnection(host, userName, password)
  def parseInput(inputStream: InputStream): Results = {
    val elem: Elem = XML.load(inputStream)
    new Results(elem)
  }
}

class SplunkConnection(host: String, userName: String, password: String) {
  val service = {
    val loginArgs: ServiceArgs = new ServiceArgs()
    loginArgs.setUsername(userName)
    loginArgs.setPassword(password)
    loginArgs.setHost(host)
    loginArgs.setPort(8089)
    Service.connect(loginArgs)
  }

  def executeSavedSearch(appName: String, savedSearchName: String): Results = {
    val savedSearch = getSavedSearches(appName).get(savedSearchName)

    val job = savedSearch.dispatch()

    while (!job.isDone) {
      Thread.sleep(500)
    }
    val inputStream: InputStream = job.getEvents
    SplunkConnection.parseInput(inputStream)
  }

  def getSavedSearches(appName: String): SavedSearchCollection = {
    val serviceArgs = new ServiceArgs
    serviceArgs.setApp(appName)

    val savedSearches: SavedSearchCollection = service.getSavedSearches(serviceArgs)
    savedSearches
  }

  def getApps: Iterable[Application] = {
    service.getApplications.values().asScala
  }

  def getUsers: Iterable[User] = {
    service.getUsers.values().asScala
  }

  def executeOneShotSearch(oneshotSearchQuery: String, startDate: Date, endDate: Date ): Results = {
    val oneshotSearchArgs = new com.splunk.Args()
    val formattedStartDate: String = SplunkConnection.splunkDateTimeFormat.format(startDate)
    oneshotSearchArgs.put("earliest_time", formattedStartDate)
    val formattedEndDate: String = SplunkConnection.splunkDateTimeFormat.format(endDate)
    oneshotSearchArgs.put("latest_time",   formattedEndDate)
    def results_oneshot =  service.oneshotSearch(oneshotSearchQuery, oneshotSearchArgs)
    SplunkConnection.parseInput(results_oneshot)
  }
}
