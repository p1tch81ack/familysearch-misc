import org.familysearch.joetools.simpledb.RowIndexEntry;
import org.familysearch.joetools.simpledb.SimpleRow;

public class Review implements SimpleRow {
    private String iterationName;
    private String reviewerName;
    private String revieweeName;
    private String ratingName;
    private String commentName;

    private Object reviewValue;

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

    public void $init$() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public RowIndexEntry getRowIndexEntry(){
        return new RowIndexEntry(SpecifierType.IterationName.name(), iterationName).
                add(SpecifierType.ReviewerName.name(), reviewerName).
                add(SpecifierType.RevieweeName.name(), revieweeName).
                add(SpecifierType.RatingName.name(), ratingName).
                add(SpecifierType.CommentName.name(), commentName);
    }

    public Object getReviewValue() {
        return reviewValue;
    }

}
