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

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.eq;
import static org.mockito.Mockito.*;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class AtMostOneFactorTest extends CrossFactorTestAbstract {

    @Test
    public void testRun1() {
        double[] values  = new double[]{0, 1, 2};
        double[] results = new double[]{0, 0, 0};
        run(new Minimize(), values, results, 0);

        results = new double[]{-2, -2, -1};
        run(new Maximize(), values, results, 2);
    }

    @Test
    public void testRun2() {
        double[] values  = new double[]{0, 0, 2};
        double[] results = new double[]{0, 0, 0};
        run(new Minimize(), values, results, 1);

        results = new double[]{-2, -2, 0};
        run(new Maximize(), values, results, 2);
    }

    @Test
    public void testRun3() {
        double[] values  = new double[]{-1, 2};
        double[] results = new double[]{0, 1};
        run(new Minimize(), values, results, 0);

        results = new double[]{-2, 0};
        run(new Maximize(), values, results, 1);
    }

    @Test
    public void testRun4() {
        double[] values  = new double[]{1, 2};
        double[] results = new double[]{0, 0};
        run(new Minimize(), values, results, -1);

        results = new double[]{-2, -1};
        run(new Maximize(), values, results, 1);
    }

    private void run(MaxOperator op, double[] values, double[] results,
            int choice)
    {
        CommunicationAdapter com = mock(CommunicationAdapter.class);

        // Setup incoming messages
        CardinalityFactor[] cfs = new CardinalityFactor[values.length];
        AtMostOneFactor s = new AtMostOneFactor();
        s.setCommunicationAdapter(com);
        s.setMaxOperator(op);
        s.setIdentity(s);


        for (int i=0; i<cfs.length; i++) {
            cfs[i] = new CardinalityFactor();
            s.addNeighbor(cfs[i]);
            s.receive(values[i], cfs[i]);
        }
        Object expectedChoice = choice >= 0 ? cfs[choice] : null;

        // This makes the factor run and send messages through the mocked com
        s.run();

        for (int i=0; i<cfs.length; i++) {
            verify(com).send(eq(results[i], Constants.DELTA), same(s), same(cfs[i]));
        }

        assertEquals(expectedChoice, s.select());
    }

    @Override
    public Factor[] buildFactors(MaxOperator op, Factor[] neighbors) {
        return new Factor[]{
            buildSpecificFactor(op, neighbors),
            buildStandardFactor(op, neighbors),
        };
    }

    private Factor buildSpecificFactor(MaxOperator op, Factor[] neighbors) {
        AtMostOneFactor factor = new AtMostOneFactor();
        factor.setMaxOperator(op);
        link(factor, neighbors);
        return factor;
    }

    private Factor buildStandardFactor(MaxOperator op, Factor[] neighbors) {
        StandardFactor factor = new StandardFactor();
        factor.setMaxOperator(op);
        link(factor, neighbors);
        factor.setPotential(buildPotential(op, neighbors));
        return factor;
    }

    public double[] buildPotential(MaxOperator op, Factor[] neighbors) {
        final int nNeighbors = neighbors.length;

        // Initialize the cost/utilities array with "no goods"
        double[] values = new double[1 << nNeighbors];
        Arrays.fill(values, op.getWorstValue());

        // Allow all variables to be inactive, which is always the first configuration
        values[0] = 0;

        // Now set the rows with exactly one variable active to "0"
        for (int idx=1; idx<values.length; idx <<= 1) {
            values[idx] = 0;
        }

        return values;
    }

}