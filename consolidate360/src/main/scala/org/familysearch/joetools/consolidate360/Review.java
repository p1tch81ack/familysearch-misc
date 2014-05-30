package org.familysearch.joetools.consolidate360;

import org.familysearch.joetools.simpledb.FieldMap;

public class Review {
    private String iterationName;
    private String reviewerName;
    private String revieweeName;
    private String ratingName;
    private String commentName;
    private Object reviewValue;

/*
    public void $init$() {
    }
    */

    public Review(String iterationName, String reviewerName, String revieweeName, String ratingName, Integer reviewValue){
        this.iterationName = iterationName;
        this.reviewerName = reviewerName;
        this.revieweeName = revieweeName;
        this.ratingName = ratingName;
        this.reviewValue = reviewValue;
    }

    public Review(String iterationName, String reviewerName, String revieweeName, String commentName, String reviewValue){
        this.iterationName = iterationName;
        this.reviewerName = reviewerName;
        this.revieweeName = revieweeName;
        this.commentName = commentName;
        this.reviewValue = reviewValue;
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

    public String getRatingName() {
        return ratingName;
    }

    public String getCommentName() {
        return commentName;
    }

    public Object getReviewValue() {
        return reviewValue;
    }

    static FieldMap<Review> fieldMap = new FieldMap<Review>() {

        public Object get(Review instance, String fieldName) {
            if(fieldName.equals(SpecifierType.IterationName.name())){
                return instance.getIterationName();
            } else if(fieldName.equals(SpecifierType.ReviewerName.name())){
                return instance.getReviewerName();
            } else if(fieldName.equals(SpecifierType.RevieweeName.name())){
                return instance.getRevieweeName();
            } else if(fieldName.equals(SpecifierType.RatingName.name())){
                return instance.getRatingName();
            } else if(fieldName.equals(SpecifierType.CommentName.name())){
                return instance.getCommentName();
            } else {
                return null;
            }
        }

        public String[] fieldNamesArray() {
            return new String[]{SpecifierType.IterationName.name(), SpecifierType.ReviewerName.name(), SpecifierType.RevieweeName.name(), SpecifierType.RatingName.name(), SpecifierType.CommentName.name()};
        }
    };

}
