package org.familysearch.joetools.consolidate360;

import jxl.Workbook;
import jxl.format.UnderlineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.*;
import jxl.write.Number;

import org.familysearch.joetools.simpledb.RowSpecifier;

import java.io.File;
import java.util.Collection;
import java.util.Set;


public class Consolidate {
//    public static final int STARTING_COLUMN = 1;
    private File inputDirectory;
    private final ReviewStore reviewStore = new ReviewStore();
    WritableCellFormat nameFormat;
    WritableCellFormat headerFormat;
    WritableCellFormat normalFormat;
    WritableCellFormat normalNumberFormat;

    public Consolidate(String inputDirectoryName, int columnShift, int rowShift, boolean invert, boolean verbose) throws Exception {
        populateFormats();

        inputDirectory = new File(inputDirectoryName);

        if(!inputDirectory.exists()){
            System.err.println(inputDirectoryName + " does not exist");
            System.exit(1);
        }

        if(!inputDirectory.isDirectory()){
            System.err.println(inputDirectoryName + " is not a directory");
            System.exit(1);
        }

        String[] iterationNames = inputDirectory.list();
        for(String iterationName:iterationNames){
            File iterationDirectory = new File(inputDirectory, iterationName);
            if(!iterationDirectory.isDirectory()){
                continue;
            }
            if(verbose) {
                System.out.println("Iteration Name: " + iterationName);
            }
            String[] reviewerFileNames = iterationDirectory.list();
            for(String reviewerFileName:reviewerFileNames){
                if(verbose){
                    System.out.println("Reviewer File Name: " + reviewerFileName);
                }
                String reviewerName = reviewerFileName;
                int periodPos = reviewerFileName.indexOf('.');
                if(periodPos>=0){
                    reviewerName = reviewerFileName.substring(0, periodPos);
                }
                if(verbose){
                    System.out.println("Reviewer: " + reviewerName);
                }
                File reviewerFile = new File(iterationDirectory, reviewerFileName);
                ReviewSpreadsheet reviewSpreadsheet = new ReviewSpreadsheet(reviewerFile, columnShift, rowShift, invert, verbose, iterationName, reviewerName);
                reviewStore.addAllReviews(reviewSpreadsheet.getReviews());
            }
        }
    }

    private void populateFormats() throws WriteException {
        nameFormat = new WritableCellFormat();
        WritableFont nameFont = new WritableFont(WritableFont.ARIAL, 18, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.BLACK);
        nameFormat.setFont(nameFont);

        headerFormat = new WritableCellFormat();
        headerFormat.setVerticalAlignment(VerticalAlignment.BOTTOM);
        WritableFont headerFont = new WritableFont(WritableFont.ARIAL, 9, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.BLACK);
        headerFormat.setFont(headerFont);
        headerFormat.setBackground(jxl.format.Colour.GREY_25_PERCENT);
        headerFormat.setWrap(true);

        normalFormat = new WritableCellFormat();
        normalFormat.setVerticalAlignment(VerticalAlignment.TOP);
        normalFormat.setWrap(true);

        normalNumberFormat = new WritableCellFormat(NumberFormats.FLOAT);
        normalNumberFormat.setVerticalAlignment(VerticalAlignment.TOP);
        normalNumberFormat.setWrap(true);
    }


