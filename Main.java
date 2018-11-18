/*
 * CS 482 Assignment 1
 * Myungsun Jung
 * 20511678
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    private static final int NINF = Integer.MIN_VALUE/2; // negative infinity (somewhat prevents integer overflow)
    private static final int match = 1, mismatch = -1, gapopen = -5, gapxt = -1; // score scheme
    private static int [][] scoreMatrix0; // match or mismatch
    private static int [][] scoreMatrix1; // gap in first sequence
    private static int [][] scoreMatrix2; // gap in second sequence
    // pointer matrices for traceback. 0 -> to scoreMatrix0, 1 -> to scoreMatrix1, ...
    private static int [][] pointMatrix0;
    private static int [][] pointMatrix1;
    private static int [][] pointMatrix2;

    // prints two aligned sequence
    private static void traceback(int startMatrix, String seq1, String seq2) {
        int i = seq1.length();
        int j = seq2.length();
        StringBuilder firstSeq = new StringBuilder();
        StringBuilder secondSeq = new StringBuilder();

        int nextMatrix = startMatrix;
        while (i > 0 || j > 0) {
            if (nextMatrix == 0) { // match or mismatch
                nextMatrix = pointMatrix0[j][i];

                --i;
                --j;
                // get both chars from seq1 and seq2
                firstSeq.insert(0, seq1.charAt(i));
                secondSeq.insert(0, seq2.charAt(j));
            } else if (nextMatrix == 1) { // gap in first sequence
                nextMatrix = pointMatrix1[j][i];

                --j;
                // get char from seq2
                firstSeq.insert(0, '-');
                secondSeq.insert(0, seq2.charAt(j));
            } else { // gap in second sequence
                nextMatrix = pointMatrix2[j][i];

                --i;
                // get char from seq1
                firstSeq.insert(0, seq1.charAt(i));
                secondSeq.insert(0, '-');
            }
        } // while

        System.out.println(firstSeq);
        System.out.println(secondSeq);
    } // traceback()

    // prints best alignment score for two sequence
    private static void alignment(String seq1, String seq2) {
        // to add extra space for first row & first column of the matrix
        String firstSeq = ' ' + seq1;
        String secondSeq = ' ' + seq2;

        // set matrix size
        scoreMatrix0 = new int[secondSeq.length()][firstSeq.length()];
        scoreMatrix1 = new int[secondSeq.length()][firstSeq.length()];
        scoreMatrix2 = new int[secondSeq.length()][firstSeq.length()];
        pointMatrix0 = new int[secondSeq.length()][firstSeq.length()];
        pointMatrix1 = new int[secondSeq.length()][firstSeq.length()];
        pointMatrix2 = new int[secondSeq.length()][firstSeq.length()];

        // set first row and column of matrices
        // scoreMatrix0: ROW: no such alignment can exist -> -infinity
        //            COLUMN: no such alignment can exist -> -infinity
        // scoreMatrix1: ROW: all gap for firstSeq -> gapopen + i*gapxt
        //            COLUMN: no such alignment can exist -> -infinity
        // scoreMatrix2: ROW: no such alignment can exist -> -infinity
        //            COLUMN: all gap for secondSeq -> gapopen + j*gapxt
        for (int i = 1; i < firstSeq.length(); ++i) {
            scoreMatrix0[0][i] = NINF;
            scoreMatrix1[0][i] = NINF;
            scoreMatrix2[0][i] = gapopen + i*gapxt;
            pointMatrix2[0][i] = 2; // to stay in pointMatrix2 in traceback
        }
        for (int j = 1; j < secondSeq.length(); ++j) {
            scoreMatrix0[j][0] = NINF;
            scoreMatrix1[j][0] = gapopen + j*gapxt;
            scoreMatrix2[j][0] = NINF;
            pointMatrix1[j][0] = 1; // to stay in pointMatrix1 in traceback
        }

        // fill-in the matrices
        for (int j = 1; j < secondSeq.length(); ++j) {
            for (int i = 1; i < firstSeq.length(); ++i) {
                // Recurrence Relations
                // scoreMatrix0: match or mismatch
                if (scoreMatrix0[j-1][i-1] >= scoreMatrix1[j-1][i-1] && scoreMatrix0[j-1][i-1] >= scoreMatrix2[j-1][i-1]) {
                    if (firstSeq.charAt(i) == secondSeq.charAt(j)) {
                        scoreMatrix0[j][i] = match + scoreMatrix0[j-1][i-1];
                        pointMatrix0[j][i] = 0;
                    } else {
                        scoreMatrix0[j][i] = mismatch + scoreMatrix0[j-1][i-1];
                        pointMatrix0[j][i] = 0;
                    }
                } else if (scoreMatrix1[j-1][i-1] >= scoreMatrix2[j-1][i-1]) {
                    if (firstSeq.charAt(i) == secondSeq.charAt(j)) {
                        scoreMatrix0[j][i] = match + scoreMatrix1[j-1][i-1];
                        pointMatrix0[j][i] = 1;
                    } else {
                        scoreMatrix0[j][i] = mismatch + scoreMatrix1[j-1][i-1];
                        pointMatrix0[j][i] = 1;
                    }
                } else {
                    if (firstSeq.charAt(i) == secondSeq.charAt(j)) {
                        scoreMatrix0[j][i] = match + scoreMatrix2[j-1][i-1];
                        pointMatrix0[j][i] = 2;
                    } else {
                        scoreMatrix0[j][i] = mismatch + scoreMatrix2[j-1][i-1];
                        pointMatrix0[j][i] = 2;
                    }
                }

                // scoreMatrix1: gap in firstSeq
                if (scoreMatrix0[j-1][i] + gapopen > scoreMatrix1[j-1][i]) {
                    scoreMatrix1[j][i] = gapxt + scoreMatrix0[j-1][i] + gapopen;
                    pointMatrix1[j][i] = 0;
                } else {
                    scoreMatrix1[j][i] = gapxt + scoreMatrix1[j-1][i];
                    pointMatrix1[j][i] = 1;
                }

                // scoreMatrix2: gap in secondSeq
                if (scoreMatrix0[j][i-1] + gapopen > scoreMatrix2[j][i-1]) {
                    scoreMatrix2[j][i] = gapxt + scoreMatrix0[j][i-1] + gapopen;
                    pointMatrix2[j][i] = 0;
                } else {
                    scoreMatrix2[j][i] = gapxt + scoreMatrix2[j][i-1];
                    pointMatrix2[j][i] = 2;
                }
            } // for
        } // for

        // determine the best score among three matrices
        int score = scoreMatrix0[secondSeq.length()-1][firstSeq.length()-1];
        int startMatrix = 0; // for traceback function
        if (score < scoreMatrix1[secondSeq.length()-1][firstSeq.length()-1]
                || score < scoreMatrix2[secondSeq.length()-1][firstSeq.length()-1]) {
            if (scoreMatrix1[secondSeq.length()-1][firstSeq.length()-1] > scoreMatrix2[secondSeq.length()-1][firstSeq.length()-1]) {
                score = scoreMatrix1[secondSeq.length()-1][firstSeq.length()-1];
                startMatrix = 1;
            } else {
                score = scoreMatrix2[secondSeq.length()-1][firstSeq.length()-1];
                startMatrix = 2;
            }
        }

        System.out.println(score); // print the best score
        traceback(startMatrix, seq1, seq2); // prints two aligned sequence
    } // alignment(String seq1, String seq2)

    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String firstSeq = br.readLine().replaceAll("[^ATGCatgc]+", "").toUpperCase();
            String secondSeq = br.readLine().replaceAll("[^ATGCatgc]+", "").toUpperCase();

            alignment(firstSeq, secondSeq); // also runs traceback function
        } catch (IOException e) {
            e.printStackTrace();
        } // try catch
    } // main
}
