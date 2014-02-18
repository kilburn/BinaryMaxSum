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
import es.csic.iiia.maxsum.factors.CardinalityFactor.CardinalityFunction;
import org.junit.Test;

import static org.mockito.AdditionalMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link es.csic.iiia.maxsum.factors.CardinalityFactor} class.
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class CardinalityFactorTest extends CrossFactorTestAbstract {

    @Test
    public void testRun1() {
        double[] values  = new double[]{0, 1, 2};
        double[] results = new double[]{-1, 0, 0};
        run(new Minimize(), values, results);

        results = new double[]{-2, -2, -1};
        run(new Maximize(), values, results);
    }

    @Test
    public void testRun2() {
        double[] values  = new double[]{0, 0, 2};
        double[] results = new double[]{0, 0, 0};
        run(new Minimize(), values, results);

        results = new double[]{-2, -2, 0};
        run(new Maximize(), values, results);
    }

    @Test
    public void testRun3() {
        double[] values  = new double[]{-1, 2};
        double[] results = new double[]{-2, 1};
        run(new Minimize(), values, results);

        results = new double[]{-2, 1};
        run(new Maximize(), values, results);
    }

    private void run(final MaxOperator op, double[] values, double[] results) {
        CommunicationAdapter com = mock(CommunicationAdapter.class);

        // Setup incoming messages
        CardinalityFactor[] cfs = new CardinalityFactor[values.length];
        CardinalityFactor s = new CardinalityFactor();
        s.setFunction(new CardinalityFunction() {
            @Override
            public double getCost(int nActiveVariables) {
                if (nActiveVariables != 1) {
                    return op.getWorstValue();
                }
                return 0;
            }
        });
        s.setCommunicationAdapter(com);
        s.setMaxOperator(op);
        s.setIdentity(s);

        for (int i=0; i<cfs.length; i++) {
            cfs[i] = new CardinalityFactor();
            s.addNeighbor(cfs[i]);
            s.receive(values[i], cfs[i]);
        }

        // This makes the factor run and send messages through the mocked com
        s.run();

        for (int i=0; i<cfs.length; i++) {
            verify(com).send(eq(results[i], Constants.DELTA), same(s), same(cfs[i]));
        }
    }

    @Override
    public Factor[] buildFactors(final MaxOperator op, Factor[] neighbors) {
        CardinalityFunction function = new RandomCardinalityFunction(neighbors.length);

        return new Factor[]{
            buildSpecificFactor(op, neighbors, function),
            buildStandardFactor(op, neighbors, function),
        };
    }

    private Factor buildSpecificFactor(MaxOperator op, Factor[] neighbors, CardinalityFunction function) {
        CardinalityFactor factor = new CardinalityFactor();
        factor.setMaxOperator(op);
        link(factor, neighbors);
        factor.setFunction(function);
        return factor;
    }

    private Factor buildStandardFactor(MaxOperator op, Factor[] neighbors, CardinalityFunction function) {
        StandardFactor factor = new StandardFactor();
        factor.setMaxOperator(op);
        link(factor, neighbors);
        factor.setPotential(buildPotential(neighbors, function));
        return factor;
    }

    private double[] buildPotential(Factor[] neighbors, CardinalityFunction function) {
        final int nNeighbors = neighbors.length;
        final int size = 1 << nNeighbors;
        double[] values = new double[size];

        for (int idx=0; idx<size; idx++) {
            int nActiveVariables = Integer.bitCount(idx);
            values[idx] = function.getCost(nActiveVariables);
        }

        return values;
    }

    /**
     * Cardinality function that returns some (fixed) random number for every different number
     * of active neighbors.
     */
    private class RandomCardinalityFunction implements CardinalityFunction {
        private double[] values;

        public RandomCardinalityFunction(int nNeighbors) {
            values = new double[nNeighbors+1];
            for (int i=0; i<nNeighbors+1; i++) {
                values[i] = getRandomValue();
            }
        }

        @Override
        public double getCost(int nActiveVariables) {
            return values[nActiveVariables];
        }
    }
}