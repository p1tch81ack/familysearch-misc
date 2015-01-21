package org.familysearch.joetools.versiononeteamdata

import java.util.Date
import scala.collection.JavaConversions._
import org.familysearch.joetools.simpledb.SimpleTable
import com.versionone.om._

object TaskRow {
  val TASK_ID = "taskID"
  val TASK_NAME = "taskName"
  val OWNER_ID = "ownerID"
  val OWNER_NAME = "ownerName"
  val ITERATION_ID = "iterationID"
  val ITERATION_NAME = "iterationName"
  val PARENT_NAME = "parentName"

  def getTaskRowsForProject(connection: V1Connection, projectName: String, beginDate: Date, endDate: Date): List[TaskRow] = {
    val project: Project = connection.getProjectByName(projectName)
    if (project != null) {
      var taskRows = List[TaskRow]()
      val filteredIterations = connection.getIterationsForProjectEndingBetween(project, beginDate, endDate)
      for (iteration <- filteredIterations) {
        val primaryworkItems: java.util.Collection[PrimaryWorkitem] = iteration.getPrimaryWorkitems(null)
        for (primaryWorkitem <- primaryworkItems) {
          val secondaryWorkItems = primaryWorkitem.getSecondaryWorkitems(null)
          for (secondaryWorkItem <- secondaryWorkItems) {
            val owners = secondaryWorkItem.getOwners
            var ownerCount: Int = 0
            for (owner <- owners) {
              taskRows = taskRows.::(new TaskRow(secondaryWorkItem, iteration, owner))
              ownerCount = ownerCount + 1
            }
            if (secondaryWorkItem.isClosed) {
            } else {
            }
          }
        }
      }
      taskRows
    } else {
      null
    }
  }

  def getTaskRowsForTeam(connection: V1Connection, teamName: String, beginDate: Date, endDate: Date): List[TaskRow] = {
    val team = connection.getTeamByName(teamName)
    if (team != null) {
      var taskRows = List[TaskRow]()
      val primaryWorkItems = team.getPrimaryWorkitems(null)
      for (primaryWorkitem <- primaryWorkItems) {
        val iteration = primaryWorkitem.getIteration
        if (!(iteration eq null)) {
          if (((beginDate eq null) || iteration.getEndDate.getValue.compareTo(beginDate) >= 0) && ((endDate eq null) || iteration.getEndDate.getValue.compareTo(endDate) <= 0)) {
            val secondaryWorkItems = primaryWorkitem.getSecondaryWorkitems(null)
            for (secondaryWorkItem <- secondaryWorkItems) {
              val owners = secondaryWorkItem.getOwners
              var ownerCount: Int = 0
              for (owner <- owners) {
                taskRows = taskRows.::(new TaskRow(secondaryWorkItem, iteration, owner))
                ownerCount = ownerCount + 1
              }
            }
          }
        }
      }
      taskRows
    } else {
      null
    }
  }
}

class TaskRow(secondaryWorkItem: SecondaryWorkitem, iteration: Iteration, owner: Member) {
//  val secondaryWorkItemID = secondaryWorkItem.getID
//  val secondaryWorkItemDetailEstimate = secondaryWorkItem.getDetailEstimate
//  val secondaryWorkItemIsClosed = secondaryWorkItem.isClosed


//  val secondaryWorkItemName = secondaryWorkItem.getName

  val taskID: String = secondaryWorkItem.getDisplayID

  val taskName: String = secondaryWorkItem.getName

  val ownerID: String = owner.getID.toString

  val ownerName: String = owner.getName

  val iterationID: String = iteration.getID.toString

  val iterationName: String = iteration.getName

  val isClosed = secondaryWorkItem.isClosed

  val detailEstimate = secondaryWorkItem.getDetailEstimate

  val status = secondaryWorkItem match {
    case task: Task => {
      task.getStatus.getCurrentValue
    }
    case _ => null
  }

  val parentName = {
    val parent = secondaryWorkItem.getParent
    if(parent!=null) {parent.getName}
    else null
  }

}
