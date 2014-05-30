package org.familysearch.joetools.versiononeteamdata

import org.apache.poi.ss.usermodel._

object SpreadsheetTools {
  def createComment(creationHelper: CreationHelper, drawingPatriarch: Drawing, text: String): Comment = {
    val lines = text.split('\n')
    var maxWidth=0
    for (line<-lines){
      if(line.size>maxWidth){
        maxWidth=line.size
      }
    }
    val anchor: ClientAnchor = creationHelper.createClientAnchor()
    anchor.setCol1(0)
    anchor.setCol2(maxWidth/13+1)
    anchor.setRow1(0)
    anchor.setRow2(1+(lines.size*4)/3)
    //    val anchor: ClientAnchor = new HSSFClientAnchor(0, 0, 0, 0, /*(short)*/4, 2, /*(short)*/6, 5)
    //    val anchor: ClientAnchor = new HSSFClientAnchor()
    val comment: org.apache.poi.ss.usermodel.Comment = drawingPatriarch.createCellComment(anchor)
    val str: RichTextString  = creationHelper.createRichTextString(text)
    comment.setString(str)
    comment
  }

  val A2Z =
    List('A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S' ,'T','U','V','W','X','Y','Z')

  /**
   * returns the excel cell number (eg. C11, E4, AD1305 etc.) for this cell.
   */
  def getCellRefString(cellNum: Int, rowNum: Int): String = {
    val retval = new StringBuffer()
    var tempcellnum = cellNum
    do {
      retval.insert(0, A2Z(tempcellnum%26))
      tempcellnum = (tempcellnum / 26) - 1
    } while (tempcellnum >= 0)
    retval.append(rowNum+1)
    retval.toString
  }


}
