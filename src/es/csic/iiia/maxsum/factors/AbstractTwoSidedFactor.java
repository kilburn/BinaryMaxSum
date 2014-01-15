/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2013 Marc Pujol <mpujol@iiia.csic.es>.
 *
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 *
 *   Redistributions of source code must retain the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 *
 *   Neither the name of IIIA-CSIC, Artificial Intelligence Research Institute
 *   nor the names of its contributors may be used to
 *   endorse or promote products derived from this
 *   software without specific prior written permission of
 *   IIIA-CSIC, Artificial Intelligence Research Institute
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package es.csic.iiia.maxsum.factors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract class for two-sided factors. 
 * 
 * The neighbors in two-sided factors can be grouped in two sets (A and B). 
 * This class keeps the number of elements in the first set and provides methods 
 * to access the sorted messages for each set. 
 *  * 
 * @author Toni Penya-Alba <tonipenya@iiia.csic.es>
 */

public abstract class AbstractTwoSidedFactor<T> extends AbstractFactor<T> {

    protected int nElementsA = -1;
    protected long constraintChecks;

    /**
     * Set the number of elements in set A.
     * @param nElements number of elements in set A.
     */
    public void setNElementsA(int nElements) {
        nElementsA = nElements;
    }

    /**
     * Get a list of <neighbor, message value> pairs for the factors in set A.
     * The list is sorted by the value of the last message received.
     * 
     * @return <em>pair list</em> A list of <neighbor, message value> pairs for 
     * the factors in set A.
     */
    protected List<Pair> getSortedSetAPairs() {
        List<Pair> setAPairs = new ArrayList<Pair>(nElementsA);

        for (int i = 0; i < nElementsA; i++) {
            T neighbor = getNeighbors().get(i);
            setAPairs.add(new Pair(neighbor, getMessage(neighbor)));
        }
        constraintChecks += nElementsA;

        Collections.sort(setAPairs, Collections.reverseOrder());

        return setAPairs;
    }

    /**
     * Get a list of <neighbor, message value> pairs for the factors in set B.
     * The list is sorted by the value of the last message received.
     * 
     * @return <em>pair list</em> B list of <neighbor, message value> pairs for 
     * the factors in set B.
     */
    protected List<Pair> getSortedSetBPairs() {
        final int nNeighbors = getNeighbors().size();
        final int nElementsB = nNeighbors - nElementsA;
        List<Pair> setBPairs = new ArrayList<Pair>(nElementsB);

        for (int i = nElementsA; i < nNeighbors; i++) {
            T neighbor = getNeighbors().get(i);
            setBPairs.add(new Pair(neighbor, getMessage(neighbor)));
        }
        constraintChecks += nElementsB;

        Collections.sort(setBPairs, Collections.reverseOrder());

        return setBPairs;
    }

    /**
     * Class representing a pair <neighbor, value> implementing the Comparable 
     * interface. The comparison is made between the values.
     * 
     * @author Toni Penya-Alba <tonipenya@iiia.csic.es>
     *
     */
    protected class Pair implements Comparable<Pair> {
        public final T id;
        public final Double value;

        public Pair(T id, Double value) {
            this.id = id;
            this.value = value;
        }

        @Override
        public int compareTo(Pair p) {
            constraintChecks++;
            return getMaxOperator().compare(value, p.value);
        }

    }

}
