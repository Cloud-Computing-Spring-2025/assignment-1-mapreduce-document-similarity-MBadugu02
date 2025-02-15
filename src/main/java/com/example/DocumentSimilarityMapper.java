package com.example;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.HashSet;
import java.util.StringTokenizer;

public class DocumentSimilarityMapper extends Mapper<Object, Text, Text, Text> {

    @Override
    public void map(Object inputKey, Text inputValue, Context context) throws IOException, InterruptedException {
        String inputLine = inputValue.toString();
        String[] splitLine = inputLine.split("\\s+", 2);

        if (splitLine.length < 2)
            return; // Skip malformed entries

        String documentId = splitLine[0];
        String documentText = splitLine[1];

        HashSet<String> termSet = new HashSet<>();
        StringTokenizer termTokenizer = new StringTokenizer(documentText);

        while (termTokenizer.hasMoreTokens()) {
            termSet.add(termTokenizer.nextToken().toLowerCase());
        }

        // Emit (word, documentId) for further grouping in the reducer
        for (String term : termSet) {
            context.write(new Text(term), new Text(documentId));
        }
    }
}
