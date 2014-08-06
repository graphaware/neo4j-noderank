package com.graphaware.module.noderank.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.google.common.math.IntMath;
import com.google.common.math.LongMath;
import java.util.List;

/**
 * Generator of permutations in lexicographic ordering
 */
public class Permutation<T extends Comparable<T>> {
    private final List<T> from;
    private final List<T> to;

    public Permutation(List<T> init) {
        this.to = init;
        this.from = init;
    }

    public Permutation(List<T> from, List<T> to) {
        this.to = to;
        this.from = from;

        for(T first : from)
            if (!to.contains(first))
                throw new RuntimeException("Invalid arguments passed to permutation");
    }

    // suppose that comparator returns 1 if first > second
    //          and                   -1 if first < second

    /**
     * Given a permutation and a comparator,
     * returns next permutation of the nodes
     * @return nextPermutation in the sequence
     *
     * TODO: Check if the last permutation has been found
     */
    public Permutation<T> nextPermutation() {
        List<T> shallowCopy = new ArrayList<>(to);

        if(isReverseOrdered(shallowCopy))
            return new Permutation<>(from, shallowCopy);

        int i, j;
        for (i = shallowCopy.size()-1; i > 1; --i)
            if (shallowCopy.get(i).compareTo(shallowCopy.get(i - 1)) == 1) // stop at ordered seq. i-1 < i
                break;


         // i index of the largest descending pair is found
        for (j = shallowCopy.size()-1; j > i; --j) {
            if (shallowCopy.get(j).compareTo(shallowCopy.get(i - 1)) == 1) // i-1 < j
                break;
        }

        // swap index is found
        T temp = shallowCopy.get(j);
        shallowCopy.set(j, shallowCopy.get(i-1));
        shallowCopy.set(i-1, temp);

        reverseSublist(shallowCopy, i, shallowCopy.size()-1);

        return new Permutation<>(from, shallowCopy);
    }

    /**
     * Returns a Lehmer Code of the permutation
     * @return
      */
     public List<Integer> getLehmerCode() {
          List<T> shallowFrom = new ArrayList<>(from);
          List<T> shallowTo = new ArrayList<>(to);
          List<Integer> lehmerCode  = new ArrayList<>();

          for(int t = 0; t < shallowTo.size() ; ++t) {
              T decElem = shallowTo.get(t);
              int factoradicDigit = shallowFrom.indexOf(decElem);
              shallowFrom.remove(factoradicDigit);
              lehmerCode.add(factoradicDigit);
         }
         return lehmerCode;
     }

    /**
     * Returns a lexicographic index corresponding to the permutation
     * @return
     */
     public int getPermutationIndex() {
         int index = 0;
         List<Integer> lehmerCode = getLehmerCode();
         int size = lehmerCode.size();

         for (int j = size - 1; j >= 0; j-- ) {
             index += lehmerCode.get(j)*IntMath.factorial(size - j -1);
         }

         return index;
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
     * @return true if reverse ordered
     */
    public boolean isReverseOrdered(List<T> list) {
        for (int i = 0; i < list.size() - 1; ++i)
            if ((list.get(i).compareTo(list.get(i+1))) == -1)
                return false;


        return true;
    }


    /**
     * Checks if the list is ordered
     * @param list list to check
     * @return true if ordered
     */
    public boolean isOrdered(List<T> list) {
        for (int i = 0; i < list.size() - 1; ++i)
            if(list.get(i).compareTo(list.get(i+1)) == 1)
                return false;


        return true;
    }

    /**
     *  Returns a string representation of the permutation
     *  TODO: use disjoint set representation instead?
     */
    @Override
    public String toString() {
        String strFrom = from.toString();
        String strTo = to.toString();

        return strFrom + " -> " + strTo;
    }

    /**
     * Returns size of the permutation
     * @return
     */
    public int size() {
        return to.size();
    }

    /**
     * Tests if the two permutations are equal
     * They are equal if the have the same
     * Lehmer code
     * @param other
     * @return
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Permutation)) return false;
        Permutation<T> otherPermutation = (Permutation) other;

        if (otherPermutation.size() != size())
            return false;

        if (otherPermutation.hashCode() == hashCode())
            return true;

        return false;
    }

    /**
     * Returns a hashCode of the permutation
     * (Lehmer code based)
     * @return
     */
    @Override
    public int hashCode() {
        return getPermutationIndex();
    }
}
