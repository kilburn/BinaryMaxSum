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

import es.csic.iiia.maxsum.CommunicationAdapter;
import es.csic.iiia.maxsum.Factor;
import es.csic.iiia.maxsum.factors.cardinality.KAlphaFunction;
import es.csic.iiia.maxsum.MaxOperator;
import es.csic.iiia.maxsum.Minimize;
import java.util.Arrays;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalMatchers.eq;
import org.junit.Test;

/**
 *
 * @author Toni Penya-Alba <tonipenya@iiia.csic.es>
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class SingleWeightFactorTest extends CrossFactorTestAbstract {
    private static final Logger LOG = Logger.getLogger(WeightingFactorTest.class.getName());

    private final double DELTA = 0.0001d;
    private final double K     = 1;
    private final double ALPHA = 2;

    @Test
    public void testRun1() {
        double[] values     = new double[]{0, 1, 2};
        double potential = 0;
        double[] results    = new double[]{1, 1, 1};
        run(new Minimize(), values, potential, results);
    }

    @Test
    public void testRun2() {
        double[] values     = new double[]{3, 1, 2};
        double potential = 0;
        double[] results    = new double[]{1, 1, 1};
        run(new Minimize(), values, potential, results);
    }

    @Test
    public void testMessagesMaintainedWhenPotentialChanges() {
        SingleWeightFactor tested = new SingleWeightFactor(new StandardFactor());
        Factor[] neighbors = {mock(Factor.class), mock(Factor.class)};
        tested.addNeighbor(neighbors[0]);
        tested.addNeighbor(neighbors[1]);

        assertEquals(0d, tested.getMessage(neighbors[0]), DELTA);
        assertEquals(0d, tested.getMessage(neighbors[1]), DELTA);
        tested.setPotential(1d);
        assertEquals(0d, tested.getMessage(neighbors[0]), DELTA);
        assertEquals(0d, tested.getMessage(neighbors[1]), DELTA);
        tested.receive(1d, neighbors[0]);
        assertEquals(1d, tested.getMessage(neighbors[0]), DELTA);
        assertEquals(0d, tested.getMessage(neighbors[1]), DELTA);
        tested.receive(3d, neighbors[1]);
        assertEquals(1d, tested.getMessage(neighbors[0]), DELTA);
        assertEquals(3d, tested.getMessage(neighbors[1]), DELTA);
        tested.setPotential(5d);
        assertEquals(1d, tested.getMessage(neighbors[0]), DELTA);
        assertEquals(3d, tested.getMessage(neighbors[1]), DELTA);
    }

    private void init(Factor f, MaxOperator op, CommunicationAdapter com) {
        f.setIdentity(f);
        f.setMaxOperator(op);
        f.setCommunicationAdapter(com);
    }

    private void run(MaxOperator op, double[] values, double potential, double[] expected) {
        CommunicationAdapter com = mock(CommunicationAdapter.class);

        // Setup incoming messages
        Factor[] sfs = new Factor[values.length];

        // Build a factor that is made of independent potentials plus a
        // cardinality workload.
        CardinalityFactor cardinal = new CardinalityFactor();
        cardinal.setFunction(new KAlphaFunction(K, ALPHA));
        SingleWeightFactor f = new SingleWeightFactor(cardinal);
        f.setPotential(potential);
        init(f, op, com);

        for (int i=0; i<sfs.length; i++) {
            sfs[i] = mock(Factor.class);
            sfs[i].setMaxOperator(op);
            sfs[i].setIdentity(sfs[i]);
            sfs[i].setCommunicationAdapter(com);

            f.addNeighbor(sfs[i]);
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
        double potential = getRandomValue();

        return new Factor[]{
            buildSpecificFactor(op, neighbors, potential),
            buildStandardFactor(op, neighbors, potential),
        };
    }

    private Factor buildSpecificFactor(MaxOperator op, Factor[] neighbors, double potential) {
        SingleWeightFactor factor = new SingleWeightFactor(new SelectorFactor());
        factor.setMaxOperator(op);

        factor.setPotential(potential);
        link(factor, neighbors);

        return factor;
    }

    private Factor buildStandardFactor(MaxOperator op, Factor[] neighbors, double potential) {
        StandardFactor factor = new StandardFactor();
        factor.setMaxOperator(op);
        link(factor, neighbors);
        factor.setPotential(buildPotential(op, neighbors, potential));
        return factor;
    }

    public double[] buildPotential(MaxOperator op, Factor[] neighbors, double potential) {
        final int nNeighbors = neighbors.length;

        // Initialize the cost/utilites array with "no goods"
        double[] values = new double[1 << nNeighbors];
        Arrays.fill(values, op.getWorstValue());

        // Now set the rows with exactly one variable active to the corresponding potential
        int idx = 1;
        for (int i=0; i<nNeighbors; i++) {
            values[idx] = potential;
            idx = idx << 1;
        }

        return values;
    }
}