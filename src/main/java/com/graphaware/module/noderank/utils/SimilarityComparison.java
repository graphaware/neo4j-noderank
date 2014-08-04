package com.graphaware.module.noderank.utils;

import java.util.List;

/**
 * Compares the result lists of two objects
 *
 */
public class SimilarityComparison {

    /**
     * Returns the most basic similarity
     * measure of the two lists of THE SAME LENGTH!
     *
     * This is just a normalized Hamming distance of
     * the two strings.
     *
     * @param a first list
     * @param b second lisr
     * @return normalized hamming distance
     */
    public double getHammingDistanceMeasure(List a, List b) {
        if(a.size() != b.size())
            throw  new RuntimeException("Two lists of unequal length were tested for similarity!");

        int length = a.size();
        int numerator = 0;

        for(int i = 0; i < length; ++i) {
            if(a.get(i).equals(b.get(i)))
                numerator ++;
        }

        return (double) numerator / (double) length;
    }

    /**
     * Returns an unordered similarity of
     * the two lists of THE SAME LENGTH!
     *
     * @param a first list
     * @param b second list
     * @return normalized number of equal elements
     */
    public double unorderedComparisonOfEqualLengthLists(List a, List b) {
        if(a.size() != b.size())
            throw  new RuntimeException("Two lists of unequal length were tested for similarity!");

        int length = a.size();
        int numerator = 0;

        for(int i = 0; i < length; ++i) {
            if(a.contains(b.get(i)))
                numerator ++;
        }

        return (double) numerator / (double) length;
    }

    /**
     * TODO: weight success by lexicographic permutation distance?
     *
     * Weight results by their lexicographic distance
     */
}
