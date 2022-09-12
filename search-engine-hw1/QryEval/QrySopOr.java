/**
 *  Copyright (c) 2022, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.*;

/**
 *  The OR operator for all retrieval models.
 */
public class QrySopOr extends QrySop {

  /**
   *  Indicates whether the query has a match.
   *  @param r The retrieval model that determines what is a match
   *  @return True if the query matches, otherwise false.
   */
  public boolean docIteratorHasMatch (RetrievalModel r) {
    return this.docIteratorHasMatchMin (r);
  }

  /**
   *  Get a score for the document that docIteratorHasMatch matched.
   *  @param r The retrieval model that determines how scores are calculated.
   *  @return The document score.
   *  @throws IOException Error accessing the Lucene index
   */
  public double getScore (RetrievalModel r) throws IOException {

    if (r instanceof RetrievalModelUnrankedBoolean) {
      return this.getScoreUnrankedBoolean (r);
    }

    //  STUDENTS::
    //  Add support for other retrieval models here.
    else if (r instanceof RetrievalModelRankedBoolean) {
      return this.getScoreRankedBoolean (r);
    }

    else {
      throw new IllegalArgumentException
        (r.getClass().getName() + " doesn't support the OR operator.");
    }
  }
  
  /**
   *  getScore for the UnrankedBoolean retrieval model.
   *  @param r The retrieval model that determines how scores are calculated.
   *  @return The document score.
   *  @throws IOException Error accessing the Lucene index
   */
  private double getScoreUnrankedBoolean (RetrievalModel r) throws IOException {
    //  Unranked Boolean systems only have two scores:
    //  1 (document matches) and 0 (document doesn't match).  QryEval
    //  only calls getScore for documents that match, so if we get
    //  here, the document matches, and its score should be 1.  The
    //  most efficient implementation returns 1 from here.
    //
    //  Other retrieval models must do more work.  To help students
    //  understand how to implement other retrieval models, this
    //  method uses a more general solution.  OR takes the maximum
    //  of the scores from its children query nodes.


    if (! this.docIteratorHasMatchCache()) {
      return 0.0;
    } else {
      return 1.0;
    }
  }

  private double getScoreRankedBoolean (RetrievalModel r) throws IOException {
    if (!this.docIteratorHasMatchCache()) {
      return 0.0;
    } else {
      // Because or operator just requires one of the terms is matched, when calculating
      // score, must check whether this term is matched or not.
      double maxScore = 0.0;
      int docid = this.docIteratorGetMatch();
      for (Qry q_i : this.args) {
        double tmpScore = 0.0;
        if (q_i.docIteratorHasMatchCache()) {
//                  System.out.println("has match cache");
          if (docid == q_i.docIteratorGetMatch()) {
//                      System.out.println("go to score operator");
            tmpScore = ((QrySop)q_i).getScore(r);
          }
          maxScore = Math.max(maxScore, tmpScore);
        }
      }
      return maxScore;
    }
  }

}
