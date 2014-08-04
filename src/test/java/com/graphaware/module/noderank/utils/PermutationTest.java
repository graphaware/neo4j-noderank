package com.graphaware.module.noderank.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class PermutationTest {

    /**
     * Test substring reversal
     */
    @Test
    public void testSubstringReverse() {
        Permutation<Integer> permutation = new Permutation<>();
        List<Integer> toReverse = Arrays.asList(1,2,3,4);
        permutation.reverseSublist(toReverse, 1, 3); // Perform this on shallowCopy as well?
        System.out.println(toReverse);



    }

    @Test
    public void testPermutationGeneration() {
        Permutation<Integer> permutation = new Permutation<>();
        List<Integer> toPermute = Arrays.asList(1,2,3,4);

        // clumsy
        Comparator<Integer> comp = new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        };

        List<Integer> p1 = toPermute;

        for(int i = 0; i < 24; ++i) {
            p1 = permutation.nextPermutation(p1, comp);
            System.out.format("%s\n", p1.toString());
        }

    }

}