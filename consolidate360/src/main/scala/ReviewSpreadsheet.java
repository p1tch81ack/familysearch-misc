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
        List<String> titles = new LinkedList<String>();
        List<Boolean> isRatingSettings = new LinkedList<Boolean>();
        List<String> revieweeNames = new LinkedList<String>();

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
            titles.add(title);
            boolean isRating = false;
            String reason = null;
            for(int i=0; i<revieweeNames.size(); i++){
                Cell valueCell = readCell(sheet, i + 1, row);
                String value= valueCell.getContents();
                if(value.equals("NA")){
                    isRating = true;
                    reason = "NA";
                    break;
                } else if(hasDataValidation(valueCell)){
                    isRating = true;
                    reason = "data validation";
                    break;
                } else {
                    try{
                        new Integer(value);
                        reason = "integer";
                        isRating = true;
                        break;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            if(verbose){
                if(isRating){
                    System.out.println("Row " + row + " is a rating (" + reason + ").");
                } else {
                    System.out.println("Row " + row + " is not a rating.");
                }
            }
            isRatingSettings.add(isRating);
            row++;
            titleCell = readCell(sheet, 0, row);
        }

        column = 1;
        for(String revieweeName:revieweeNames){
            if(verbose){
                System.out.println("Reviewee: " + revieweeName);
            }
            for(int i=0; i<titles.size(); i++){
                if(isRatingSettings.get(i)){
                    int reviewValue = safelyReadIntegerCell(sheet, column, i + 1);
                    if(reviewValue>0){
                        Review review = new Review(iterationName, reviewerName, revieweeName, titles.get(i), reviewValue);
                        reviews.add(review);
                    }
                } else {
                    String reviewValue = safelyReadStringCell(sheet, column, i + 1);
                    if(reviewValue!=null && reviewValue.trim().length()>0) {
                        Review review = new Review(iterationName, reviewerName, revieweeName, titles.get(i), reviewValue);
                        reviews.add(review);
                    }
                }
            }
            column++;
        }
    }


    public List<Review> getReviews() {
        return reviews;
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
                return true;
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
