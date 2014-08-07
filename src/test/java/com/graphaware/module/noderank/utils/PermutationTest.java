package com.graphaware.module.noderank.utils;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class PermutationTest {
    private static final Logger LOG = LoggerFactory.getLogger(PermutationTest.class);

    @Test
    public void testPermutationGeneration() {
        Permutation<Integer> permutation = new Permutation<>(Arrays.asList(1, 2, 3, 4));
        LOG.info(permutation.toString());

        Permutation<Integer> permutation1 = permutation.nextPermutation();
        LOG.info(permutation1.toString());

        Permutation<Integer> permutation2 = permutation1.nextPermutation();
        LOG.info(permutation2.toString());

        Permutation<Integer> permutation3 = permutation2.nextPermutation();
        LOG.info(permutation3.toString());

        for (int i = 0; i < 24; ++i) {
            LOG.info("{}", permutation.getPermutationIndex());
            permutation = permutation.nextPermutation();
            LOG.info("{}", permutation.toString());

        }

        Permutation<Integer> permutation4 = new Permutation<>(Arrays.asList(1, 2, 3, 4));
        LOG.info("{}", permutation4.getLehmerCode());

        Permutation<Integer> permutation5 = new Permutation<>(Arrays.asList(1, 2, 3, 4), Arrays.asList(4, 3, 2, 1));
        LOG.info("{}", permutation5.getLehmerCode());

        Permutation<Character> permutationS = new Permutation<>(Arrays.asList('A', 'B', 'C', 'D'));
        LOG.info("{}", permutationS.getPermutationIndex());

        permutationS = permutationS.nextPermutation();
        LOG.info("{}", permutationS.getPermutationIndex());

        Permutation<Integer> permutationT = new Permutation<>(Arrays.asList(1, 2, 3, 4), Arrays.asList(1, 2, 4, 3));
        assertEquals(permutationT, permutationS);

        Permutation<Integer> permutationTN = new Permutation<>(Arrays.asList(1, 2, 3, 4, 5), Arrays.asList(1, 2, 3, 5, 4));
        assertNotEquals(permutationT, permutationTN);
    }


}