    void generate(String outputFileName, String individualReportDirectoryName, boolean showCommentsOnIndividualReports, boolean nonAnonymous) throws Exception {
        WritableWorkbook workbook = null;
        if(outputFileName!=null){
            workbook = Workbook.createWorkbook(new File(outputFileName)); 
        }

        File individualReportDirectory = null;
        if(individualReportDirectoryName!=null){
            individualReportDirectory = new File(individualReportDirectoryName);
        }

        int nextSheet = 0;
        for(Object revieweeName: reviewStore.getRevieweeNames()){
            if(workbook!=null){
                WritableSheet sheet = workbook.createSheet(revieweeName.toString(), nextSheet);
                populateSheetForRevieweeWithAverageRatingsAcrossAndIterationsDown(sheet, revieweeName.toString(), true, true);
            }

            if(individualReportDirectory!=null){
                File individualReportFile = new File(individualReportDirectory, revieweeName + ".xls");
                WritableWorkbook individualWorkBook = Workbook.createWorkbook(individualReportFile);
                WritableSheet sheet = individualWorkBook.createSheet("Summary", 0);
                populateSheetForRevieweeWithAverageRatingsAcrossAndIterationsDown(sheet, revieweeName.toString(),
                        showCommentsOnIndividualReports, nonAnonymous);
                Collection<Object> iterationNames = reviewStore.getIterationNamesForReviewee(revieweeName.toString());
                for(Object iterationName:iterationNames){
                    WritableSheet iterationSheet = individualWorkBook.createSheet(iterationName.toString(), 0);
                    populateSheetForIterationAndRevieweeWithRatingsAcrossAndReviewersDown(iterationSheet, iterationName.toString(),
                            revieweeName.toString(), showCommentsOnIndividualReports, nonAnonymous);
                }
                individualWorkBook.write();
                individualWorkBook.close();
            }
            nextSheet++;
        }

        if(workbook!=null){
            Set<Object> iterationNames = reviewStore.getIterationNames();
            for(Object iterationName:iterationNames){
                WritableSheet summaryIterationSheet = workbook.createSheet(iterationName.toString(), 0);
                populateSheetForIterationWithAverageRatingsAcrossAndRevieweesDown(summaryIterationSheet, iterationName.toString(), true, true);
            }
            WritableSheet summaryIterationSheet = workbook.createSheet("Summary", 0);
            populateSheetWithAverageRatingsAcrossAndRevieweesDown(summaryIterationSheet, true, true);
            workbook.write();
            workbook.close();
        }
    }


    private void populateSheetForRevieweeWithAverageRatingsAcrossAndIterationsDown(WritableSheet sheet, String revieweeName, boolean showComments, boolean nonAnonymous) throws Exception {
        populateFormats();

        int row = 0;
        sheet.addCell(new Label(0, row, revieweeName, nameFormat));
        row++;

        int nextColumn = populateSheetForRowSpecColumnSpecAndReviewSpecifier(sheet, 0, row, SpecifierType.RatingName,
                SpecifierType.IterationName, SpecifierType.RatingName,
                with(new RowSpecifier(), SpecifierType.RevieweeName, revieweeName),
                true, true, nonAnonymous, true, nonAnonymous, 11);
        int columnAfterTurnedInReview = addTurnedInReview(sheet, nextColumn, row, headerFormat, revieweeName);
        if(showComments){
            populateSheetForRowSpecColumnSpecAndReviewSpecifier(sheet, columnAfterTurnedInReview, row, SpecifierType.CommentName,
                    SpecifierType.IterationName, SpecifierType.CommentName,
                    with(new RowSpecifier(), SpecifierType.RevieweeName, revieweeName),
                    true, false, false, false, nonAnonymous, 30);
        }
    }

    private void populateSheetForIterationAndRevieweeWithRatingsAcrossAndReviewersDown(WritableSheet sheet, String iterationName, String revieweeName, boolean showComments, boolean nonAnonymous) throws Exception {
        int row = 0;
        sheet.addCell(new Label(0, row, revieweeName, nameFormat));
        row++;
        int nextColumn = populateSheetForRowSpecColumnSpecAndReviewSpecifier(
                sheet,
                0,
                row,
                SpecifierType.RatingName,
                SpecifierType.ReviewerName,
                SpecifierType.RatingName,
                with( with(new RowSpecifier(), SpecifierType.RevieweeName, revieweeName),
                        SpecifierType.IterationName,
                        iterationName),
                true,
                true,
                nonAnonymous,
                true,
                nonAnonymous,
                11);
        if(showComments){
        populateSheetForRowSpecColumnSpecAndReviewSpecifier(sheet, nextColumn, row, SpecifierType.CommentName,
                SpecifierType.ReviewerName, SpecifierType.CommentName,
                with( with(new RowSpecifier(), SpecifierType.RevieweeName, revieweeName),
                        SpecifierType.IterationName,
                        iterationName),
                true,
                false,
                false,
                false,
                nonAnonymous,
                30);
        }
    }


    private void populateSheetForIterationWithAverageRatingsAcrossAndRevieweesDown(WritableSheet sheet, String iterationName, boolean showComments, boolean nonAnonymous) throws Exception {
        int row = 0;
        sheet.addCell(new Label(0, row, iterationName, nameFormat));
        row++;
        int nextColumn = populateSheetForRowSpecColumnSpecAndReviewSpecifier(sheet, 0, row, SpecifierType.RatingName,
                SpecifierType.RevieweeName, SpecifierType.RatingName,
                with(new RowSpecifier(), SpecifierType.IterationName, iterationName),
                true, true, nonAnonymous, true, nonAnonymous, 11);
        if(showComments){
            populateSheetForRowSpecColumnSpecAndReviewSpecifier(sheet, nextColumn, row, SpecifierType.CommentName,
                    SpecifierType.RevieweeName, SpecifierType.CommentName,
                    with(new RowSpecifier(), SpecifierType.IterationName, iterationName),
                    true, false, false, false, nonAnonymous, 30);
        }

    }


