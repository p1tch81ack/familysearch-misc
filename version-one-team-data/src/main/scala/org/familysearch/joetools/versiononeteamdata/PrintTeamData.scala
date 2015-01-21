package org.familysearch.joetools.versiononeteamdata

import java.io.FileOutputStream
import java.util.Date
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel._
import org.familysearch.joetools.simpledb.{RowSpecifier, SimpleTable}
//import scala.collection.JavaConversions._

/*
_drawing = (HSSFPatriarch) _sheet.createDrawingPatriarch();

Row row = _sheet.getRow(rowIndex_);
Cell cell = row.getCell(0);
CreationHelper factory = _workbook.getCreationHelper();

HSSFAnchor anchor = new HSSFClientAnchor(0, 0, 0, 0, (short)4, 2, (short)6, 5);
        org.apache.poi.ss.usermodel.Comment comment = _drawing.createComment(anchor);
RichTextString str = factory.createRichTextString("Hello, World "+rowIndex_);
comment.setString(str);

  cell.setCellComment(comment);
 */

object PrintTeamData {
  def generateSpreadsheet(connection: V1Connection, outputFileName: String, projectNames: List[String], teamNames: List[String], beginDate: Date, endDate: Date) {
    val wb: Workbook = new HSSFWorkbook()
    val creationHelper: CreationHelper = wb.getCreationHelper
    val dataFormat = wb.createDataFormat()
    val fixedTwoStyle = wb.createCellStyle()
    fixedTwoStyle.setDataFormat(dataFormat.getFormat("0.00"))

    for (projectName <- projectNames){
        val taskRowsForProject = TaskRow.getTaskRowsForProject(connection, projectName, beginDate, endDate)
        if (!generateSheet(connection, wb, creationHelper, fixedTwoStyle, projectName, taskRowsForProject, beginDate, endDate)){
          println("Project " + projectName + " was not found.")
        }
    }

    for (teamName <- teamNames){
      val taskRowsForTeam = TaskRow.getTaskRowsForTeam(connection, teamName, beginDate, endDate)
      if (!generateSheet(connection, wb, creationHelper, fixedTwoStyle, teamName, taskRowsForTeam, beginDate, endDate)){
        println("Team " + teamName + " was not found.")
      }
    }

    val fileOut: FileOutputStream = new FileOutputStream(outputFileName)
    wb.write(fileOut)
    fileOut.close()
  }


