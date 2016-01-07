/*
 * Copyright (c) 2013-2016 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.module.noderank.utils;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class PermutationTest {

    @Test
    public void testPermutationGeneration() {
        /**
         * Construct init object, log the permutation into the console
         */
        Permutation<Integer> permutation = new Permutation<>(Arrays.asList(1, 2, 3, 4));

        /**
         * Check that the permutation index mapping is correct:
         */
        for (int i = 0; i < 24; ++i) {
            assertEquals(permutation.getPermutationIndex().intValue(), i);
            permutation = permutation.nextPermutation();
        }

        /**
         * Check equality between unequal-type permutations corresponding to the same permutation
         * mathematically.
         */
        Permutation<Character> permutationS = new Permutation<>(Arrays.asList('A', 'B', 'C', 'D'));
        permutationS = permutationS.nextPermutation();
        Permutation<Integer> permutationT = new Permutation<>(Arrays.asList(1, 2, 3, 4), Arrays.asList(1, 2, 4, 3));
        assertEquals(permutationT, permutationS);

        /**
         * Check that different-sizes permutations are interpreted as unequal, although they have
         * the same permutation index.
         */
        Permutation<Integer> permutationTN = new Permutation<>(Arrays.asList(1, 2, 3, 4, 5), Arrays.asList(1, 2, 3, 5, 4));
        assertNotEquals(permutationT, permutationTN);


        /**
         * Check that the range of normed permutation index is covered
          */
        Permutation<Integer> completePermutation = new Permutation<>(Arrays.asList(1,2,3,4), Arrays.asList(4,3,2,1));
        System.out.format("Permutation index for complete permutation: %f\n", completePermutation.getNormedPermutationIndex());
        assertEquals(completePermutation.getNormedPermutationIndex(), 0, 10e-7);

        Permutation<Character> zeroPermutation = new Permutation<>(Arrays.asList('a','b','c'), Arrays.asList('a','b', 'c'));
        System.out.format("Permutation index for zero permutation: %f\n", zeroPermutation.getLogNormedPermutationIndex());
        assertEquals(zeroPermutation.getNormedPermutationIndex(), 1.0, 10e-7);

    }
}