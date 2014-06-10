package org.familysearch.joetools.consolidate360;

public class Review {
    private String iterationName;
    private String reviewerName;
    private String revieweeName;
    private String entryName;
    private Object entryValue;

/*
    public void $init$() {
    }
    */

    public Review(String iterationName, String reviewerName, String revieweeName, String entryName, Object entryValue){
        this.iterationName = iterationName;
        this.reviewerName = reviewerName;
        this.revieweeName = revieweeName;
        this.entryName = entryName;
        this.entryValue = entryValue;
    }

    public String getIterationName() {
        return iterationName;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public String getRevieweeName() {
        return revieweeName;
    }

    public String getEntryName() {
        return entryName;
    }

    public Object getEntryValue() {
        return entryValue;
    }
}
