/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2012 Marc Pujol <mpujol@iiia.csic.es>.
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
package es.csic.iiia.maxsum.util;

import es.csic.iiia.maxsum.MaxOperator;

import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to compute the two best objects among a couple of them.
 *
 * @param <T> Type of the elements to track.
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class BestKValuesTracker<T> {
    private static final Logger LOG = Logger.getLogger(BestKValuesTracker.class.getName());

    private final MaxOperator operator;
    private final BoundedTreeSet<NeighborValue<T>> elements;
    private final int k;

    // Cached data for faster operation
    private boolean precomputed;
    private double sum, bound_0, bound_1;

    /**
     * Build a new tracker of best values.
     *
     * @param operator maximization operator to use.
     * @param k number of best values to extract.
     */
    public BestKValuesTracker(MaxOperator operator, int k) {
        this.k = k;
        elements = new BoundedTreeSet<NeighborValue<T>>(k+1,
                Collections.reverseOrder(new NeighborComparator<T>(operator)));
        this.operator = operator;
    }

    /**
     * Cleanup all values tracked until now.
     */
    public void reset() {
        LOG.finest("Tracking start");
        elements.clear();
        precomputed = false;
    }

    private void preCompute() {
        sum = 0; Iterator<NeighborValue<T>> it = elements.iterator();
        for (int i=0; i<k && it.hasNext(); i++) {
            sum += it.next().value;
        }
        it = elements.descendingIterator();
        bound_1 = elements.size() > k && it.hasNext() ? it.next().value : Double.NaN;
        bound_0 = it.hasNext() ? it.next().value : Double.NaN;
        precomputed = true;
    }

    /**
     * Computes the sum of the k best tracked costs/utilities.
     *
     * @return sum of the k best tracked costs/utilities.
     */
    public double sum() {
        if (!precomputed) {
            preCompute();
        }

        return sum;
    }

    /**
     * Computes the sum of the k best tracked costs, excluding the cost of the provided element and
     * optionally including an extra (untracked) cost.
     *
     * If <code>extra</code> is not null, then this cost is treated as if it pertained to the
     * list of costs to consider, but without modifying the list of tracked costs.
     *
     * If the given <code>element</code>'s cost is <strong>not</strong> within the best k costs,
     * then the returned value is the sum of those best k costs. Otherwise, the result is the sum
     * of the k+1 best costs minus the given element's cost.
     *
     * @param element element to exclude from the sum.
     * @param value value of the excluded element.
     * @param extra optional additional element to consider.
     * @return sum of the best k costs, excluding the given element
     */
    public double sumComplementaries(T element, double value, Double extra) {
        if (!precomputed) {
            preCompute();
        }

        double sum_e = sum;
        double bound = bound_0;

        // This checks if the given element is one of the "k" best ones
        // (the second part of the if checks that it is not the k+1 best element)
        if (elements.contains(new NeighborValue<T>(element, value)) && value != bound_1) {
            // If it is, we adapt the sum to be that of the other k best elements
            sum_e -= value;
            sum_e += Double.isNaN(bound_1) ? 0 : bound_1;
            bound = bound_1;
        }

        // If an "extra" element to consider has been supplied _and_ it is better than the worst
        // among the selected ones, adapt the sum
        if (extra != null && (Double.isNaN(bound) || operator.compare(extra, bound) > 0)) {
            sum_e -= Double.isNaN(bound) ? 0 : bound;
            sum_e += extra;
        }

        return sum_e;
    }

    /**
     * Track an element and its associated value.
     *
     * @param element element to track.
     * @param value value of this element.
     */
    public void track(T element, double value) {
        LOG.log(Level.FINEST, "BestKValues tracking element {0} with value {1}",
                new Object[]{element, value});
        elements.add(new NeighborValue<T>(element, value));
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("Best[")
                .append(elements.capacity)
                .append("](");
        String separator = "";
        for (NeighborValue<T> e : elements) {
            buf.append(separator).append(e);
            separator = ", ";
        }
        buf.append(")");
        return buf.toString();
    }

}