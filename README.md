# Document Similarity Using MapReduce

## Project Overview

This project leverages the *Apache Hadoop MapReduce framework* to compute *Jaccard Similarity* between multiple text documents. The Jaccard Similarity quantifies the similarity between two sets of words and is defined as:

\[
Jaccard Similarity (A, B) = \frac{|A \cap B|}{|A \cup B|}
\]

The key steps involved include:
- Tokenizing documents and extracting unique words.
- Determining word intersections and unions for document pairs.
- Calculating the Jaccard Similarity score for each document pair.

## Approach and Implementation

### Mapper (DocumentSimilarityMapper)
- Reads input text, where each line represents a document.
- Extracts the document ID and its content.
- Tokenizes content into unique lowercase words.
- Emits (word, document ID) pairs for further processing.

### Reducer (DocumentSimilarityReducer)
- Groups document IDs based on unique words.
- Constructs word sets for each document.
- Computes the Jaccard Similarity between document pairs based on shared words.
- Outputs (document pair, similarity score).

## Execution Steps

### 1. Start the Hadoop Cluster
```bash
docker compose up -d
```

### 2. Build the Code
```bash
mvn install
```

### 3. Move JAR Files
```bash
mv target/*.jar shared-folder/input/code/
```

### 4. Copy JAR to Docker Container
```bash
docker cp shared-folder/input/code/DocumentSimilarity-0.0.1-SNAPSHOT.jar resourcemanager:/opt/hadoop-3.2.1/share/hadoop/mapreduce/
```

### 5. Transfer Dataset to Docker Container
```bash
docker cp shared-folder/input/data/doc.txt resourcemanager:/opt/hadoop-3.2.1/share/hadoop/mapreduce/
```

### 6. Access Docker Container
```bash
docker exec -it resourcemanager /bin/bash
```

### 7. Navigate to the Hadoop Directory
```bash
cd /opt/hadoop-3.2.1/share/hadoop/mapreduce/
```

### 8. Create Input Directory in HDFS
```bash
hadoop fs -mkdir -p /input/dataset
```

### 9. Upload Dataset to HDFS
```bash
hadoop fs -put ./doc.txt /input/dataset/
```

### 10. Execute the MapReduce Job
```bash
hadoop jar /opt/hadoop-3.2.1/share/hadoop/mapreduce/DocumentSimilarity-0.0.1-SNAPSHOT.jar com.example.controller.DocumentSimilarityDriver /input/dataset /output
```

### 11. View the Output
```bash
hadoop fs -cat /output/part-r-00000
```

### 12. Copy Output from HDFS to Local System
```bash
hdfs dfs -get /output /opt/hadoop-3.2.1/share/hadoop/mapreduce/
exit
docker cp resourcemanager:/opt/hadoop-3.2.1/share/hadoop/mapreduce/output/ shared-folder/output/
```

## Challenges and Solutions

### 1. Issue: Docker `cp` Command Failure
- **Problem:** The `docker cp` command failed when the `resourcemanager` container was not running.
- **Solution:** Verified container status using `docker ps` and ensured the destination path existed in the container.

### 2. Issue: Existing Output Directory Error
- **Problem:** Hadoop job failed due to an existing output directory.
- **Solution:** Removed the output directory before execution:
  ```bash
  hadoop fs -rm -r /output
  ```

### 3. Issue: HDFS Directory/File Errors
- **Problem:** Errors such as "No such file or directory" or "Permission denied" when copying files.
- **Solution:** Verified HDFS was running using:
  ```bash
  hadoop fs -ls /
  ```

### 4. Issue: Job Execution Failures
- **Problem:** Job failed due to a missing JAR file, incorrect class name, or insufficient cluster resources.
- **Solution:** Ensured the JAR file was correctly copied and verified using:
  ```bash
  ls /opt/hadoop-3.2.1/share/hadoop/mapreduce/
  ```

## Sample Output

### Input Format:
```
doc1    Hotels have good food
doc2    Good food is better for good health
doc3    Taste is not important for health
```

### Expected Output:
```
<doc3, doc2>	 -> 33.33%
<doc2, doc1>	 -> 25.00%

