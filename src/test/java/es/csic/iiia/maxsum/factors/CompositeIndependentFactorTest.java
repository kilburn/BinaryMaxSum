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

import java.util.Arrays;

import static org.junit.Assert.assertSame;
import static org.mockito.AdditionalMatchers.eq;
import static org.mockito.Mockito.*;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class CompositeIndependentFactorTest extends CrossFactorTestAbstract {

    public static final double K     = 1;
    public static final double ALPHA = 2;

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
    public void testRunSelectorAndWorkload() {
        final int N_ITERATIONS = 10;
        final MaxOperator operator = new Maximize();
        TickCommunicationAdapter com = new TickCommunicationAdapter();
        double[][] utilities = new double[][]{
            {0.017, 10.01},
            {0.1,   1.599},
        };
        int[] choices = new int[]{1, 0};

        final int nAgents  = utilities.length;
        final int nTargets = utilities[0].length;

        // Create the cardinality factors for each target
        CardinalityFunction f = new CardinalityFunction() {
            @Override
            public double getCost(int nActiveVariables) {
                return nActiveVariables > 1 ? operator.getWorstValue() : 0;
            }
        };
        CardinalityFactor[] cfs = new CardinalityFactor<?>[nTargets];
        for (int i = 0; i < nTargets; i++) {
            cfs[i] = new CardinalityFactor<Factor<?>>();
            cfs[i].setFunction(f);
            init(cfs[i], operator, com);
        }

        // Create a potential + selector for each agent
        SelectorFactor[] sfs = new SelectorFactor[nAgents];
        CompositeIndependentFactor[] ifs = new CompositeIndependentFactor[nAgents];
        for (int agent = 0; agent < nAgents; agent++) {
            sfs[agent] = new SelectorFactor<Factor>();
            IndependentFactor<Factor<?>> pot = new IndependentFactor<Factor<?>>();
            ifs[agent] = new CompositeIndependentFactor<Factor<?>>();
            init(ifs[agent], operator, com);
            ifs[agent].setIndependentFactor(pot);
            ifs[agent].setInnerFactor(sfs[agent]);

            // Set potentials and connect the factors
            for (int target = 0; target < nTargets; target++) {
                pot.setPotential(cfs[target], utilities[agent][target]);
                cfs[target].addNeighbor(ifs[agent]);
                ifs[agent].addNeighbor(cfs[target]);
            }
        }

        // Ok now everything is built. Let's rock it!
        for (int i = 0; i < N_ITERATIONS; i++) {
            for (int agent = 0; agent < nAgents; agent++) {
                ifs[agent].run();
            }
            for (int target = 0; target < nTargets; target++) {
                cfs[target].run();
            }
            com.tick();
        }

        // Show choices
        for (int agent = 0; agent < nAgents; agent++) {
            assertSame(cfs[choices[agent]], sfs[agent].select());
        }
    }

    private void init(Factor f, MaxOperator op, CommunicationAdapter com) {
        f.setIdentity(f);
        f.setMaxOperator(op);
        f.setCommunicationAdapter(com);
    }

    private void run(MaxOperator op, double[] values, double[] potentials, double[] expected) {
        CommunicationAdapter<Factor> com = mock(CommunicationAdapter.class);

        // Setup incoming messages
        SelectorFactor[] sfs = new SelectorFactor[values.length];

        // Build a factor that is made of independent potentials plus a
        // cardinality workload.
        CompositeIndependentFactor<Factor> c = new CompositeIndependentFactor<Factor>();
        IndependentFactor<Factor> potential = new IndependentFactor<Factor>();
        c.setIndependentFactor(potential);
        CardinalityFactor<Factor> cardinal = new CardinalityFactor<Factor>();
        cardinal.setFunction(new CardinalityFunction() {
            @Override
            public double getCost(int nActiveVariables) {
                return K * Math.pow(nActiveVariables, ALPHA);
            }
        });
        c.setInnerFactor(cardinal);

        c.setMaxOperator(op);
        c.setIdentity(c);
        c.setCommunicationAdapter(com);

        for (int i=0; i<sfs.length; i++) {
            sfs[i] = new SelectorFactor<Factor>();
            sfs[i].setMaxOperator(op);
            sfs[i].setIdentity(sfs[i]);
            sfs[i].setCommunicationAdapter(com);

            c.addNeighbor(sfs[i]);
            potential.setPotential(sfs[i], potentials[i]);
            c.receive(values[i], sfs[i]);
        }

        // This makes the factor run and send messages through the mocked com
        cardinal.run();

        // Check expectations
        for (int i=0; i<sfs.length; i++) {
            verify(com).send(eq(expected[i], Constants.DELTA), same(c), same(sfs[i]));
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

    private Factor buildSpecificFactor(MaxOperator op, Factor[] neighbors, double[] independentPotentials) {
        CompositeIndependentFactor factor = new CompositeIndependentFactor();
        factor.setMaxOperator(op);
        factor.setInnerFactor(new SelectorFactor());

        IndependentFactor independent = new IndependentFactor();
        link(factor, neighbors);
        for (int i=0; i<neighbors.length; i++) {
            independent.setPotential(neighbors[i], independentPotentials[i]);
        }
        factor.setIndependentFactor(independent);

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