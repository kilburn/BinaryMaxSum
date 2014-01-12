package es.csic.iiia.maxsum.factors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.csic.iiia.maxsum.MaxOperator;

/**
 * Max-sum "Two-sided equality" factor.
 * 
 * Given two sets of neighbors A = {a_1, ..., a_p} and B = {b_1, ..., b_q}, this
 * factor tries to ensure that there are the same number of neighbors from the
 * first set chosen than from the second one. That is, f(a_1, ..., a_p, b_1,
 * ..., b_q) = 0 if (\sum_i a_i) = (\sum_j b_j) or (-)\infty otherwise.
 * <p/>
 * Outgoing messages are computed in <em>O(n log(n))</em> time. Where <em>n</em>
 * is the number of elements in the biggest set. That is, n = max(p, q).
 * 
 * @author Toni Penya-Alba <tonipenya@iiia.csic.es>
 */

public class TwoSidedEqualityFactor<T> extends AbstractFactor<T> {

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

        if (nElementsA == 0 || nElementsB == 0) {
            for (T neigh : getNeighbors()) {
                send(op.getWorstValue(), neigh);
            }

            return nNeighbors;
        }
        
        final List<Pair> setAPairs = getSortedSetAPairs();
        final List<Pair> setBPairs = getSortedSetBPairs();

        final int eta = getEta(setAPairs, setBPairs);

        final double nuAEta = (eta == 0) ? -op.getWorstValue() : setAPairs.get(eta-1).value; 
        final double nuAEtaPlusOne = (nElementsA > eta) ? setAPairs.get(eta).value : op.getWorstValue(); 
        final double nuBEta = (eta == 0) ? -op.getWorstValue() : setBPairs.get(eta-1).value; 
        final double nuBEtaPlusOne = (nElementsB > eta) ? setBPairs.get(eta).value : op.getWorstValue(); 
        
        final double tauPlus = -op.max(-nuBEta, nuAEtaPlusOne);
        final double tauMinus = op.max(-nuAEta, nuBEtaPlusOne);
        
        constraintChecks += 6;
        
        // active sellers
        for (int i = 0; i < eta; i++) {
            send(tauPlus, setAPairs.get(i).id);
        }

        // inactive sellers
        for (int i = eta; i < nElementsA; i++) {
            send(tauMinus, setAPairs.get(i).id);
        }

        // active buyers
        for (int i = 0; i < eta; i++) {
            send(-tauMinus, setBPairs.get(i).id);
        }

        // inactive buyers
        for (int i = eta; i < nElementsB; i++) {
            send(-tauPlus, setBPairs.get(i).id);
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

    private int getEta(List<Pair> setAPairs, List<Pair> setBPairs) {
        final MaxOperator op = getMaxOperator();
        final int nElementsB = setBPairs.size();
        final int n = Math.min(nElementsA, nElementsB);

        int eta = 0;
        while (eta < n
                && op.compare(
                        setBPairs.get(eta).value + setAPairs.get(eta).value, 0) >= 0) {
            eta++;
        }

        constraintChecks += eta * 3;

        return eta;
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