    private void populateSheetWithAverageRatingsAcrossAndRevieweesDown(WritableSheet sheet, boolean showComments, boolean nonAnonymous) throws Exception {
        int row = 0;
        int nextColumn = populateSheetForRowSpecColumnSpecAndReviewSpecifier(sheet, 0, row, SpecifierType.RatingName,
                SpecifierType.RevieweeName, SpecifierType.RatingName, new RowSpecifier(),
                true, true, nonAnonymous, true, nonAnonymous, 11);
        if(showComments){
            populateSheetForRowSpecColumnSpecAndReviewSpecifier(sheet, nextColumn, row, SpecifierType.CommentName,
                    SpecifierType.RevieweeName, SpecifierType.CommentName, new RowSpecifier(),
                    true, false, false, false, nonAnonymous, 30);
        }

    }


    private int populateSheetForRowSpecColumnSpecAndReviewSpecifier(WritableSheet sheet,
                                                                    int startingColumn,
                                                                    int startingRow,
                                                                    SpecifierType columSpecifierType,
                                                                    SpecifierType rowSpecifierType,
                                                                    SpecifierType consolidationSpecifierType,
                                                                    RowSpecifier rowSpecifier,
                                                                    boolean showColumnNames,
                                                                    boolean showColumnAverages,
                                                                    boolean showRowNames,
                                                                    boolean showRowAverages,
                                                                    boolean showReviewerNamesOnCommens,
                                                                    int columnWidth) throws Exception {
        int column=startingColumn;
        int row = startingRow;
        Set<Object> columnNames = reviewStore.getSpecifierNamesForMatchingReviews(rowSpecifier, columSpecifierType);
        Set<Object> rowNames = reviewStore.getSpecifierNamesForMatchingReviews(rowSpecifier, rowSpecifierType);
        if(showColumnNames){
            sheet.addCell(new Label(column, row, "", headerFormat));
            column++; // we advance anyway so there is room on the left for things like average at the bottom left.
            for(Object ratingTitle: columnNames){
                sheet.addCell(new Label(column, row, ratingTitle.toString(), headerFormat));
                sheet.setColumnView(column, columnWidth);
                column++;
            }
        }
        if(showRowAverages){
            sheet.addCell(new Label(column, row, "Average", headerFormat));
            sheet.setColumnView(column, columnWidth);
        }
        row++;

        for(Object rowName: rowNames){
            column = startingColumn;
            if(showRowNames) {
                sheet.addCell(new Label(column, row, rowName.toString(), headerFormat));
            } else {
                sheet.addCell(new Label(column, row, "", headerFormat));
            }
            column++; // we advance anyway so there is room on the left for things like average at the bottom left.
            RowSpecifier rowRowSpecifier = with(rowSpecifier, rowSpecifierType, rowName.toString());
            int columnCount = 0;
            double columnTotal = 0.0;
            for(Object columnName: columnNames){
                RowSpecifier columnRowSpecifier = with(rowRowSpecifier, columSpecifierType, columnName.toString());
                Object cellValue = reviewStore.getAverageRatingOrCombinedCommentsforMatchingReviews(columnRowSpecifier, consolidationSpecifierType, showReviewerNamesOnCommens);
                if(cellValue instanceof Double){
                    columnTotal += (Double)cellValue;
                }
                addCellIfNotNullOrZero(sheet, column, row, cellValue);
                column++;
                columnCount++;
            }
            if(showRowAverages && columnCount>0){
                /*
                Object cellValue = reviewStore.getAverageRatingOrCombinedCommentsforMatchingReviews(rowRowSpecifier, consolidationSpecifierType, showReviewerNamesOnCommens);
                addCellIfNotNullOrZero(sheet, column, row, cellValue);
                */
                addCellIfNotNullOrZero(sheet, column, row, columnTotal/((double)columnCount));
            }
            row++;
        }
        if(showColumnAverages){
            column = startingColumn;
            sheet.addCell(new Label(column, row, "Average", headerFormat));
            column++; // we advance anyway so there is room on the left for things like average at the bottom left.
            int totalCount = 0;
            double totalTotal = 0;
            for( Object columnName: columnNames){
                int rowCount = 0;
                double rowTotal = 0.0;
                for( Object rowName: rowNames){
                    RowSpecifier rowRowSpecifier = with(rowSpecifier, rowSpecifierType, rowName.toString());
                    RowSpecifier columnRowSpecifier = with(rowRowSpecifier, columSpecifierType, columnName.toString());
                    Object cellValue = reviewStore.getAverageRatingOrCombinedCommentsforMatchingReviews(columnRowSpecifier, consolidationSpecifierType, showReviewerNamesOnCommens);
                    if(cellValue instanceof Double){
                        rowTotal+=(Double)cellValue;
                        totalTotal +=(Double)cellValue;
                    }
                    rowCount++;
                    totalCount++;
                }
                if(rowCount>0){
                    addCellIfNotNullOrZero(sheet, column, row, rowTotal/((double)rowCount));
                }
                column++;
            }
            if(showRowAverages){
             /*
                Object cellValue = reviewStore.getAverageRatingOrCombinedCommentsforMatchingReviews(rowSpecifier, consolidationSpecifierType, showReviewerNamesOnCommens);
                addCellIfNotNullOrZero(sheet, column, row, cellValue);
                */
                if(totalCount>0){
                    addCellIfNotNullOrZero(sheet, column, row, totalTotal/((double)totalCount));
                }
            }
        }
        column++;
        return column;
    }

