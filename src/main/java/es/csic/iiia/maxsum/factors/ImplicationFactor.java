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
package es.csic.iiia.maxsum.factors;

import es.csic.iiia.maxsum.MaxOperator;

import java.util.List;
import java.util.Map;

/**
 * Max-sum "Implication" factor.
 * <p/>
 * Given an ordered set of neighbors {x_1, ..., x_n}, this factor tries to ensure
 * that if the i-th neighbor is active (x_i = 1) all the following neighbors must
 * be active. That is, f(x_1, ..., x_n) = 0 if x_i <= x_{i+1}, for all i in [1,n-1];
 * -Infinity, otherwise
 * <p/>
 * Outgoing messages are computed in <em>O(n)</em> time. Where <em>n</em>
 * is the number of neighbors.
 *
 * @param <T> Type of the factor's identity.
 * @author Toni Penya-Alba <tonipenya@iiia.csic.es>
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class ImplicationFactor<T> extends AbstractFactor<T> {

    @Override
    public long run() {
        final List<T> neighbors = getNeighbors();
        final int nNeighbors = neighbors.size();
        final MaxOperator op = getMaxOperator();
        final int[] phiZero = new int[nNeighbors];
        final double[] allActiveAcc = new double[nNeighbors + 1];  // The +1 is a trick to avoid out of range access in the for loop.

        double bestAcc = 0;
        phiZero[nNeighbors - 1] = nNeighbors;
        for (int i = nNeighbors - 1; i >= 0; i--) {
            final T neighbor = getNeighbors().get(i);
            allActiveAcc[i] = allActiveAcc[i + 1] + getMessage(neighbor);

            if (i > 0) {
                final boolean better = op.compare(allActiveAcc[i], bestAcc) >= 0;
                phiZero[i - 1] = better ? i : phiZero[i];
            }
            bestAcc = op.max(bestAcc, allActiveAcc[i]);
        }

        int phiOne = 0;
        double bestActive = op.getWorstValue();

        for (int i = 0; i < nNeighbors; i++) {
            final T neighbor = getNeighbors().get(i);

            if (op.compare(allActiveAcc[i], bestActive) > 0) {
                phiOne = i;
                bestActive = allActiveAcc[i];
            }

            double message = allActiveAcc[phiOne] - allActiveAcc[phiZero[i]] - getMessage(neighbor);
            send(message, neighbor);
        }

        return nNeighbors * 2;
    }

    @Override
    protected double eval(Map<T, Boolean> values) {
        boolean isAnyActive = false;

        for (T neighbor : getNeighbors()) {
            if (values.get(neighbor)) {
                isAnyActive = true;
            } else if (isAnyActive) {
                return getMaxOperator().getWorstValue();
            }
        }

        return 0;
    }
}