  def generateSheet(connection: V1Connection, wb: Workbook, creationHelper: CreationHelper, fixedTwoStyle: CellStyle, tabTitle: String, taskRows: Iterable[TaskRow], beginDate: Date, endDate: Date): Boolean = {
    if (!(taskRows eq null)) {
      val simpleTable = new SimpleTable[TaskRow](taskRows)
      val sheet = wb.createSheet(tabTitle)
      val drawingPatriarch: Drawing = sheet.createDrawingPatriarch()
      val row0 = sheet.createRow(0)
      val cell00 = row0.createCell(0)
      cell00.setCellValue("Iteration")
      sheet.setColumnWidth(0, 30 * 250)
      val ownerNames = simpleTable.getSpecifierValues(TaskRow.OWNER_NAME)
      var colPos = 1
      for (ownerName <- ownerNames) {
        val cellP0 = row0.createCell(colPos)
        cellP0.setCellValue(ownerName.toString)
        sheet.setColumnWidth(colPos, ownerName.toString.size * 250)
        colPos = colPos + 1
      }
      val colCount = colPos
      val colT0 = row0.createCell(colPos)
      colT0.setCellValue("Total")
      colPos = colPos + 1
      val colA0 = row0.createCell(colPos)
      colA0.setCellValue("Average")
      colPos = colPos + 1
      val iterationNames = simpleTable.getSpecifierValues(TaskRow.ITERATION_NAME)
      var maxWidth = "Iteration".size
      var rowPos = 1
      for (iterationName <- iterationNames) {
        val rowP = sheet.createRow(rowPos)
        val cell0P = rowP.createCell(0)
        cell0P.setCellValue(iterationName.toString)
        if (iterationName.toString.size > maxWidth) {
          maxWidth = iterationName.toString.size
        }
        var colPos = 1
        for (ownerName <- ownerNames) {
          val matchSpecifier = new RowSpecifier().`with`(TaskRow.ITERATION_NAME, iterationName).`with`(TaskRow.OWNER_NAME, ownerName)
          var commentText: String = ""
          val rows = simpleTable.getMatchingRows(matchSpecifier)
          var total = 0.0
          for (row <- rows) {
            if (commentText.size > 0) {
              commentText += "\n"
            }
            commentText = commentText + "ID: " + row.taskID + "  Estimate: " + row.detailEstimate
            if (row.isClosed) {
              commentText = commentText + "  Closed"
//              val secondaryWorkItem: SecondaryWorkitem = row.secondaryWorkItem
                commentText = commentText + "  Status: " + row.status
                println( "Status: " + row.status)
                if( !(row.status eq null) && row.status == "Completed"){
                  println("adding estimate")
                  val detailEstimate: java.lang.Double = row.detailEstimate
                  if (!(detailEstimate eq null)) {
                    total = total.doubleValue() + detailEstimate.doubleValue()
                  }
                }
            } else {
              commentText = commentText + "  Open"
            }
            commentText = commentText + "  Name: " + row.taskName
          }
          val cellCR = rowP.createCell(colPos)
          if(total.doubleValue() >0.0 || commentText.size>0){
            cellCR.setCellValue(total.doubleValue())
          }
          if (commentText.size > 0) {
            val comment = SpreadsheetTools.createComment(creationHelper, drawingPatriarch, commentText)
            cellCR.setCellComment(comment)
          }
          colPos = colPos + 1
        }
        val cellTP = rowP.createCell(colPos)
        cellTP.setCellFormula("sum(" + SpreadsheetTools.getCellRefString(1, rowPos) + ":" + SpreadsheetTools.getCellRefString(colCount - 1, rowPos) + ")")
        colPos = colPos + 1
        val cellAP = rowP.createCell(colPos)
        cellAP.setCellFormula("average(" + SpreadsheetTools.getCellRefString(1, rowPos) + ":" + SpreadsheetTools.getCellRefString(colCount - 1, rowPos) + ")")
        cellAP.setCellStyle(fixedTwoStyle)
        colPos = colPos + 1
        rowPos = rowPos + 1
      }
      val rowCount = rowPos
      val rowT = sheet.createRow(rowPos)
      colPos = 0
      val cell0T = rowT.createCell(0)
      cell0T.setCellValue("Total")
      colPos = colPos + 1
      for (ownerName <- ownerNames) {
        val cellPT = rowT.createCell(colPos)
        cellPT.setCellFormula("sum(" + SpreadsheetTools.getCellRefString(colPos, 1) + ":" + SpreadsheetTools.getCellRefString(colPos, rowCount - 1) + ")")
        colPos = colPos + 1
      }
      rowPos = rowPos + 1
      val rowA = sheet.createRow(rowPos)
      colPos = 0
      val cell0A = rowA.createCell(0)
      cell0A.setCellValue("Average")
      colPos = colPos + 1
      for (ownerName <- ownerNames) {
        val cellPA = rowA.createCell(colPos)
        cellPA.setCellFormula("average(" + SpreadsheetTools.getCellRefString(colPos, 1) + ":" + SpreadsheetTools.getCellRefString(colPos, rowCount - 1) + ")")

        cellPA.setCellStyle(fixedTwoStyle)
        colPos = colPos + 1
      }
      rowPos = rowPos + 1

      sheet.setColumnWidth(0, maxWidth * 250)
      true
    } else {
      false
    }
  }


}
