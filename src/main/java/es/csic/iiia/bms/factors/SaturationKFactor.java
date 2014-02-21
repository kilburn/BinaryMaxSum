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
package es.csic.iiia.bms.factors;

import es.csic.iiia.bms.MaxOperator;
import es.csic.iiia.bms.util.BestKValuesTracker;

import java.util.Map;

/**
 * Factor that selects the best <em>k</em> active neighbor but doesn't constraint others.
 * <p/>
 * This is very similar to the {@link IndependentFactor}, but in this case the utility obtained by
 * the factor is that of only the best <em>k</em> active neighbors (instead of all of them).
 * Formally, this factor is defined over a sequence of neighbors <em>X = {x_1, ..., x_n}</em>, a
 * sequence of corresponding weights <em>B = {b_1, ..., b_n}, b_i > 0</ëm>, and a maximum number
 * of contributing neighbors <em>k</em>. Then, the factor is defined as
 * <pre>
 *      f(X,B,k) = max_{s \in {X \choose min(n_a, k)}} \sum_{x_i \in s} b_i,
 * </pre>
 * where <em>n_a = \sum x_i</em> is the number of active variables.
 *
 * Messages from this factor can be computed in <em>O(n log(k))</em> time. First, the maximization
 * cases are divided in two groups: those where <em>n_a <= k</em> and those where <em>n_a > k</em>.
 *
 * @param <T> Type of the factor's identity.
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class SaturationKFactor<T> extends IndependentFactor<T> {

    private final int k;

    /** Tracks the maximum gain from the max(b_i) part */
    private BestKValuesTracker<T> max_b;
    /** Tracks the maximum gain from the max(b_i + v_i) part (k elements) */
    private BestKValuesTracker<T> max_bv_0;
    /** Tracks the maximum gain from the max(b_i + v_i) part (k-1 elements) */
    private BestKValuesTracker<T> max_bv_1;
    /** Tracks the sum of positive messages */
    private double sum;

    /**
     * Build a new saturation factor, that gains utility from at most <em>k</em> active neighbors.
     *
     * @param k maximum number of neighbors that can contribute to this factor's utility (the
     *          saturation threshold)
     */
    public SaturationKFactor(int k) {
        this.k = k;
    }

    @Override
    public void setMaxOperator(MaxOperator maxOperator) {
        super.setMaxOperator(maxOperator);
        max_b = new BestKValuesTracker<T>(maxOperator, k);
        max_bv_0 = new BestKValuesTracker<T>(maxOperator, k-1);
        max_bv_1 = new BestKValuesTracker<T>(maxOperator, k-2);
    }

    /**
     * Maximum number of neighbors to select.
     *
     * @return maximum number of neighbors to select (the <em>k</em> value)
     */
    public int getK() {
        return k;
    }

    @Override
    protected double eval(Map<T, Boolean> values) {
        BestKValuesTracker<T> chosen = new BestKValuesTracker<T>(getMaxOperator(), k);

        // Pick all active neighbors (the TreeSet will evict the ones with lower utilities)
        for (T neighbor : getNeighbors()) {
            if (values.get(neighbor)) {
                chosen.track(neighbor, getPotential(neighbor));
            }
        }

        return chosen.sum();
    }

    // TODO: Track the number of constraint checks
    @Override
    public long run() {
        final MaxOperator max = getMaxOperator();
        max_b.reset();
        max_bv_0.reset();
        max_bv_1.reset();

        // Compute the maximum lists
        for (T neighbor : getNeighbors()) {
            final double b_i = getPotential(neighbor);
            final double v_i = getMessage(neighbor);

            final double v_i_negative = max.compare(v_i, 0) >= 0 ? 0 : v_i;
            max_b.track(neighbor, b_i + v_i_negative);

            final double bv_i = b_i + v_i;
            if (max.compare(bv_i, 0) >= 0) {
                max_bv_0.track(neighbor, bv_i);
                max_bv_1.track(neighbor, bv_i);
            }
            sum += max.max(v_i, 0);
        }

        // Compute messages
        for (T neighbor : getNeighbors()) {
            final double b_i = getPotential(neighbor);
            final double v_i = getMessage(neighbor);
            final double v_i_positive = max.max(v_i, 0);
            final double sum_i = sum - v_i_positive;
            final double v_i_negative = max.compare(v_i, 0) >= 0 ? 0 : v_i;

            final double m_0_lower = max_bv_0.sumComplementaries(neighbor, b_i + v_i, null);
            final double m_0_upper = max_b.sumComplementaries(neighbor, b_i + v_i_negative, null) + sum_i;
            final double m_0 = max.max(m_0_lower, m_0_upper);

            final double m_1_lower = max_bv_1.sumComplementaries(neighbor, b_i + v_i, null) + b_i;
            final double m_1_upper = max_b.sumComplementaries(neighbor, b_i + v_i_negative, b_i) + sum_i;
            final double m_1 = max.max(m_1_lower, m_1_upper);

            send(m_1 - m_0, neighbor);
        }

        return 0;
    }

}
