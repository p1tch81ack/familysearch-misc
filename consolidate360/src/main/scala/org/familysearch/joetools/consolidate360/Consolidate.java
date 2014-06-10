package org.familysearch.joetools.consolidate360;

import jxl.Workbook;
import jxl.format.UnderlineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.*;
import jxl.write.Number;

import java.io.File;
import java.util.*;

import org.familysearch.joetools.simpledb.RowSpecifier;
import scala.Tuple2;
import scala.collection.immutable.Map;
import scala.collection.immutable.HashMap;


public class Consolidate {
//    public static final int STARTING_COLUMN = 1;
    private File inputDirectory;
    private ReviewStore reviewStore;
    private Set<String> ratingTitles;
    private Set<String> commentTitles;
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

        List<Review> reviews = new LinkedList<Review>();
        ratingTitles = new TreeSet<String>();
        commentTitles = new TreeSet<String>();
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
                reviews.addAll(reviewSpreadsheet.getReviews());
                for(String ratingTitle: ratingTitles){
                    if(reviewSpreadsheet.commentTitles.contains(ratingTitle)){
                        System.out.println("Warning!  reviewSpreadsheet has " + ratingTitle + " as a comment title, but previous spreadsheets had it as a rating title.");
                    }
                }
                ratingTitles.addAll(reviewSpreadsheet.getRatingTitles());
                for(String commentTitle: commentTitles){
                    if(reviewSpreadsheet.ratingTitles.contains(commentTitle)){
                        System.out.println("Warning!  reviewSpreadsheet has " + commentTitle + " as a rating title, but previous spreadsheets had it as a comment title.");
                    }
                }
                commentTitles.addAll(reviewSpreadsheet.getCommentTitles());
            }
            reviewStore = new ReviewStore(reviews);
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

        Map<String, Object> tagsAndValues = with(new HashMap<String, Object>(), SpecifierType.revieweeName.toString(), revieweeName);
        //Set<Object> columnTitles = reviewStore.getSpecifierNamesForMatchingReviews(RowSpecifier.apply(tagsAndValues), SpecifierType.entryName);
        Set<Object> rowNames = reviewStore.getSpecifierNamesForMatchingReviews(RowSpecifier.apply(tagsAndValues), SpecifierType.revieweeName);
        int nextColumn = populateSheetForRowSpecColumnSpecAndReviewSpecifier(
                sheet,
                0,
                row,
                ratingTitles,
                SpecifierType.entryName,
                rowNames,
                SpecifierType.iterationName,
                ConsolidationType.rating,
                tagsAndValues,
                true,
                true,
                nonAnonymous,
                true,
                nonAnonymous,
                11
        );
        int columnAfterTurnedInReview = addTurnedInReview(sheet, nextColumn, row, headerFormat, revieweeName);
        if(showComments){
            populateSheetForRowSpecColumnSpecAndReviewSpecifier(
                    sheet,
                    columnAfterTurnedInReview,
                    row,
                    commentTitles,
                    SpecifierType.entryName,
                    rowNames,
                    SpecifierType.iterationName,
                    ConsolidationType.comment,
                    tagsAndValues,
                    true,
                    false,
                    false,
                    false,
                    nonAnonymous,
                    30
            );
        }
    }

    private void populateSheetForIterationAndRevieweeWithRatingsAcrossAndReviewersDown(WritableSheet sheet, String iterationName, String revieweeName, boolean showComments, boolean nonAnonymous) throws Exception {
        int row = 0;
        sheet.addCell(new Label(0, row, revieweeName, nameFormat));
        row++;
        Map<String, Object> tagsAndValues = with(with(new HashMap<String, Object>(), SpecifierType.revieweeName.toString(), revieweeName),
                SpecifierType.iterationName.toString(),
                iterationName);
//        Set<Object> columnTitles = reviewStore.getSpecifierNamesForMatchingReviews(RowSpecifier.apply(tagsAndValues), SpecifierType.entryName);
        Set<Object> rowNames = reviewStore.getSpecifierNamesForMatchingReviews(RowSpecifier.apply(tagsAndValues), SpecifierType.revieweeName);
        int nextColumn = populateSheetForRowSpecColumnSpecAndReviewSpecifier(
                sheet,
                0,
                row,
                ratingTitles,
                SpecifierType.entryName,
                rowNames,
                SpecifierType.reviewerName,
                ConsolidationType.rating,
                tagsAndValues,
                true,
                true,
                nonAnonymous,
                true,
                nonAnonymous,
                11
            );
        if(showComments){
            populateSheetForRowSpecColumnSpecAndReviewSpecifier(
                    sheet,
                    nextColumn,
                    row,
                    commentTitles,
                    SpecifierType.entryName,
                    rowNames,
                    SpecifierType.reviewerName,
                    ConsolidationType.comment,
                    tagsAndValues,
                    true,
                    false,
                    false,
                    false,
                    nonAnonymous,
                    30
            );
        }
    }


    private void populateSheetForIterationWithAverageRatingsAcrossAndRevieweesDown(WritableSheet sheet, String iterationName, boolean showComments, boolean nonAnonymous) throws Exception {
        int row = 0;
        sheet.addCell(new Label(0, row, iterationName, nameFormat));
        row++;
        Map<String, Object> tagsAndValues = with(new HashMap<String, Object>(), SpecifierType.iterationName.toString(), iterationName);
//        Set<Object> columnTitles = reviewStore.getSpecifierNamesForMatchingReviews(RowSpecifier.apply(tagsAndValues), SpecifierType.entryName);
        Set<Object> rowNames = reviewStore.getSpecifierNamesForMatchingReviews(RowSpecifier.apply(tagsAndValues), SpecifierType.revieweeName);
        int nextColumn = populateSheetForRowSpecColumnSpecAndReviewSpecifier(
                sheet,
                0,
                row,
                ratingTitles,
                SpecifierType.entryName,
                rowNames,
                SpecifierType.revieweeName,
                ConsolidationType.rating,
                tagsAndValues,
                true,
                true,
                nonAnonymous,
                true,
                nonAnonymous,
                11
        );
        if(showComments){
            populateSheetForRowSpecColumnSpecAndReviewSpecifier(
                    sheet,
                    nextColumn,
                    row,
                    commentTitles,
                    SpecifierType.entryName,
                    rowNames,
                    SpecifierType.revieweeName,
                    ConsolidationType.comment,
                    tagsAndValues,
                    true,
                    false,
                    false,
                    false,
                    nonAnonymous,
                    30
            );
        }

    }


    private void populateSheetWithAverageRatingsAcrossAndRevieweesDown(WritableSheet sheet, boolean showComments, boolean nonAnonymous) throws Exception {
        int row = 0;
        HashMap<String, Object> tagsAndValues = new HashMap<String, Object>();
//        Set<Object> columnTitles = reviewStore.getSpecifierNamesForMatchingReviews(RowSpecifier.apply(tagsAndValues), SpecifierType.entryName);
        Set<Object> rowNames = reviewStore.getSpecifierNamesForMatchingReviews(RowSpecifier.apply(tagsAndValues), SpecifierType.revieweeName);

        int nextColumn = populateSheetForRowSpecColumnSpecAndReviewSpecifier(
                sheet,
                0,
                row,
                ratingTitles,
                SpecifierType.entryName,
                rowNames,
                SpecifierType.revieweeName,
                ConsolidationType.rating,
                tagsAndValues,
                true,
                true,
                nonAnonymous,
                true,
                nonAnonymous,
                11
        );
        if(showComments){
            populateSheetForRowSpecColumnSpecAndReviewSpecifier(
                    sheet,
                    nextColumn,
                    row,
                    commentTitles,
                    SpecifierType.entryName,
                    rowNames,
                    SpecifierType.revieweeName,
                    ConsolidationType.comment,
                    tagsAndValues,
                    true,
                    false,
                    false,
                    false,
                    nonAnonymous,
                    30
            );
        }

    }


    private int populateSheetForRowSpecColumnSpecAndReviewSpecifier(WritableSheet sheet,
                                                                    int startingColumn,
                                                                    int startingRow,
                                                                    Set<?> columnTitles,
                                                                    SpecifierType columSpecifierType,
                                                                    Set<?> rowNames,
                                                                    SpecifierType rowSpecifierType,
                                                                    ConsolidationType consolidationSpecifierType,
                                                                    Map<String, Object> tagsAndValues,
                                                                    boolean showColumnNames,
                                                                    boolean showColumnAverages,
                                                                    boolean showRowNames,
                                                                    boolean showRowAverages,
                                                                    boolean showReviewerNamesOnComments,
                                                                    int columnWidth) throws Exception {
        int column=startingColumn;
        int row = startingRow;
        /*
        Set<Object> columnTitles = reviewStore.getSpecifierNamesForMatchingReviews(RowSpecifier.apply(tagsAndValues), columSpecifierType);
        Set<Object> rowNames = reviewStore.getSpecifierNamesForMatchingReviews(RowSpecifier.apply(tagsAndValues), rowSpecifierType);
        */
        if(showColumnNames){
            sheet.addCell(new Label(column, row, "", headerFormat));
            column++; // we advance anyway so there is room on the left for things like average at the bottom left.
            for(Object ratingTitle: columnTitles){
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
            Map<String, Object> rowTagsAndValues = with(tagsAndValues, rowSpecifierType.toString(), rowName);
            int columnCount = 0;
            double columnTotal = 0.0;
            for(Object columnName: columnTitles){
                Map<String, Object> columnTagsAndValues = with(rowTagsAndValues, columSpecifierType.toString(), columnName);
                Object cellValue = reviewStore.getAverageRatingOrCombinedCommentsforMatchingReviews(columnTagsAndValues, consolidationSpecifierType, showReviewerNamesOnComments);
                if(cellValue instanceof Double){
                    columnTotal += (Double)cellValue;
                }
                addCellIfNotNullOrZero(sheet, column, row, cellValue);
                column++;
                columnCount++;
            }
            if(showRowAverages && columnCount>0){
                /*
                Object cellValue = reviewStore.getAverageRatingOrCombinedCommentsforMatchingReviews(rowRowSpecifier, consolidationSpecifierType, showReviewerNamesOnComments);
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
            for( Object columnName: columnTitles){
                int rowCount = 0;
                double rowTotal = 0.0;
                for( Object rowName: rowNames){
                    Map<String, Object> rowTagsAndValues = with(tagsAndValues, rowSpecifierType.toString(), rowName);
                    Map<String, Object> columnTagsAndValues = with(rowTagsAndValues, columSpecifierType.toString(), columnName);
                    Object cellValue = reviewStore.getAverageRatingOrCombinedCommentsforMatchingReviews(columnTagsAndValues, consolidationSpecifierType, showReviewerNamesOnComments);
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
                Object cellValue = reviewStore.getAverageRatingOrCombinedCommentsforMatchingReviews(rowSpecifier, consolidationSpecifierType, showReviewerNamesOnComments);
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

    Map<String, Object> with(Map<String, Object> tagsAndValues, String tag, Object value){
        return tagsAndValues.$plus(new Tuple2<String, Object>(tag, value));
    }
}
