/*
 *  Copyright (c) 2022, Carnegie Mellon University.  All Rights Reserved.
 *  Version 3.10.
 *  
 *  Compatible with Lucene 8.1.1.
 */
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import org.apache.lucene.index.*;

/**
 *  This software illustrates the architecture for the portion of a
 *  search engine that evaluates queries.  It is a guide for class
 *  homework assignments, so it emphasizes simplicity over efficiency.
 *  It implements an unranked Boolean retrieval model, however it is
 *  easily extended to other retrieval models.  For more information,
 *  see the ReadMe.txt file.
 */
public class QryEval {

  //  --------------- Constants and variables ---------------------

  private static final String USAGE =
    "Usage:  java QryEval paramFile\n\n";
  private static Map<String, String> parameters;
  //  --------------- Methods ---------------------------------------

  /**
   *  @param args The only argument is the parameter file name.
   *  @throws Exception Error accessing the Lucene index.
   */
  public static void main(String[] args) throws Exception {

    //  This is a timer that you may find useful.  It is used here to
    //  time how long the entire program takes, but you can move it
    //  around to time specific parts of your code.
    
    Timer timer = new Timer();
    timer.start ();

    //  Check that a parameter file is included, and that the required
    //  parameters are present.  Just store the parameters.  They get
    //  processed later during initialization of different system
    //  components.

    if (args.length < 1) {
      throw new IllegalArgumentException (USAGE);
    }

    parameters = readParameterFile (args[0]);

    //  Open the index and initialize the retrieval model.

    Idx.open (parameters.get ("indexPath"));
    RetrievalModel model = initializeRetrievalModel (parameters);

    //  Perform experiments.
    
    processQueryFile(parameters.get("queryFilePath"), model);

    //  Clean up.
    
    timer.stop ();
    System.out.println ("Time:  " + timer);
  }

  /**
   *  Allocate the retrieval model and initialize it using parameters
   *  from the parameter file.
   *  @return The initialized retrieval model
   *  @throws IOException Error accessing the Lucene index.
   */
  private static RetrievalModel initializeRetrievalModel (Map<String, String> parameters)
    throws IOException {

    RetrievalModel model = null;
    String modelString = parameters.get ("retrievalAlgorithm").toLowerCase();

    if (modelString.equals("unrankedboolean")) {

      model = new RetrievalModelUnrankedBoolean();

      //  If this retrieval model had parameters, they would be
      //  initialized here.

    }

    //  STUDENTS::  Add new retrieval models here.
    else if (modelString.equals("rankedboolean")) {
      model = new RetrievalModelRankedBoolean();
    }
    else {
      throw new IllegalArgumentException
        ("Unknown retrieval model " + parameters.get("retrievalAlgorithm"));
    }
      
    return model;
  }

  /**
   * Print a message indicating the amount of memory used. The caller can
   * indicate whether garbage collection should be performed, which slows the
   * program but reduces memory usage.
   * 
   * @param gc 
   *          If true, run the garbage collector before reporting.
   */
  public static void printMemoryUsage(boolean gc) {

    Runtime runtime = Runtime.getRuntime();

    if (gc)
      runtime.gc();

    System.out.println("Memory used:  "
        + ((runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L)) + " MB");
  }

  /**
   * Process one query.
   * @param qString A string that contains a query.
   * @param model The retrieval model determines how matching and scoring is done.
   * @return Search results
   * @throws IOException Error accessing the index
   */
  static ScoreList processQuery(String qString, RetrievalModel model)
    throws IOException {

    String defaultOp = model.defaultQrySopName ();
    qString = defaultOp + "(" + qString + ")";
    // the parent must be QrySop
    Qry q = QryParser.getQuery (qString);
    // Show the query that is evaluated
    
    System.out.println("    --> " + q);
    
    if (q != null) {

      ScoreList results = new ScoreList ();
      
      if (q.args.size () > 0) {		// Ignore empty queries

        q.initialize (model);

        while (q.docIteratorHasMatch (model)) {
          int docid = q.docIteratorGetMatch ();
          double score = ((QrySop) q).getScore (model);
          results.add (docid, score);
          q.docIteratorAdvancePast (docid);
        }
      }

      return results;
    } else
      return null;
  }

  /**
   *  Process the query file.
   *  @param queryFilePath Path to the query file
   *  @param model A retrieval model that will guide matching and scoring
   *  @throws IOException Error accessing the Lucene index.
   */
  static void processQueryFile(String queryFilePath,
                               RetrievalModel model)
      throws IOException {

    BufferedReader input = null;

    try {
      String qLine = null;

      input = new BufferedReader(new FileReader(queryFilePath));

      //  Each pass of the loop processes one query.

      while ((qLine = input.readLine()) != null) {
        printMemoryUsage(false);
        System.out.println("Query " + qLine);
        String[] pair = qLine.split(":");

        if (pair.length != 2) {
          throw new IllegalArgumentException
                  ("Syntax error:  Each line must contain one ':'.");
        }

        String qid = pair[0];
        String query = pair[1];
        ScoreList results = processQuery(query, model);
        int limit = Integer.parseInt(parameters.get("trecEvalOutputLength"));
        exportResult(qid, results, limit);
//        printResults(qid, results);
        System.out.println();

      }
    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      input.close();
    }
  }


