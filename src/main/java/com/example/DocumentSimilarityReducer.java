package com.example;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.*;

public class DocumentSimilarityReducer extends Reducer<Text, Text, Text, Text> {

    private final Map<String, Set<String>> docTermMap = new HashMap<>();

    @Override
    public void reduce(Text term, Iterable<Text> docIds, Context context)
            throws IOException, InterruptedException {
        List<String> docList = new ArrayList<>();

        for (Text docId : docIds) {
            String docName = docId.toString();
            docList.add(docName);

            // Store terms for each document
            docTermMap.putIfAbsent(docName, new HashSet<>());
            docTermMap.get(docName).add(term.toString());
        }
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        List<String> docList = new ArrayList<>(docTermMap.keySet());

        // Compare each document pair and calculate Jaccard Similarity
        for (int i = 0; i < docList.size(); i++) {
            for (int j = i + 1; j < docList.size(); j++) {
                String firstDoc = docList.get(i);
                String secondDoc = docList.get(j);

                double similarityScore = calculateJaccardSimilarity(firstDoc, secondDoc);

                if (similarityScore > 0) {
                    String key = "<" + firstDoc + ", " + secondDoc + ">";
                    String value = " -> " + String.format("%.2f", similarityScore * 100) + "%";
                    context.write(new Text(key), new Text(value));
                }
            }
        }
    }

    private double calculateJaccardSimilarity(String firstDoc, String secondDoc) {
        Set<String> termsFirstDoc = docTermMap.getOrDefault(firstDoc, new HashSet<>());
        Set<String> termsSecondDoc = docTermMap.getOrDefault(secondDoc, new HashSet<>());

        Set<String> intersection = new HashSet<>(termsFirstDoc);
        intersection.retainAll(termsSecondDoc); // Compute |A ∩ B|

        Set<String> union = new HashSet<>(termsFirstDoc);
        union.addAll(termsSecondDoc); // Compute |A ∪ B|

        if (union.isEmpty())
            return 0; // Avoid division by zero

        return (double) intersection.size() / union.size();
    }
}
