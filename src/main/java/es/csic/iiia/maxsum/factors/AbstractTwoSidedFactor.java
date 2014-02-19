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

import es.csic.iiia.maxsum.util.NeighborComparator;
import es.csic.iiia.maxsum.util.NeighborValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import es.csic.iiia.maxsum.util.NeighborComparator;
import es.csic.iiia.maxsum.util.NeighborValue;

/**
 * Abstract class for two-sided factors.
 *
 * The neighbors in two-sided factors can be grouped in two sets (A and B).
 * This class keeps the number of elements in the first set and provides methods
 * to access the sorted messages for each set.
 *
 * @author Toni Penya-Alba <tonipenya@iiia.csic.es>
 * @param <T> Type of the factor's identity.
 */
public abstract class AbstractTwoSidedFactor<T> extends AbstractFactor<T> {

    /**
     * Number of elements in set A.
     */
    protected int nElementsA = 0;

    /**
     * Accumulator of constraint checks
     */
    protected long constraintChecks;

    /**
     * Get the number of elements in set A.
     *
     * @return The number of elements in set A.
     */
    public int getNElementsA() {
        return nElementsA;
    }

    /**
     * Set the number of elements in set A.
     * @param nElements number of elements in set A.
     */
    public void setNElementsA(int nElements) {
        nElementsA = nElements;
    }

    /**
     * Adds a new neighbor of this factor (graph link). The added neighbor is of
     * the set A.
     *
     * @param factor new neighbor.
     */
    public void addANeighbor(T factor) {
        getNeighbors().add(nElementsA, factor);
        receive(0d, factor);
        nElementsA++;
    }

    /**
     * Adds a new neighbor of this factor (graph link). The added neighbor is of
     * the set A.
     *
     * @param factor new neighbor.
     */
    public void addBNeighbor(T factor) {
        super.addNeighbor(factor);
    }

    @Override
    public boolean removeNeighbor(T factor) {
        final int nNeighbors = getNeighbors().size();
        int index = 0;

        while(index < nNeighbors && getNeighbors().get(index) != factor) {
            index++;
        }

        boolean isInA = index >= 0 && index < nElementsA;
        if (isInA) {
            nElementsA--;
        }

        return super.removeNeighbor(factor);
    }

    /**
     * Get a list of <neighbor, message value> pairs for the factors in set A.
     * The list is sorted by the value of the last message received.
     *
     * @return <em>pair list</em> A list of <neighbor, message value> pairs for
     * the factors in set A.
     */
    protected List<NeighborValue<T>> getSortedSetAPairs() {
        List<NeighborValue<T>> setAPairs = new ArrayList<NeighborValue<T>>(nElementsA);

        for (int i = 0; i < nElementsA; i++) {
            T neighbor = getNeighbors().get(i);
            setAPairs.add(new NeighborValue<T>(neighbor, getMessage(neighbor)));
        }
        constraintChecks += nElementsA;

        NeighborComparator<T> cmp = new NeighborComparator<T>(getMaxOperator());
        Collections.sort(setAPairs, Collections.reverseOrder(cmp));
        constraintChecks += cmp.getConstraintChecks();

        return setAPairs;
    }

    /**
     * Get a list of <neighbor, message value> pairs for the factors in set B.
     * The list is sorted by the value of the last message received.
     *
     * @return <em>pair list</em> B list of <neighbor, message value> pairs for
     * the factors in set B.
     */
    protected List<NeighborValue<T>> getSortedSetBPairs() {
        final int nNeighbors = getNeighbors().size();
        final int nElementsB = nNeighbors - nElementsA;
        List<NeighborValue<T>> setBPairs = new ArrayList<NeighborValue<T>>(nElementsB);

        for (int i = nElementsA; i < nNeighbors; i++) {
            T neighbor = getNeighbors().get(i);
            setBPairs.add(new NeighborValue<T>(neighbor, getMessage(neighbor)));
        }
        constraintChecks += nElementsB;

        NeighborComparator<T> cmp = new NeighborComparator<T>(getMaxOperator());
        Collections.sort(setBPairs, Collections.reverseOrder(cmp));
        constraintChecks += cmp.getConstraintChecks();

        return setBPairs;
    }

    /**
     * Get the difference between the number of active neighbors in set A and
     * the number of active neighbors in set B.
     * That is, |active(A)| - |active(B)|.
     *
     * @param values a map <neighbor, boolean> describing the active state of
     *  each neighbor.
     * @return <em>reserve</em> the difference between the number of active
     *  neighbors in sets A and B.
     */
    protected int getReserve(Map<T, Boolean> values) {
        int reserve = 0;

        for(int i = 0; i < nElementsA; i++) {
            if (values.get(getNeighbors().get(i))) {
                reserve++;
            }
        }

        final int nNeighbors = getNeighbors().size();
        for(int i = nElementsA; i < nNeighbors; i++) {
            if (values.get(getNeighbors().get(i))) {
                reserve--;
            }
        }

        return reserve;
    }

}
