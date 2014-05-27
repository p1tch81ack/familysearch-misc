
import org.familysearch.joetools.simpledb.Function;
import org.familysearch.joetools.simpledb.RowSpecifier;
import org.familysearch.joetools.simpledb.SimpleTable;

import java.util.*;

public class ReviewStore {
    private SimpleTable<Review> table;

    public ReviewStore() {
        table = new SimpleTable<Review>();
    }

    /*
    public SimpleTable<Review> getTable(){
        return table;
    }
    */

    public void addAllReviews(Collection<Review> reviews) {
        for(Review review: reviews){
            table.addRow(review);
        }
    }

    public Set<String> getSpecifierNamesForMatchingReviews(RowSpecifier rowSpecifier, SpecifierType specifierType){
        return table.getSpecifierValuesForMatchingRows(rowSpecifier, specifierType.name());
    }

    private Set<String> getIterationNames(RowSpecifier rowSpecifier) {
        return getSpecifierNamesForMatchingReviews(rowSpecifier, SpecifierType.IterationName);
    }

    public Set<String> getIterationNames() {
        return getIterationNames(new RowSpecifier());
    }

    public Collection<String> getIterationNamesForReviewee(String revieweeName) {
        return getIterationNames(new RowSpecifier(SpecifierType.RevieweeName.name(), revieweeName).without(SpecifierType.ReviewerName.name(), revieweeName));
    }

    private Collection<String> getRevieweeNames(RowSpecifier rowSpecifier) {
        return getSpecifierNamesForMatchingReviews(rowSpecifier, SpecifierType.RevieweeName);
    }

    public Collection<String> getRevieweeNames() {
        return getRevieweeNames(new RowSpecifier());
    }

    private Map<String, ? extends List<String>> getListOfCommentsForMatchingReviews(RowSpecifier rowSpecifier) {
        return table.getMappedListOfValuesMatchingSpecifierGrupedByConcatinatedUniqueValues(rowSpecifier,
                new Function<Review, String>(){public String get(Review o){return o.getReviewValue().toString();}},
                " - "
        );
    }

    public String getAllCommentsForMatchingReviews(RowSpecifier rowSpecifier, boolean showCommentKeys) {
        Map<String, ? extends List<String>>  lines = getListOfCommentsForMatchingReviews(rowSpecifier);
        return concatinateLines(!showCommentKeys, lines, showCommentKeys);
    }

    public double getAverageRatingForMatchingReviews(RowSpecifier rowSpecifier) {
        return table.getAverageValueForMatchingRows(rowSpecifier,
                new Function<Review, Double>(){public Double get(Review o){
                    Object reviewValue = o.getReviewValue();
                    if(reviewValue instanceof Integer){
                      return (double)((Integer) reviewValue);
                    } else {
                        return null;
                    }
                }}
        );
    }

    public Object getAverageRatingOrCombinedCommentsforMatchingReviews(RowSpecifier rowSpecifier, SpecifierType consolidationSpecifierType, boolean showCommentKeys) {
        if(consolidationSpecifierType==SpecifierType.RatingName){
            return getAverageRatingForMatchingReviews(rowSpecifier);
        } else if(consolidationSpecifierType == SpecifierType.CommentName){
            return getAllCommentsForMatchingReviews(rowSpecifier, showCommentKeys);
        }
        return null;
    }

//                     line += ": ";
    public static String concatinateLines(boolean shuffle, Map<String, ? extends List<String>> map, boolean showCommentKeys) {
        LinkedList<String> lines = new LinkedList<String>();
        for(String key: map.keySet()){
            String line = "";
            if (showCommentKeys && key.length()>0) {
                line += key;
                line += ": ";
            }
            List<String> mapLines = map.get(key);
            if(mapLines.size()<1){
                line += "null";
            } else if(mapLines.size()==1){
                line+=mapLines.get(0);
            } else {
                line+=mapLines;
            }
            lines.add(line);
        }

        if (shuffle) {
            Collections.shuffle(lines);
        }
        String out = "";
        for (String line : lines) {
            if (out.length() > 0) {
                out += "\n\n";
            }
            out += line;
        }
        if (out.length() > 0) {
            return out;
        } else {
            return null;
        }
    }

}
