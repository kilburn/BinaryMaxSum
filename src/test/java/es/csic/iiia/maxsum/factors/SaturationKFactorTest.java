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

import es.csic.iiia.maxsum.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.AdditionalMatchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests the SaturationKFactor implementation.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class SaturationKFactorTest extends CrossFactorTestAbstract {
    private static final Logger LOG = Logger.getLogger(SaturationKFactorTest.class.getName());

    @Test
    public void testRun1() {
        double[] potentials = new double[]{17, 13};
        double[] messages = new double[]{59, 96};
        double[] results = new double[]{17, 13};
        run(new Maximize(), 2, potentials, messages, results);
    }

    @Test
    public void testRun2() {
        double[] potentials = new double[]{2, 1};
        double[] messages = new double[]{-1, 2};
        double[] results = new double[]{1, 0};
        run(new Maximize(), 1, potentials, messages, results);

        results = new double[]{2, 0};
        run(new Minimize(), 1, potentials, messages, results);
    }

    @Test
    public void testRun3() {
        double[] potentials = new double[]{86};
        double[] messages = new double[]{93};
        double[] results = new double[]{86};
        run(new Minimize(), 1, potentials, messages, results);
    }

    private void run(MaxOperator op, int k, double[] potentials, double[] inMessages, double[] results) {
        CommunicationAdapter com = mock(CommunicationAdapter.class);

        // Setup incoming messages
        Factor[] neighbors = new Factor[inMessages.length];

        SaturationKFactor tested = new SaturationKFactor(k);
        tested.setCommunicationAdapter(com);
        tested.setMaxOperator(op);
        tested.setIdentity(tested);

        for (int i=0; i<neighbors.length; i++) {
            neighbors[i] = mock(Factor.class);
            tested.addNeighbor(neighbors[i]);
            tested.setPotential(neighbors[i], potentials[i]);
            tested.receive(inMessages[i], neighbors[i]);
        }

        // This makes the factor run and send messages through the mocked com
        tested.run();

        for (int i=0; i<neighbors.length; i++) {
            verify(com).send(eq(results[i], Constants.DELTA), same(tested), same(neighbors[i]));
        }
    }

    @Override
    public Factor[] buildFactors(MaxOperator op, Factor[] neighbors) {
        int k = getRandomIntValue(neighbors.length) + 1;

        double[] weights = new double[neighbors.length];
        for (int i=0; i<neighbors.length; i++) {
            weights[i] = getRandomValue();
        }

        LOG.log(Level.FINEST, "Op={0}, k={1}, weights={2}", new Object[]{op, k, Arrays.toString(weights)});

        return new Factor[]{
            buildSpecificFactor(op, neighbors, k, weights),
            buildStandardFactor(op, neighbors, k, weights),
        };
    }

    private Factor buildSpecificFactor(MaxOperator op, Factor[] neighbors, int k, double[] weights) {
        SaturationKFactor factor = new SaturationKFactor(k);
        factor.setMaxOperator(op);
        link(factor, neighbors);
        for (int i=0; i<neighbors.length; i++) {
            factor.setPotential(neighbors[i], weights[i]);
        }
        return factor;
    }

    private Factor buildStandardFactor(MaxOperator op, Factor[] neighbors, int k, double[] weights) {
        StandardFactor factor = new StandardFactor();
        factor.setMaxOperator(op);
        link(factor, neighbors);
        factor.setPotential(buildPotential(op, neighbors, k, weights));
        return factor;
    }

    public double[] buildPotential(MaxOperator op, Factor[] neighbors, int k, double[] weights) {
        final int nNeighbors = neighbors.length;
        final int size = 1 << nNeighbors;

        double[] values = new double[size];
        Arrays.fill(values, 0);

        PriorityQueue<Double> queue = new PriorityQueue<Double>(nNeighbors, new MaxOperatorComparator(op));
        for (int idx=0; idx<size; idx++) {
            int mask = 1;
            queue.clear();

            for (int n=0; n<nNeighbors; n++) {
                // Check if the variable is active in this index
                if ((idx & mask) != 0) {
                    // If it *is* active, add it to the queue
                    queue.add(weights[nNeighbors-1-n]);
                }

                mask = mask << 1;
            }

            // Extract and sum the maximum k
            for (int i=0; i<k && !queue.isEmpty(); i++) {
                values[idx] += queue.poll();
            }
        }

        return values;
    }

    private class MaxOperatorComparator implements Comparator<Double> {
        private final MaxOperator operator;

        public MaxOperatorComparator(MaxOperator op) {
            this.operator = op;
        }

        @Override
        public int compare(Double o1, Double o2) {
            return -operator.compare(o1, o2);
        }

    }

}