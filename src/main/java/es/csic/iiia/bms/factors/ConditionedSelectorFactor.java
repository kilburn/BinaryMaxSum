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
import es.csic.iiia.bms.util.BestValuesTracker;

import java.util.Map;

/**
 * Max-sum conditioned selector factor.
 *
 * This factor implements a conditional selector. There is one "conditioning" variable c and a set
 * of dependent variables x_i.
 * <p>
 * If c = 0, then the factor tries to ensure that \sum x_i = 0 <br>
 * If c = 1 ,then it tries to ensure that \sum x_i = 1
 *
 * <p/>
 * Outgoing messages are computed in <em>O(n)</em> time, where <em>n</em> is the
 * total number of variables connected to this factor.
 *
 * @param <T> Type of the factor's identity.
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class ConditionedSelectorFactor<T> extends AbstractFactor<T> {

    private BestValuesTracker<T> tracker;

    private T conditionNeighbor;

    @Override
    public void setMaxOperator(MaxOperator maxOperator) {
        super.setMaxOperator(maxOperator);
        tracker = new BestValuesTracker<T>(maxOperator);
    }

    /**
     * Get the neighbor that represents the conditional variable.
     *
     * @return the Neighbor that represents the conditional variable.
     */
    public T getConditionNeighbor() {
        return conditionNeighbor;
    }

    /**
     * Set the neighbor that represents the conditional variable.
     *
     * @param condition conditional variable.
     */
    public void setConditionNeighbor(T condition) {
        this.conditionNeighbor = condition;
    }

    @Override
    protected double eval(Map<T, Boolean> values) {
        int nActive = 0;
        for (T neighbor : getNeighbors()) {
            if (neighbor.equals(conditionNeighbor)) {
                continue;
            }
            if (values.get(neighbor)) {
                nActive++;
            }
        }

        boolean c = values.get(conditionNeighbor);
        if ((c && nActive == 1) || (!c && nActive == 0)) {
            return 0;
        }
        return getMaxOperator().getWorstValue();
    }

    @Override
    public long run() {

        // Compute the maximums between the dependent variables
        tracker.reset();
        for (T f : getNeighbors()) {
            // Skip the condition neighbor
            if (f.equals(conditionNeighbor)) {
                continue;
            }

            tracker.track(f, getMessage(f));
        }

        // Send messages
        for (T f : getNeighbors()) {
            if (f.equals(conditionNeighbor)) {
                sendMessageToConditionNeighbor(f);
            } else {
                sendMessageToDependentNeighbor(f);
            }
        }

        return getNeighbors().size()*2;
    }

    /**
     * Sends the message to the condition variable.
     *
     * For the "0" value, all other variables must be "0". Therefore, we only have two options,
     * - The case where \sum x_i = 0 has a value of "0" (because \mu_{x_i->f}(0) is 0 for all x_i).
     * - Otherwise, the value is -inf because it violates the constraint.
     * Hence:
     *      \mu_{f->c}(0) = max(0, -inf) = 0
     *
     * For the "1" value, one and only one of the other variables must be "1". Two options again:
     * - When \sum x_i = 0, the constraint is violated so we get -inf
     * - Otherwise, we choose the best (highest) message among the dependent variables.
     * Hence:
     *      \mu_{f->c}(1) = max(-inf, max_{x_i}(\mu_{x_i->f})) = max_{x_i}(\mu_{x_i->f})
     *
     * Finally, we combine both messages into a single-valued message:
     *
     *      \gamma_{f->c} = \mu_{f->c}(1) - \mu_{f->c}(0) = max_{x_i}(\mu_{x_i->f})
     *
     * @param neighbor the condition variable neighbor (c).
     */
    private void sendMessageToConditionNeighbor(T neighbor) {
        final double value = tracker.getBestValue();
        send(value, neighbor);
    }

    private void sendMessageToDependentNeighbor(T neighbor) {
        final double bestNeighbor = tracker.getComplementary(neighbor);
        final double value = -getMaxOperator().max(bestNeighbor, -getMessage(conditionNeighbor));
        send(value, neighbor);
    }

}
