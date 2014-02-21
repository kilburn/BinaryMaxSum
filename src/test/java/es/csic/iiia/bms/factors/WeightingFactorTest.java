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

import es.csic.iiia.bms.CommunicationAdapter;
import es.csic.iiia.bms.Factor;
import es.csic.iiia.bms.MaxOperator;
import es.csic.iiia.bms.Minimize;
import es.csic.iiia.bms.factors.CardinalityFactor.CardinalityFunction;
import org.junit.Test;

import java.util.Arrays;

import static es.csic.iiia.bms.factors.Constants.DELTA;
import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.eq;
import static org.mockito.Mockito.*;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class WeightingFactorTest extends CrossFactorTestAbstract {

    private final static double K     = 1;
    private final static double ALPHA = 2;

    @Test
    public void testRun1() {
        double[] values     = new double[]{0, 1, 2};
        double[] potentials = new double[]{0, 0, 0};
        double[] results    = new double[]{1, 1, 1};
        run(new Minimize(), values, potentials, results);
    }

    @Test
    public void testRun2() {
        double[] values     = new double[]{3, 1, 2};
        double[] potentials = new double[]{0, 0, 0};
        double[] results    = new double[]{1, 1, 1};
        run(new Minimize(), values, potentials, results);
    }

    @Test
    public void testRun3() {
        double[] values     = new double[]{0, 1};
        double[] potentials = new double[]{3, 0};
        double[] results    = new double[]{4, 1};
        run(new Minimize(), values, potentials, results);
    }

    @Test
    public void testMessagesMaintainedWhenPotentialChanges() {
        WeightingFactor tested = new WeightingFactor(new StandardFactor());
        Factor neighbor = mock(Factor.class);
        tested.addNeighbor(neighbor);

        assertEquals(tested.getMessage(neighbor), 0, DELTA);
        tested.setPotential(neighbor, 1d);
        assertEquals(tested.getMessage(neighbor), 0, DELTA);
        tested.receive(1d, neighbor);
        assertEquals(tested.getMessage(neighbor), 1, DELTA);
        tested.setPotential(neighbor, 0);
        assertEquals(tested.getMessage(neighbor), 1, DELTA);
    }

    private void init(Factor f, MaxOperator op, CommunicationAdapter com) {
        f.setIdentity(f);
        f.setMaxOperator(op);
        f.setCommunicationAdapter(com);
    }

    private void run(MaxOperator op, double[] values, double[] potentials, double[] expected) {
        CommunicationAdapter com = mock(CommunicationAdapter.class);

        // Setup incoming messages
        Factor[] sfs = new Factor[values.length];

        // Build a factor that is made of independent potentials plus a
        // cardinality workload.
        CardinalityFactor cardinal = new CardinalityFactor();
        cardinal.setFunction(new CardinalityFunction() {
            @Override
            public double getCost(int nActiveVariables) {
                return K * Math.pow(nActiveVariables, ALPHA);
            }
        });
        WeightingFactor f = new WeightingFactor(cardinal);
        init(f, op, com);

        for (int i=0; i<sfs.length; i++) {
            sfs[i] = mock(Factor.class);
            sfs[i].setMaxOperator(op);
            sfs[i].setIdentity(sfs[i]);
            sfs[i].setCommunicationAdapter(com);

            f.addNeighbor(sfs[i]);
            f.setPotential(sfs[i], potentials[i]);
            f.receive(values[i], sfs[i]);
        }

        // This makes the factor run and send messages through the mocked com
        cardinal.run();

        // Check expectations
        for (int i=0; i<sfs.length; i++) {
            verify(com).send(eq(expected[i], DELTA), same(f.getIdentity()), same(sfs[i]));
        }
    }

    @Override
    public Factor[] buildFactors(final MaxOperator op, Factor[] neighbors) {
        double[] independentPotentials = buildIndependentPotentials(neighbors.length);

        return new Factor[]{
            buildSpecificFactor(op, neighbors, independentPotentials),
            buildStandardFactor(op, neighbors, independentPotentials),
        };
    }

    private double[] buildIndependentPotentials(int nNeighbors) {
        double[] values = new double[nNeighbors];
        for (int i=0; i<nNeighbors; i++) {
            values[i] = getRandomValue();
        }
        return values;
    }

    private WeightingFactor buildSpecificFactor(MaxOperator op, Factor[] neighbors, double[] independentPotentials) {
        WeightingFactor factor = new WeightingFactor(new SelectorFactor());
        factor.setMaxOperator(op);

        link(factor, neighbors);
        for (int i=0; i<neighbors.length; i++) {
            factor.setPotential(neighbors[i], independentPotentials[i]);
        }

        return factor;
    }

    private Factor buildStandardFactor(MaxOperator op, Factor[] neighbors, double[] independentPotentials) {
        StandardFactor factor = new StandardFactor();
        factor.setMaxOperator(op);
        link(factor, neighbors);
        factor.setPotential(buildPotential(op, neighbors, independentPotentials));
        return factor;
    }

    public double[] buildPotential(MaxOperator op, Factor[] neighbors, double[] independentPotentials) {
        final int nNeighbors = neighbors.length;

        // Initialize the cost/utilities array with "no goods"
        double[] values = new double[1 << nNeighbors];
        Arrays.fill(values, op.getWorstValue());

        // Now set the rows with exactly one variable active to the corresponding potential
        int idx = 1;
        for (int i=0; i<nNeighbors; i++) {
            values[idx] = independentPotentials[nNeighbors-1-i];
            idx = idx << 1;
        }

        return values;
    }
}