    private void addCellIfNotNullOrZero(WritableSheet sheet, int column, int row, Object cellValue, WritableCellFormat format, WritableCellFormat formatIfNumber) throws Exception {
        if(cellValue==null){
            addCellIfNotNull(sheet, column, row, (String) cellValue, format);
        } else {
            if(cellValue instanceof Double){
                addCellIfNotZero(sheet, column, row, (Double)cellValue, formatIfNumber);
            } else {
                addCellIfNotNull(sheet, column, row, cellValue.toString(), format);
            }
        }
    }

    private void addCellIfNotNullOrZero(WritableSheet sheet, int column, int row, Object cellValue) throws Exception {
        addCellIfNotNullOrZero(sheet, column, row, cellValue, normalFormat, normalNumberFormat);
    }

    private void addCellIfNotZero(WritableSheet sheet, int column, int row, double value, WritableCellFormat format) throws Exception {
        if(value!=0.0){
            sheet.addCell(new Number(column, row, value, format));
        }
    }

    private void addCellIfNotNull(WritableSheet sheet, int column, int row, String value, WritableCellFormat format) throws Exception {
        if(value!=null){
            sheet.addCell(new Label(column, row, value, format));
        }
    }

    private int addTurnedInReview(WritableSheet sheet, int column, int row, WritableCellFormat format, String revieweeName) throws WriteException {
        sheet.addCell(new Label(column, row, "Turned In Reveiw", format));
        row++;
        for(Object iterationName : reviewStore.getIterationNamesForReviewee(revieweeName)){
            sheet.addCell(new Label(0, row, iterationName.toString(), headerFormat));
            if(turnedInReview(iterationName.toString(), revieweeName)){
                sheet.addCell(new Label(column, row, "Yes", normalFormat));
            } else {
                sheet.addCell(new Label(column, row, "No", normalFormat));
            }
            row++;
        }
        return column + 1;
    }

    public boolean turnedInReview(String iterationName, String reviewerName) {
        File iterationDirectory = new File(inputDirectory, iterationName);
        if (!iterationDirectory.isDirectory()) {
            return false;
        }
        String[] reviewerFileNames = iterationDirectory.list();
        for (String reviewerFileName : reviewerFileNames) {
            String reviewer = reviewerFileName;
            int periodPos = reviewerFileName.indexOf('.');
            if (periodPos >= 0) {
                reviewer = reviewerFileName.substring(0, periodPos);
            }
            if (reviewer.equals(reviewerName)) {
                return true;
            }
        }
        return false;
    }

    RowSpecifier with(RowSpecifier rowSpecifier, SpecifierType specifierType, String value){
        RowSpecifier newSpecifier = rowSpecifier.with(specifierType.name(), value);
        if(specifierType.equals(SpecifierType.RevieweeName)){
            newSpecifier = newSpecifier.without(SpecifierType.ReviewerName.name(), value);
        }
        return newSpecifier;
    }

}
