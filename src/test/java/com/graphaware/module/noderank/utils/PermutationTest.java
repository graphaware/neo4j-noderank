package com.graphaware.module.noderank.utils;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class PermutationTest {

    @Test
    public void testPermutationGeneration() {
        Permutation<Integer> permutation = new Permutation<>(Arrays.asList(1,2,3,4));
        System.out.println(permutation.toString());

        Permutation<Integer> permutation1 = permutation.nextPermutation();
        System.out.println(permutation1.toString());

        Permutation<Integer> permutation2 = permutation1.nextPermutation();
        System.out.println(permutation2.toString());

        Permutation<Integer> permutation3 = permutation2.nextPermutation();
        System.out.println(permutation3.toString());

        for(int i = 0; i < 24; ++i) {
            System.out.println(permutation.getPermutationIndex());
            permutation = permutation.nextPermutation();
            System.out.format("%s\n", permutation.toString());

        }

        Permutation<Integer> permutation4 = new Permutation<>(Arrays.asList(1,2,3,4));
        System.out.println(permutation4.getLehmerCode());

        Permutation<Integer> permutation5 = new Permutation<>(Arrays.asList(1,2,3,4), Arrays.asList(4,3,2,1));
        System.out.println(permutation5.getLehmerCode());

        Permutation<Character> permutationS = new Permutation<>(Arrays.asList('A','B','C','D'));
        System.out.println(permutationS.getPermutationIndex());

        permutationS = permutationS.nextPermutation();
        System.out.println(permutationS.getPermutationIndex());

        Permutation<Integer> permutationT = new Permutation<>(Arrays.asList(1,2,3,4), Arrays.asList(1,2,4,3));
        assertEquals(permutationT, permutationS);

        Permutation<Integer> permutationTN = new Permutation<>(Arrays.asList(1,2,3,4,5), Arrays.asList(1,2,3,5,4));
        assertNotEquals(permutationT, permutationTN);
    }


}