  private static class Score {
    private String docid;
    private double score;

    public Score(String docid, double score) {
      this.docid = docid;
      this.score = score;
    }
    @Override
    public String toString() {
      return "docId: " + this.docid + " score: " + this.score;
    }
  }


  static void exportResult(String qid, ScoreList result, int limit) throws IOException {
    FileWriter fw = new FileWriter(parameters.get("trecEvalOutputPath"), true);
    BufferedWriter bw = new BufferedWriter(fw);
    String reference = " 2022/09/14";
    try {
      if (result.size() == 0) {
        String s = qid + " Q0 NothingMatched" + " 1" + " 0" + reference;
        bw.write(s);
        bw.newLine();
      } else {
        /**
         * Method1: using priority queue to pick top k element. Theoretically, its
         * time complexity is O(N logK) is better than sorting the whole list O(N logN),
         * when K < N, where N is the length of  score list and K is the maximum
         * number of return value.
         * However, both run time is similar on homework testing system,
         * so I currently decide to simply sort the list and write out top K element. But still
         * keep this code block in case we need it later.
         */

//        PriorityQueue<Score> pq = new PriorityQueue<>((o1, o2) -> {
//          if (Double.compare(o1.score, o2.score) == 0) {
//            return o2.docid.compareTo(o1.docid);
//          }
//          return Double.compare(o1.score, o2.score);
//        });
//
//        for (int i = 0; i < result.size(); i++) {
//          String docId;
//          Score curr;
//          double score = result.getDocidScore(i);
//          docId = result.getExternalDocid(i);
//          curr = new Score(docId, score);
//          if (pq.size() < limit) {
//            pq.offer(curr);
//          } else {
//            if (score > pq.peek().score || score == pq.peek().score && docId.compareTo(pq.peek().docid) < 0) {
//              pq.offer(curr);
//              pq.poll();
//            }
//          }
//        }
//
//        int idx = 1;
//        int N = pq.size();
//        String[] output = new String[N];
//        while (!pq.isEmpty()) {
//          Score score = pq.poll();
//          String s = qid + " Q0 " + score.docid + " " + (N - idx + 1) + " " + score.score + reference;
//          output[N - idx] = s;
//          idx++;
//        }
//        for (int i = 0; i < output.length; i++) {
//          bw.write(output[i]);
//          bw.newLine();
//        }

        int size = limit < result.size() ? limit : result.size();
        result.sort();
        for (int i = 0; i < size; i++) {
          String id = result.getExternalDocid(i);
          Double sc = result.getDocidScore(i);
          String s = qid + " Q0 " + id + " " + (i + 1) + " " + sc + reference;
          bw.write(s);
          bw.newLine();
        }
      }

    } catch (Exception e) {
      e.getStackTrace();
    } finally {
      bw.close();
      System.out.println("Successfully written");
    }

  }

  /**
   * Print the query results.
   * 
   * STUDENTS:: 
   * This is not the correct output format. You must change this method so
   * that it outputs in the format specified in the homework page, which is:
   * 
   * QueryID Q0 DocID Rank Score RunID
   * 
   * @param queryName
   *          Original query.
   * @param result
   *          A list of document ids and scores
   * @throws IOException Error accessing the Lucene index.
   */
  static void printResults(String queryName, ScoreList result) throws IOException {

    if (result.size() < 1) {
      System.out.println("\tNo results.");
    } else {
      for (int i = 0; i < result.size(); i++) {
        System.out.println("\t" + i + ":  " + Idx.getExternalDocid(result.getDocid(i)) + ", "
            + result.getDocidScore(i));
      }
    }
    System.out.println(result.size());
  }

  /**
   *  Read the specified parameter file, and confirm that the required
   *  parameters are present.  The parameters are returned in a
   *  HashMap.  The caller (or its minions) are responsible for processing
   *  them.
   *  @return The parameters, in <key, value> format.
   */
  private static Map<String, String> readParameterFile (String parameterFileName)
    throws IOException {

    Map<String, String> parameters = new HashMap<String, String>();
    File parameterFile = new File (parameterFileName);

    if (! parameterFile.canRead ()) {
      throw new IllegalArgumentException
        ("Can't read " + parameterFileName);
    }

    //  Store (all) key/value parameters in a hashmap.

    Scanner scan = new Scanner(parameterFile);
    String line = null;
    do {
      line = scan.nextLine();
      String[] pair = line.split ("=");
      parameters.put(pair[0].trim(), pair[1].trim());
    } while (scan.hasNext());

    scan.close();

    //  Confirm that some of the essential parameters are present.
    //  This list is not complete.  It is just intended to catch silly
    //  errors.

    if (! (parameters.containsKey ("indexPath") &&
           parameters.containsKey ("queryFilePath") &&
           parameters.containsKey ("trecEvalOutputPath") &&
           parameters.containsKey ("retrievalAlgorithm"))) {
      throw new IllegalArgumentException
        ("Required parameters were missing from the parameter file.");
    }

    return parameters;
  }

}
