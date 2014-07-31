package org.familysearch.joetools.consolidate360;

import jxl.*;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ReviewSpreadsheet {
    List<Review> reviews;
    int columnShift;
    int rowShift;
    boolean flipAxis;
    boolean verbose;
    List<String> ratingTitles;
    List<String> commentTitles;

    public ReviewSpreadsheet(File spreadsheetFile, int columnShift, int rowShift, boolean flipAxis, boolean verbose, String iterationName, String reviewerName) throws Exception {
        reviews = new LinkedList<Review>();
        this.columnShift = columnShift;
        this.rowShift = rowShift;
        this.flipAxis = flipAxis;
        this.verbose = verbose;
        
        WorkbookSettings workbookSettings = new WorkbookSettings();
        workbookSettings.setSuppressWarnings(true);
        Workbook inputWorkbook = Workbook.getWorkbook(spreadsheetFile, workbookSettings);
        Sheet sheet = inputWorkbook.getSheet(0);
        List<String> revieweeNames = new LinkedList<String>();
        ratingTitles = new LinkedList<String>();
        commentTitles = new LinkedList<String>();

        int column = 1;
        Cell nameCell = readCell(sheet, column, 0);
        while(nameCell!=null && !nameCell.getContents().equals("")){
            revieweeNames.add(nameCell.getContents());
            column++;
            nameCell = readCell(sheet, column, 0);
        }

        int row = 1;
        Cell titleCell = readCell(sheet, 0, row);
        while(titleCell!=null && !titleCell.getContents().equals("")){
            String title = titleCell.getContents();
            boolean isRating = false;
            String reason = null;
            Integer reasonColumn = null;
            for(int i=0; i<revieweeNames.size(); i++){
                Cell valueCell = readCell(sheet, i + 1, row);
                String value= valueCell.getContents();
                if(value.equals("NA")){
                    isRating = true;
                    reason = "NA";
                    reasonColumn = i+1;
                    break;
                } else if(hasDataValidation(valueCell)){
                    isRating = true;
                    reason = "data validation";
                    reasonColumn = i+1;
                    break;
                } else {
                    try{
                        new Integer(value);
                        reason = "integer";
                        isRating = true;
                        reasonColumn = i+1;
                        break;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            if(verbose){
                if(isRating){
                    System.out.println("Row " + row + " (" + title + ") is a rating (" + reason + " in column " + reasonColumn + ").");
                } else {
                    System.out.println("Row " + row + " (" + title + ") is not a rating.");
                }
            }
            if(isRating){
                ratingTitles.add(title);
            } else {
                commentTitles.add(title);
            }
            row++;
            titleCell = readCell(sheet, 0, row);
        }

        column = 1;
        for(String revieweeName:revieweeNames){
            if(verbose){
                System.out.println("Reviewee: " + revieweeName);
            }
            if(revieweeName.equals(reviewerName)){
                System.out.println("Skipping " + revieweeName + " because they are the reviewer (" + reviewerName + ").");
            } else {
                row = 1;
                for(String ratingTitle: ratingTitles){
                    int reviewValue = safelyReadIntegerCell(sheet, column, row);
                    if(reviewValue>0){
                        Review review = new Review(iterationName, reviewerName, revieweeName, ratingTitle, reviewValue);
                        reviews.add(review);
                    }
                    row++;
                }
                for(String commentTitle: commentTitles) {
                    String reviewValue = safelyReadStringCell(sheet, column, row);
                    if(reviewValue!=null && reviewValue.trim().length()>0) {
                        Review review = new Review(iterationName, reviewerName, revieweeName, commentTitle, reviewValue);
                        reviews.add(review);
                    }
                    row++;
                }
            }
            column++;
        }
    }


    public List<Review> getReviews() {
        return reviews;
    }

    public List<String> getCommentTitles() {
        return commentTitles;
    }

    public List<String> getRatingTitles() {
        return ratingTitles;
    }

    private String safelyReadStringCell(Sheet sheet, int column, int row){
        Cell cell = readCell(sheet, column, row);
//        hasDataValidation(cell);
        if (cell!=null){
            String contents = cell.getContents();
            if(contents!=null && contents.equals("")){
                return null;
            } else {
                return contents;
            }
        }
        return null;
    }

    private boolean hasDataValidation(Cell cell){
        CellFeatures cellFeatures = cell.getCellFeatures();
        String validationList = null;
        if(cellFeatures!=null){
            try{
                validationList = cellFeatures.getDataValidationList();

            } catch (NullPointerException ignored){
            }
            if(validationList!=null){
                if(verbose){
                    System.out.println("Data Validation (" + cell.getColumn() + ", " + cell.getRow() + "): " + validationList);
                }
                if(!validationList.contains("any")) {
                    return true;
                }
            }
        }
        return false;
    }

    private int safelyReadIntegerCell(Sheet sheet, int column, int row){
        String contents = safelyReadStringCell(sheet, column, row);
        if(contents!=null){
            int colonPos = contents.indexOf(':');
            if(colonPos>=0){
                contents = contents.substring(0, colonPos);
            }
            try{
                return new Integer(contents);
            } catch (NumberFormatException e){
                return 0;
            }
        } else {
            return 0;
        }
    }

    private Cell readCell(Sheet sheet, int column, int row){
        if(flipAxis){
            if(verbose){
                System.out.println("Reading " + (columnShift+row) + ", " + (rowShift+column));
            }
            if(sheet.getColumns()>(columnShift+row) && sheet.getRows()>(rowShift+column)){
                return sheet.getCell(columnShift+row, rowShift+column);
            } else {
                return null;
            }
        } else {
            if(verbose){
                System.out.println("Reading " + (columnShift+column) + ", " + (rowShift+row));
            }
            if(sheet.getColumns()>(columnShift+column) && sheet.getRows()>(rowShift+row)){
                return sheet.getCell(columnShift+column, rowShift+row);
            } else {
                return null;
            }
        }
    }
}
