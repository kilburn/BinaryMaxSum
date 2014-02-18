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
import es.csic.iiia.maxsum.util.BestValuesTracker;

import java.util.Map;

/**
 * Max-sum "at most one" factor.
 *
 * This factor tries to ensure that at most one of the neighbors is chosen.
 * That is, f(x_1, ..., x_n) = 0 if (\sum_i x_i) <=1 or (-)\infty otherwise.
 * <p/>
 * Outgoing messages are computed in <em>O(n)</em> time, where <em>n</em> is the
 * total number of variables connected to this factor.
 *
 * @param <T> Type of the factor's identity.
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class AtMostOneFactor<T> extends AbstractFactor<T> {

    private BestValuesTracker<T> tracker;

    @Override
    public void setMaxOperator(MaxOperator maxOperator) {
        super.setMaxOperator(maxOperator);
        tracker = new BestValuesTracker<T>(maxOperator);
    }

    @Override
    protected double eval(Map<T, Boolean> values) {
        int nActive = 0;
        for (T neighbor : getNeighbors()) {
            if (values.get(neighbor)) {
                nActive++;
            }
            if (nActive > 1) {
                return getMaxOperator().getWorstValue();
            }
        }
        return 0;
    }

    @Override
    public long run() {
        // Compute the minimums
        tracker.reset();
        for (T f : getNeighbors()) {
            tracker.track(f, getMessage(f));
        }

        // Send messages
        for (T f : getNeighbors()) {
            final double value = - getMaxOperator().max(
                    0, tracker.getComplementary(f));
            send(value, f);
        }

        return getNeighbors().size()*2;
    }

    /**
     * Pick the "winning" neighboring factor.
     *
     * @return the best fitting neighbor factor.
     */
    public T select() {
        double bestChoiceValue = tracker.getBestValue();

        // Check whether to pick the best choice, or do nothing
        if (getMaxOperator().max(bestChoiceValue, 0) == bestChoiceValue) {
            return tracker.getBest();
        }

        // Do nothing
        return null;
    }

}
