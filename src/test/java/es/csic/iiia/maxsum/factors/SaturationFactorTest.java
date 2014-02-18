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

import static org.mockito.AdditionalMatchers.eq;
import static org.mockito.Mockito.*;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class SaturationFactorTest extends CrossFactorTestAbstract {

    @Test
    public void testRun1() {
        double[] potentials = new double[]{2, 1};
        double[] messages = new double[]{-1, 2};
        double[] results = new double[]{1, 0};
        run(new Maximize(), potentials, messages, results);

        results = new double[]{2, 0};
        run(new Minimize(), potentials, messages, results);
    }

    @Test
    public void testRun2() {
        double[] potentials = new double[]{-2, 9};
        double[] messages = new double[]{6, 3};
        double[] results = new double[]{0, 11};
        run(new Maximize(), potentials, messages, results);
    }

    private void run(MaxOperator op, double[] potentials, double[] inMessages, double[] results) {
        CommunicationAdapter com = mock(CommunicationAdapter.class);

        // Setup incoming messages
        Factor[] neighbors = new Factor[inMessages.length];

        SaturationFactor tested = new SaturationFactor();
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
        double[] weights = new double[neighbors.length];
        for (int i=0; i<neighbors.length; i++) {
            weights[i] = getRandomValue();
        }

        return new Factor[]{
            buildSpecificFactor(op, neighbors, weights),
            buildStandardFactor(op, neighbors, weights),
        };
    }

    private Factor buildSpecificFactor(MaxOperator op, Factor[] neighbors, double[] weights) {
        SaturationFactor factor = new SaturationFactor();
        factor.setMaxOperator(op);
        link(factor, neighbors);
        for (int i=0; i<neighbors.length; i++) {
            factor.setPotential(neighbors[i], weights[i]);
        }
        return factor;
    }

    private Factor buildStandardFactor(MaxOperator op, Factor[] neighbors, double[] weights) {
        StandardFactor factor = new StandardFactor();
        factor.setMaxOperator(op);
        link(factor, neighbors);
        factor.setPotential(buildPotential(op, neighbors, weights));
        return factor;
    }

    public double[] buildPotential(MaxOperator op, Factor[] neighbors, double[] weights) {
        final int nNeighbors = neighbors.length;
        final int size = 1 << nNeighbors;

        double[] values = new double[size];
        // Initialize all values to nogood except for the "all inactive" configuration, which has
        // 0 value because no variable will be active there.
        Arrays.fill(values, op.getWorstValue());
        values[0] = 0;

        for (int idx=0; idx<size; idx++) {
            int mask = 1;
            for (int n=0; n<nNeighbors; n++) {
                // Check if the variable is active in this index
                if ((idx & mask) != 0) {
                    // If it *is* active, check the max
                    values[idx] = op.max(values[idx], weights[nNeighbors-1-n]);
                }

                mask = mask << 1;
            }
        }

        return values;
    }

}