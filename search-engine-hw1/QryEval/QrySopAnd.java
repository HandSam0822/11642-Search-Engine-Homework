/**
 *  Copyright (c) 2022, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.*;

/**
 *  The OR operator for all retrieval models.
 */
public class QrySopAnd extends QrySop {

    /**
     *  Indicates whether the query has a match.
     *  @param r The retrieval model that determines what is a match
     *  @return True if the query matches, otherwise false.
     */
    public boolean docIteratorHasMatch (RetrievalModel r) {
        return this.docIteratorHasMatchAll (r);
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
            double minScore = Double.MAX_VALUE;
            // Because and operator requires all terms are matched, as long as the
            // docIterator has cached the matched document id, all the terms are matched.
            // There is no need to check whether the terms are matched or not(like the method
            // done in QrySopOr class).
            for (Qry q_i : this.args) {
                double tmpScore = ((QrySop)q_i).getScore(r);
                minScore = Math.min(tmpScore, minScore);
            }
            return minScore;
        }
    }

}
