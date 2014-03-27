/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2013-2014 Marc Pujol <mpujol@iiia.csic.es>
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

import java.util.Map;

/**
 * Max-sum "Conditioned deactivation" Factor.
 * <p/>
 * Given a set of neighbors {x_1, ..., x_n} and an exemplar (x_e) among them,
 * this factor tries to ensure that none of the neighbors is active unless the
 * exemplar is active itself.
 * That is f(x_1, ..., x_n) = 0 if x_e = 1 or \sum x_i = 0; -Infinity, otherwise
 * <p/>
 * Outgoing messages are computed in <em>O(n)</em> time. Where <em>n</em>
 * is the number of neighbors.

 * @param <T> Type of the factor's identity.
 * @author Toni Penya-Alba <tonipenya@iiia.csic.es>
 */
public class ConditionedDeactivationFactor<T> extends AbstractFactor<T>{
    private T exemplar;
    @Override
    public long run() {
        final int nNeighbors = getNeighbors().size();
        final MaxOperator op = getMaxOperator();
        final double nonExemplarSum = getNonExemplarSum();
        final double exemplarMessage = getMessage(exemplar);

        for (T neighbor : getNeighbors()) {
            double value;
            if (neighbor.equals(exemplar)) {
                value = nonExemplarSum;
            } else {
                double a = exemplarMessage + nonExemplarSum - op.max(getMessage(neighbor), 0);
                value = a - op.max(0, a);
            }

            send(value, neighbor);
        }

        return nNeighbors * 2;
    }

    private double getNonExemplarSum() {
        final MaxOperator op = getMaxOperator();
        double sum = 0;
        for (T neighbor : getNeighbors()) {
            if (neighbor.equals(exemplar)) {
                continue;
            }

            sum += op.max(0, getMessage(neighbor));
        }

        return sum;
    }

    @Override
    protected double eval(Map<T, Boolean> values) {
        if (values.get(exemplar)) {
            return 0;
        }

        for (T neighbor : getNeighbors()) {
            if (values.get(neighbor)) {
                return getMaxOperator().getWorstValue();
            }
        }

        return 0;
    }

    public T getExemplar() {
        return exemplar;
    }

    public void setExemplar(T exemplar) {
        this.exemplar = exemplar;
    }
}
