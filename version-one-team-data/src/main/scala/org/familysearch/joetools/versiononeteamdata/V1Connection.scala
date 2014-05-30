package org.familysearch.joetools.versiononeteamdata

import com.versionone.om._
import com.versionone.om.filters._
import java.util
import scala.collection.JavaConversions._
import util.Date
import scala.List

class V1Connection(v1Url: String, username: String, password: String) {
  val v1Instance = new V1Instance( v1Url, username, password )
  val v1InstanceGetter = v1Instance.get()
  v1Instance.validate()

  def getTeamByName(name: String): Team = {
    val teamNameFilter = new TeamFilter
    teamNameFilter.name.add(name)
    var matchingTeam: Team = null
    val teams =v1InstanceGetter.teams(teamNameFilter)
    if(teams.size>0){
      matchingTeam = teams.head
    }
    println("looked for team name: " + name)
    if(matchingTeam!=null){
      println("found team with id " + matchingTeam.getID)
      println("found team with url " + matchingTeam.getURL)
      println("found team with name " + matchingTeam.getName)
    } else {
      println("team was not found")
    }
    matchingTeam
  }

  def getProjectByName(name: String): Project = {
    val projectNameFilter = new ProjectFilter
    projectNameFilter.name.add(name)
    var matchingProject: Project = null
    val projects =v1InstanceGetter.projects(projectNameFilter)
    if(projects.size>0){
      matchingProject = projects.head
    }
    println("looked for project name: " + name)
    if(matchingProject!=null){
      println("found project with id " + matchingProject.getID)
      println("found project with url " + matchingProject.getURL)
      println("found project with name " + matchingProject.getName)
    } else {
      println("project was not found")
    }
    matchingProject
  }


  def getIterationsForProjectEndingBetween(project: Project, start: Date, end: Date): scala.List[Iteration] = {
    val projectIterations: java.util.Collection[Iteration] = project.getIterations(null)
    getIterationsEndingBetween(projectIterations, start, end)
  }

  private def getIterationsEndingBetween(iterations: util.Collection[Iteration], start: Date, end: Date): scala.List[Iteration] = {
    var scalaList = List[Iteration]()
    for (iteration <- iterations){
      scalaList = scalaList :+ iteration
    }
    scalaList filter (candidateIteration => {((start eq null) || candidateIteration.getEndDate.getValue.compareTo(start) >= 0) && ( (end eq null) || candidateIteration.getEndDate.getValue.compareTo(end) <= 0) })
  }

  /*
  def testDirect() {
    val httpClient = new URLConnectionHTTPClient
    val response = httpClient.request(v1Url+"meta.v1/Member")
    val code = response.responseCode
    println("Response was: " + code)
    if (code==200){
      val inputStream = response.inputStream
      var c = inputStream.read()
      while(c> -1){
        print(c.asInstanceOf[Char])
        c=inputStream.read()
      }
      println()
    } else if (code==302){
      val location = response.headerFields("Location")
      println("Location is " + location)
    } else {
      println("Headers: ")
      for (headerKey <- response.headerFields.keySet()) {println(headerKey + ": " + response.headerFields(headerKey))}
    }
  }
*/
}
