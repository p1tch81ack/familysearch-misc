package org.familysearch.joetools.consolidate360;

import org.familysearch.joetools.simpledb.*;

import java.util.*;

public class ReviewStore {
    private SimpleTable<Review> table;

    public ReviewStore(List<Review> reviews) {
        table = new SimpleTable<Review>(reviews, Review.class);
    }

    /*
    public SimpleTable<org.familysearch.joetools.consolidate360.Review> getTable(){
        return table;
    }

    public void addAllReviews(Collection<Review> reviews) {
    }
    */

    private Set<Object> scalaCollectionToSortedJavaSet(scala.collection.immutable.Set<Object> scalaSet){
        TreeSet<Object> ret = new TreeSet<Object>();
        scala.collection.Iterator<Object> scalaSetIterator = scalaSet.iterator();
        while(scalaSetIterator.hasNext()){
            Object nxt = scalaSetIterator.next();
            if(nxt!=null){
                ret.add(nxt);
            }
        }
        return ret;
    }

    public Set<Object> getSpecifierNamesForMatchingReviews(RowSpecifier rowSpecifier, SpecifierType specifierType){
        return scalaCollectionToSortedJavaSet(table.getSpecifierValuesForMatchingRows(rowSpecifier, specifierType.name()));
    }

    private Set<Object> getIterationNames(RowSpecifier rowSpecifier) {
        return getSpecifierNamesForMatchingReviews(rowSpecifier, SpecifierType.iterationName);
    }

    public Set<Object> getIterationNames() {
        return getIterationNames(new RowSpecifier());
    }

    public Collection<Object> getIterationNamesForReviewee(String revieweeName) {
        return getIterationNames(new RowSpecifier(SpecifierType.revieweeName.name(), revieweeName).without(SpecifierType.reviewerName.name(), revieweeName));
    }

    private Collection<Object> getRevieweeNames(RowSpecifier rowSpecifier) {
        return getSpecifierNamesForMatchingReviews(rowSpecifier, SpecifierType.revieweeName);
    }

    public Collection<Object> getRevieweeNames() {
        return getRevieweeNames(new RowSpecifier());
    }

    private Map<String, ? extends List<String>> getListOfCommentsForMatchingReviews(scala.collection.immutable.Map<String, Object> tagsAndValues ) {
        Map<String, java.util.List<String>> out = new TreeMap<String, java.util.List<String>>();
        scala.collection.immutable.HashSet<String> initialTagsToSkip = new scala.collection.immutable.HashSet<String>();
        scala.collection.Set<String> tagsToSkip = initialTagsToSkip.$plus("entryValue");
        scala.collection.immutable.Map<String, ? extends scala.collection.immutable.List<String>> scalaMap =
                table.getMappedListOfValuesMatchingSpecifierGroupedByConcatinatedUniqueValues(
                        tagsAndValues,
                        tagsToSkip,
                        new Function<Review, String>() {
                            public String get(Review o) {
                                return o.getEntryValue().toString();
                            }
                        },
                        " - "
                );
        scala.collection.Iterator<String> scalaMapIterator = scalaMap.keySet().iterator();
        while(scalaMapIterator.hasNext()){
            String key = scalaMapIterator.next();
            scala.collection.immutable.List<String> scalaMapEntry = scalaMap.apply(key);
            java.util.List<String> mapEntry = new LinkedList<String>();
            scala.collection.Iterator<String> scalaMapEntryIterator = scalaMapEntry.iterator();
            while (scalaMapEntryIterator.hasNext()){
                mapEntry.add(scalaMapEntryIterator.next());
            }
            out.put(key, mapEntry);
        }
        return out;
    }

    public String getAllCommentsForMatchingReviews(scala.collection.immutable.Map<String, Object> tagsAndValues, boolean showCommentKeys) {
        Map<String, ? extends List<String>>  lines = getListOfCommentsForMatchingReviews(tagsAndValues);
        return concatinateLines(!showCommentKeys, lines, showCommentKeys);
    }

    public double getAverageRatingForMatchingReviews(RowSpecifier rowSpecifier) {
        return table.getAverageValueForMatchingRows(rowSpecifier,
                new Function<Review, Double>(){public Double get(Review o){
                    Object reviewValue = o.getEntryValue();
                    if(reviewValue instanceof Integer){
                      return (double)((Integer) reviewValue);
                    } else {
                        return null;
                    }
                }}
        );
    }

    public Object getAverageRatingOrCombinedCommentsforMatchingReviews(scala.collection.immutable.Map<String, Object> tagsAndValues, ConsolidationType consolidationType, boolean showCommentKeys) {
        if(consolidationType==ConsolidationType.rating){
            return getAverageRatingForMatchingReviews(RowSpecifier.apply(tagsAndValues));
        } else if(consolidationType == ConsolidationType.comment){
            return getAllCommentsForMatchingReviews(tagsAndValues, showCommentKeys);
        } else {
            throw new IllegalArgumentException("Invalid ConsolidationType specified: " + consolidationType.name());

        }
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
