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
public class AllActiveIncentiveFactorTest extends CrossFactorTestAbstract {

    @Test
    public void testRun1() {
        double[] values  = new double[]{10, -5};
        double[] results = new double[]{5, 0};
        run(new Minimize(), 20, values, results);

        results = new double[]{15, 20};
        run(new Maximize(), 20, values, results);
    }

    @Test
    public void testRun2() {
        double[] values  = new double[]{-2, -5, -7};
        double[] results = new double[]{5, 2, 2};
        run(new Minimize(), 20, values, results);

        results = new double[]{8, 11, 13};
        run(new Maximize(), 20, values, results);
    }

    @Test
    public void testRun3() {
        double[] values  = new double[]{5, 5, 7};
        double[] results = new double[]{-8, -8, -10};
        run(new Minimize(), -20, values, results);

        results = new double[]{-5, -5, -5};
        run(new Maximize(), -20, values, results);
    }

    private void run(MaxOperator op, double incentive, double[] values, double[] results) {
        CommunicationAdapter com = mock(CommunicationAdapter.class);

        // Setup incoming messages
        CardinalityFactor[] cfs = new CardinalityFactor[values.length];
        AllActiveIncentiveFactor s = new AllActiveIncentiveFactor();
        s.setIncentive(incentive);
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
    public Factor[] buildFactors(MaxOperator op, Factor[] neighbors) {
        double incentive = getRandomValue();

        return new Factor[]{
            buildSpecificFactor(op, neighbors, incentive),
            buildStandardFactor(op, neighbors, incentive),
        };
    }

    private Factor buildSpecificFactor(MaxOperator op, Factor[] neighbors, double incentive) {
        AllActiveIncentiveFactor factor = new AllActiveIncentiveFactor();
        factor.setMaxOperator(op);
        link(factor, neighbors);
        factor.setIncentive(incentive);
        return factor;
    }

    private Factor buildStandardFactor(MaxOperator op, Factor[] neighbors, double incentive) {
        StandardFactor factor = new StandardFactor();
        factor.setMaxOperator(op);
        link(factor, neighbors);
        factor.setPotential(buildPotential(neighbors, incentive));
        return factor;
    }

    @SuppressWarnings("WeakerAccess")
    public double[] buildPotential(Factor[] neighbors, double incentive) {
        final int nNeighbors = neighbors.length;

        // Initialize the cost/utilities array with "no goods"
        double[] values = new double[1 << nNeighbors];
        Arrays.fill(values, 0);

        // Incentivize the "all ones" configuration, which is always the last one
        values[values.length-1] = incentive;

        return values;
    }

}