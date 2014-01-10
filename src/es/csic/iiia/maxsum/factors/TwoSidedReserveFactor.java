package es.csic.iiia.maxsum.factors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.csic.iiia.maxsum.MaxOperator;
import es.csic.iiia.maxsum.Maximize;

/**
 * Max-sum "Two-sided reserve" factor.
 * 
 * Given two sets of neighbors A = {a_1, ..., a_p} and B = {b_1, ..., b_q}, this
 * factor tries to ensure that there are at least the same number of neighbors
 * from the first set chosen than from the second one. That is, f(a_1, ..., a_p,
 * b_1, ..., b_q) = 0 if (\sum_i a_i) >= (\sum_j b_j) or (-)\infty otherwise.
 * <p/>
 * Outgoing messages are computed in <em>O(n log(n))</em> time. Where <em>n</em>
 * is the number of elements in the biggest set. That is, n = max(p, q).
 * 
 * @author Toni Penya-Alba <tonipenya@iiia.csic.es>
 */

public class TwoSidedReserveFactor<T> extends AbstractFactor<T> {

    private int nElementsA = -1;
    private long constraintChecks;

    public void setNElementsA(int nElements) {
        nElementsA = nElements;
    }

    @Override
    protected long iter() {
        final MaxOperator op = getMaxOperator();
        final int nNeighbors = getNeighbors().size();
        final int nElementsB = nNeighbors - nElementsA;
        constraintChecks = 0;

        if (nElementsA == 0) {
            for (T neigh : getNeighbors()) {
                send(op.getWorstValue(), neigh);
            }

            return nNeighbors;
        }

        // Optimization. If there are no elements in set B, messages for the 
        // members in set A is always 0.
        if (nElementsB == 0) {
            for (T neigh : getNeighbors()) {
                send(0.0, neigh);
            }

            return nNeighbors;
        }

        final List<Pair> setAPairs = getSortedSetAPairs();
        final List<Pair> setBPairs = getSortedSetBPairs();

        final int theta = getTheta(setAPairs, setBPairs);

        final double nuAtheta = (theta == 0) ? -op.getWorstValue() : setAPairs
                .get(theta - 1).value;
        final double nuAthetaPlus = (nElementsA > theta) ? setAPairs.get(theta).value
                : op.getWorstValue();
        final double nuBtheta = (theta == 0) ? -op.getWorstValue() : setBPairs
                .get(theta - 1).value;
        final double nuBTthetaPlus = (nElementsB > theta) ? setBPairs
                .get(theta).value : op.getWorstValue();

        final double A = op.max(nuAthetaPlus, -nuBtheta);
        final double B = op.max(0, op.max(nuBTthetaPlus, -nuAtheta));

        constraintChecks += 6;

        final int nPositiveA = getNPositive(setAPairs);
        if (nPositiveA > theta) {
            for (T neighbor : getNeighbors()) {
                send(0, neighbor);
            }
        } else {
            final int nActiveA = Math.max(theta, nPositiveA);
            // Send -A to active 'a's
            for (int i = 0; i < nActiveA; i++) {
                send(-A, setAPairs.get(i).id);
            }

            // Send B to inactive 'a's
            for (int i = nActiveA; i < nElementsA; i++) {
                send(B, setAPairs.get(i).id);
            }

            // Send -B to active 'b's
            for (int i = 0; i < theta; i++) {
                send(-B, setBPairs.get(i).id);
            }

            // Send A to inactive 'b's
            for (int i = theta; i < nElementsB; i++) {
                send(A, setBPairs.get(i).id);
            }
        }
        constraintChecks += nNeighbors;

        return constraintChecks;
    }

    private List<Pair> getSortedSetAPairs() {
        List<Pair> setAPairs = new ArrayList<Pair>(nElementsA);

        for (int i = 0; i < nElementsA; i++) {
            T neighbor = getNeighbors().get(i);
            setAPairs.add(new Pair(neighbor, getMessage(neighbor)));
        }
        constraintChecks += nElementsA;

        Collections.sort(setAPairs, Collections.reverseOrder());

        return setAPairs;
    }

    private List<Pair> getSortedSetBPairs() {
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

    private int getTheta(List<Pair> setAPairs, List<Pair> setBPairs) {
        final MaxOperator op = getMaxOperator();
        final int nElementsB = setBPairs.size();
        final int n = Math.min(nElementsA, nElementsB);

        int theta = 0;
        while (theta < n
                && op.compare(setBPairs.get(theta).value, 0) > 0
                && op.compare(setBPairs.get(theta).value
                        + setAPairs.get(theta).value, 0) >= 0) {
            theta++;
        }

        constraintChecks += theta * 3;

        return theta;
    }

    private int getNPositive(List<Pair> pairs) {
        final int pairsLength = pairs.size();
        final MaxOperator op = getMaxOperator();

        int nPositive = 0;
        while (nPositive < pairsLength
                && op.compare(pairs.get(nPositive).value, 0) >= 0) {
            nPositive++;
        }

        constraintChecks += nPositive;

        return nPositive;
    }

    private class Pair implements Comparable<Pair> {
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
