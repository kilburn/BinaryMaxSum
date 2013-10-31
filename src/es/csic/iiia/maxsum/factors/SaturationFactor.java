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

import es.csic.iiia.maxsum.MaxOperator;
import es.csic.iiia.maxsum.Maximize;
import es.csic.iiia.maxsum.Minimize;
import es.csic.iiia.maxsum.util.BestValuesTracker;
import java.util.HashMap;
import java.util.Map;

/**
 * Factor that selects the best active neighbor but doesn't constraint others. This is very similar
 * to the {@link SelectorFactor}, but in this case other neighbors are allowed to be active
 * at the same time (without an increase in utility).
 * <p/>
 * Formally, this factor is defined over a sequence of neighbors <em>X = {x_1, ..., x_n}</em> and a
 * sequence of corresponding weights <em>B = {b_1, ..., b_n}, b_i > 0</ëm>. Then, the factor is
 * defined as
 * <pre>
 *      f(X,B) = max_{i=1..n} b_i*x_i
 * </pre>
 *
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class SaturationFactor<T> extends AbstractFactor<T> {

    private Map<T, Double> potential = new HashMap<T, Double>();

    @Override
    public void setMaxOperator(MaxOperator maxOperator) {
        super.setMaxOperator(maxOperator);
    }

    /**
     * Remove all potential costs.
     */
    public void clearPotentials() {
        potential.clear();
    }

    /**
     * Get the cost/utility of activating the variable shared with the given
     * neighbor.
     *
     * @param neighbor neighbor to consider
     * @return cost of activating the given neighbor
     */
    public double getPotential(T neighbor) {
        if (!potential.containsKey(neighbor)) {
            throw new IllegalArgumentException("Requested potential for a non-existant neighbor");
        }
        return potential.get(neighbor);
    }

    /**
     * Remove the cost/utility associated to activating the given factor.
     *
     * @param f factor to consider
     * @return previous cost of activating the given factor
     */
    public Double removePotential(T f) {
        return potential.remove(f);
    }

    /**
     * Set the cost/utility of activating the variable that corresponds to the given neighbor.
     *
     * @param neighbor neighbor with which this one shares a binary variable
     * @param value cost/utility of activating this neighbor
     */
    public void setPotential(T factor, double value) {
        potential.put(factor, value);
    }

    @Override
    protected long iter() {

        for (T neighbor : getNeighbors()) {
            sendMessage(neighbor);
        }

        return 0;
    }

    private void sendMessage(T neighborToSendTo) {
        final MaxOperator max = getMaxOperator();

        double M_i_positive = max.getWorstValue();
        double M_i_negative = max.getWorstValue();

        double sum_positive = 0;

        for (T neighbor : getNeighbors()) {
            // Skip the neighbor to which the message will be sent
            if (neighbor == neighborToSendTo) continue;

            final double v_j = getMessage(neighbor);
            final double b_j = getPotential(neighbor);
            sum_positive += max.max(v_j, 0);

            if (max.compare(v_j, 0) >= 0) {
                M_i_positive = max.max(M_i_positive, b_j);
            } else {
                M_i_negative = max.max(M_i_negative, b_j + v_j);
            }
        }

        final double b_i = getPotential(neighborToSendTo);
        final double mu_0 = max.max(0, sum_positive + max.max(M_i_positive, M_i_negative));
        final double mu_1 = sum_positive + max.max(b_i, max.max(M_i_positive, M_i_negative));
        send(mu_1 - mu_0, neighborToSendTo);
    }

}
