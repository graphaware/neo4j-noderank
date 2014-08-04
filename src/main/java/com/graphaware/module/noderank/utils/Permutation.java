package com.graphaware.module.noderank.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Generator of permutations in lexicographic ordering
 */
public class Permutation<T> {
    public Permutation() {}

    // suppose that comparator returns 1 if first > second
    //          and                   -1 if first < second

    /**
     * Given a permutation and a comparator,
     * returns next permutation of the nodes
     * @param initial initial permutation as a list
     * @param comparator type comparator
     * @return nextPermutation in the sequence
     *
     * TODO: Check if the last permutation has been found
     */
    public List<T> nextPermutation(List<T> initial, Comparator<T> comparator) {
        List<T> shallowCopy = new ArrayList<>(initial);

        if(isReverseOrdered(shallowCopy, comparator))
            return shallowCopy;

        int i, j;
        for (i = shallowCopy.size()-1; i > 1; --i)
            if (comparator.compare(shallowCopy.get(i), shallowCopy.get(i - 1)) == 1) // stop at ordered seq. i-1 < i
                break;


         // i index of the largest descending pair is found
        for (j = shallowCopy.size()-1; j > i; --j) {
            if (comparator.compare(shallowCopy.get(j), shallowCopy.get(i - 1)) == 1) // i-1 < j
                break;
        }

        // swap index is found
        T temp = shallowCopy.get(j);
        shallowCopy.set(j, shallowCopy.get(i-1));
        shallowCopy.set(i-1, temp);

        reverseSublist(shallowCopy, i, shallowCopy.size()-1);

        return shallowCopy;
    }

    /**
     * Reverses a sublist of a list
     * @param list to reverse
     * @param from index
     * @param to index
     */
    public void reverseSublist(List<T> list, int from, int to) {

        // reverse suffix starting at i ?
        int length = to - from;
        for (int k = 0; k <= length/2; ++k) {
            T left  = list.get(from + k);
            T right = list.get(to - k);

            list.set(from + k, right);
            list.set(to - k, left);
        }
    }

    /***
     * Checks if the list is reverse ordered.
     * @param list list to check
     * @param comparator comparator on the list
     * @return true if reverse ordered
     */
    public boolean isReverseOrdered(List<T> list, Comparator<T> comparator) {
        for (int i = 0; i < list.size() - 1; ++i)
            if(comparator.compare(list.get(i), list.get(i+1)) == -1)
                return false;


        return true;
    }


    /**
     * Checks if the list is ordered
     * @param list list to check
     * @param comparator comparator on the list
     * @return true if ordered
     */
    public boolean isOrdered(List<T> list, Comparator<T> comparator) {
        for (int i = 0; i < list.size() - 1; ++i)
            if(comparator.compare(list.get(i), list.get(i+1)) == 1)
                return false;


        return true;
    }


}
