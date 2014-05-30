package org.familysearch.joetools.versiononeteamdata

import java.util.Date
import scala.collection.JavaConversions._
import org.familysearch.joetools.simpledb.{RowIndexEntry, SimpleRow, SimpleTable}
import com.versionone.om._

object TaskRow {
  val TASK_ID = "TaskID"
  val TASK_NAME = "TaskName"
  val OWNER_ID = "OwnerID"
  val OWNER_NAME = "OwnerName"
  val ITERATION_ID = "IterationID"
  val ITERATION_NAME = "IterationName"

  def getTaskRowsForProject(connection: V1Connection, projectName: String, beginDate: Date, endDate: Date): SimpleTable[TaskRow] = {
    val project: Project = connection.getProjectByName(projectName)
    if (project!=null){
      val simpleTable = new SimpleTable[TaskRow]
      val filteredIterations = connection.getIterationsForProjectEndingBetween(project, beginDate, endDate)
      for (iteration<-filteredIterations){
        val primaryworkItems: java.util.Collection[PrimaryWorkitem] = iteration.getPrimaryWorkitems(null)
        for (primaryWorkitem <- primaryworkItems){
          val secondaryWorkItems = primaryWorkitem.getSecondaryWorkitems(null)
          for (secondaryWorkItem <- secondaryWorkItems) {
            val owners = secondaryWorkItem.getOwners
            var ownerCount:Int = 0
            for(owner <- owners){
              simpleTable.addRow(new TaskRow(secondaryWorkItem, iteration, owner))
              ownerCount = ownerCount + 1
            }
            if (secondaryWorkItem.isClosed){
            } else {
            }
          }
        }
      }
      simpleTable
    } else {
      null
    }
  }

  def getTaskRowsForTeam(connection: V1Connection, teamName: String, beginDate: Date, endDate: Date): SimpleTable[TaskRow] = {
    val team = connection.getTeamByName(teamName)
    if (team!=null){
      val simpleTable = new SimpleTable[TaskRow]
      val primaryWorkItems = team.getPrimaryWorkitems(null)
      for (primaryWorkitem <- primaryWorkItems){
        val iteration = primaryWorkitem.getIteration
        if (!(iteration eq null)){
          if (((beginDate eq null) || iteration.getEndDate.getValue.compareTo(beginDate) >= 0) && ( (endDate eq null) || iteration.getEndDate.getValue.compareTo(endDate) <= 0)) {
            val secondaryWorkItems = primaryWorkitem.getSecondaryWorkitems(null)
            for (secondaryWorkItem <- secondaryWorkItems) {
              val owners = secondaryWorkItem.getOwners
              var ownerCount:Int = 0
              for(owner <- owners){
                simpleTable.addRow(new TaskRow(secondaryWorkItem, iteration, owner))
                ownerCount = ownerCount + 1
              }
            }
          }
        }
      }
      simpleTable
    } else {
      null
    }
  }

  def dumpSimpleTable(taskRowSimpleTable: SimpleTable[TaskRow]){
    val rowMap = taskRowSimpleTable.getMapOfMatchingRows(null)
    for (rowIndex <- rowMap.keySet()){
      var first=true
      for(tag <- rowIndex.getSpecifierTags){
        if (!first){
          print (", ")
        } else {
          first = false
        }
        print(tag + ": " + rowIndex.getSpecifierValue(tag))
      }
      println()
    }
  }

}

class TaskRow(secondaryWorkItem: SecondaryWorkitem, iteration: Iteration, owner: Member) extends SimpleRow {
  val secondaryWorkItemID = secondaryWorkItem.getID
  val secondaryWorkItemDetailEstimate = secondaryWorkItem.getDetailEstimate
  val secondaryWorkItemIsClosed = secondaryWorkItem.isClosed
  val secondaryWorkItemStatus = secondaryWorkItem match {
    case task: Task => task.getStatus.getCurrentValue
    case _ => null
  }
  val secondaryWorkItemName = secondaryWorkItem.getName
  override def getRowIndexEntry: RowIndexEntry = {
    val rowIndexEntry = new RowIndexEntry().
      add(TaskRow.TASK_ID, secondaryWorkItem.getDisplayID).
      add(TaskRow.TASK_NAME, secondaryWorkItem.getName).
      add(TaskRow.OWNER_ID, owner.getID.toString).
      add(TaskRow.OWNER_NAME, owner.getName).
      add(TaskRow.ITERATION_ID, iteration.getID.toString).
      add(TaskRow.ITERATION_NAME, iteration.getName)
    rowIndexEntry
  }

}
