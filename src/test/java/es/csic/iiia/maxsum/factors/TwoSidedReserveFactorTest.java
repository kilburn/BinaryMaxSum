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
import es.csic.iiia.maxsum.MaxOperator;
import es.csic.iiia.maxsum.Maximize;
import org.junit.Test;

import static org.mockito.AdditionalMatchers.eq;
import static org.mockito.Mockito.*;

/**
 *
 * @author Toni Penya-Alba <tonipenya@iiia.csic.es>
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class TwoSidedReserveFactorTest extends TwoSidedFactorTestAbstract {

    @Test
    public void testRun1() {
        double[] values = new double[] { 3, 2, -1, 1, -1, -3 };
        double[] results = new double[] { 0, 0, 0, 0, 0, 0 };

        run(new Maximize(), values, results, 3);
    }

    @Test
    public void testRun2() {
        double[] values = new double[] { 2, -1, -1, 2, 1, -1 };
        double[] results = new double[] { 1, 1, 1, -1, -1, -1 };

        run(new Maximize(), values, results, 3);
    }

    @Test
    public void testRun3() {
        double[] values = new double[] { 1, -1, 1, -1 };
        double[] results = new double[] { 1, 0, 0, -1 };

        run(new Maximize(), values, results, 2);
    }

    @Test
    public void testRun4() {
        double[] values = new double[] { 6, 4, 8 };
        double[] results = new double[] { 8, -8, -4 };

        run(new Maximize(), values, results, 1);
    }

    @Test
    public void testRunEmptyA1() {
        double[] values = new double[] { 1, -1 };
        double[] results = new double[] { 0, 0 };

        run(new Maximize(), values, results, 2);
    }

    @Test
    public void testRunEmptyA2() {
        double[] values = new double[] { -1, -2 };
        double[] results = new double[] { 0, 0 };

        run(new Maximize(), values, results, 2);
    }

    @Test
    public void testRunEmptyA3() {
        double[] values = new double[] { 1, 2 };
        double[] results = new double[] { 0, 0 };

        run(new Maximize(), values, results, 2);
    }

    @Test
    public void testRunEmptyB1() {
        MaxOperator op = new Maximize();

        double[] values = new double[] { -1, 1 };
        double[] results = new double[] { op.getWorstValue(),
                op.getWorstValue() };

        run(op, values, results, 0);
    }

    @Test
    public void testRunEmptyB2() {
        MaxOperator op = new Maximize();

        double[] values = new double[] { -1, -2 };
        double[] results = new double[] { op.getWorstValue(),
                op.getWorstValue() };

        run(new Maximize(), values, results, 0);
    }

    @Test
    public void testRunEmptyB3() {
        MaxOperator op = new Maximize();

        double[] values = new double[] { 1, 2 };
        double[] results = new double[] { op.getWorstValue(),
                op.getWorstValue() };

        run(op, values, results, 0);
    }

    @Override
    protected AbstractTwoSidedFactor createFactor() {
        return new TwoSidedEqualityFactor();
    }

    private void run(MaxOperator op, double[] values, double[] results,
            int nElementsA) {
        CommunicationAdapter com = mock(CommunicationAdapter.class);

        // Setup incoming messages
        Factor[] cfs = new Factor[values.length];
        TwoSidedReserveFactor s = new TwoSidedReserveFactor();
        s.setNElementsA(nElementsA);
        s.setCommunicationAdapter(com);
        s.setMaxOperator(op);
        s.setIdentity(s);

        for (int i = 0; i < cfs.length; i++) {
            cfs[i] = mock(Factor.class);
            s.addNeighbor(cfs[i]);
            s.receive(values[i], cfs[i]);
        }
        // This makes the factor run and send messages through the mocked com
        s.run();

        for (int i = 0; i < cfs.length; i++) {
            verify(com).send(eq(results[i], Constants.DELTA), same(s), same(cfs[i]));
        }
    }

    @Override
    public Factor[] buildFactors(MaxOperator op, Factor[] neighbors) {
        int nElementsA = getRandomIntValue(neighbors.length);

        return new Factor[] { buildSpecificFactor(op, neighbors, nElementsA),
                buildStandardFactor(op, neighbors, nElementsA), };
    }

    private Factor buildSpecificFactor(MaxOperator op, Factor[] neighbors,
            int nElementsA) {
        TwoSidedReserveFactor factor = new TwoSidedReserveFactor();
        factor.setNElementsA(nElementsA);
        factor.setMaxOperator(op);
        link(factor, neighbors);
        return factor;
    }

    private Factor buildStandardFactor(MaxOperator op, Factor[] neighbors,
            int nElementsA) {
        StandardFactor factor = new StandardFactor();
        factor.setMaxOperator(op);
        link(factor, neighbors);
        factor.setPotential(buildPotential(op, neighbors, nElementsA));
        return factor;
    }

    public double[] buildPotential(MaxOperator op, Factor[] neighbors,
            int nElementsA) {
        final int nNeighbors = neighbors.length;
        final int nElementsB = nNeighbors - nElementsA;

        double[] values = new double[1 << nNeighbors];

        for (int configurationIdx = 0; configurationIdx < values.length; configurationIdx++) {
            int reserve = 0;
            for (int neighIdx = 0; neighIdx < nNeighbors; neighIdx++) {
                int mask = 1 << neighIdx;
                if ((configurationIdx & mask) > 0) {
                    reserve += (neighIdx < nElementsB) ? -1 : 1;
                }
            }

            values[configurationIdx] = (reserve < 0) ? op.getWorstValue() : 0;
        }

        return values;
    }

}