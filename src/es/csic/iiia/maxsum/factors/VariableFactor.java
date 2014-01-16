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

import java.util.List;
import java.util.Map;

/**
 * Factor defined over a single variable (variable node in classical MaxSum).
 *
 * @param <T> Type of the factor's identity.
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class VariableFactor<T> extends AbstractFactor<T> {

    @Override
    protected double eval(Map<T, Boolean> values) {
        final List<T> neighbors = getNeighbors();
        final int nNeighbors = neighbors.size();

        if (nNeighbors <= 1) {
            return 0;
        }

        final boolean value = values.get(neighbors.get(0));
        for (int i=1; i<nNeighbors; i++) {
            if (value != values.get(neighbors.get(i))) {
                return getMaxOperator().getWorstValue();
            }
        }

        return 0;
    }

    /**
     * Computes and sends the messages of this factor, using the formula:
     *
     * \nu_{n_i} =  [ \sum_{n_j \in N} \nu_{n_j} ] - \nu_{n_i}
     *
     * where N is the set of neighbors of this factor.
     *
     * @return number of Constraint Checks performed by this node.
     */
    @Override
    public long run() {
        double belief = 0;

        for (T f : getNeighbors()) {
            belief += getMessage(f);
        }

        // Send messages
        for (T f : getNeighbors()) {
            final double value = belief - getMessage(f);
            send(value, f);
        }

        return getNeighbors().size()*2;
    }

}
