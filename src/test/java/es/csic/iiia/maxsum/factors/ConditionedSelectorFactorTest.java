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
public class ConditionedSelectorFactorTest extends CrossFactorTestAbstract {

    @Test
    public void testRun1() {
        double[] values = new double[] { 3, -1, 2};
        double[] results = new double[] { 2, -2, 1 };

        run(new Maximize(), values, results, 0);
    }

    private void run(MaxOperator op, double[] values, double[] results, int conditionNeighborIdx) {
        CommunicationAdapter com = mock(CommunicationAdapter.class);

        // Setup incoming messages
        Factor[] cfs = new Factor[values.length];
        ConditionedSelectorFactor s = new ConditionedSelectorFactor();
        s.setCommunicationAdapter(com);
        s.setMaxOperator(op);
        s.setIdentity(s);

        for (int i = 0; i < cfs.length; i++) {
            cfs[i] = mock(Factor.class);
            s.addNeighbor(cfs[i]);
            s.receive(values[i], cfs[i]);
        }

        s.setConditionNeighbor(cfs[conditionNeighborIdx]);

        // This makes the factor run and send messages through the mocked com
        s.run();

        for (int i = 0; i < cfs.length; i++) {
            verify(com).send(eq(results[i], Constants.DELTA), same(s), same(cfs[i]));
        }
    }

    @Override
    public Factor[] buildFactors(MaxOperator op, Factor[] neighbors) {
        Factor conditionNeighbor =  neighbors[getRandomIntValue(neighbors.length)];
        return new Factor[] { buildSpecificFactor(op, neighbors, conditionNeighbor),
                buildStandardFactor(op, neighbors, conditionNeighbor)};
    }

    private Factor buildSpecificFactor(MaxOperator op, Factor[] neighbors, Factor conditionNeighbor) {
        ConditionedSelectorFactor factor = new ConditionedSelectorFactor();
        factor.setMaxOperator(op);
        factor.setConditionNeighbor(conditionNeighbor);
        link(factor, neighbors);
        return factor;
    }

    private Factor buildStandardFactor(MaxOperator op, Factor[] neighbors, Factor conditionNeighbor) {
        StandardFactor factor = new StandardFactor();
        factor.setMaxOperator(op);
        link(factor, neighbors);
        factor.setPotential(buildPotential(op, neighbors, conditionNeighbor));
        return factor;
    }

    public double[] buildPotential(MaxOperator op, Factor[] neighbors, Factor conditionNeighbor) {
        final int nNeighbors = neighbors.length;
        int conditionIdx = -1;
        for (int i = 0; i < nNeighbors; i++) {
            if (neighbors[i] == conditionNeighbor) {
                conditionIdx = nNeighbors - i -1;
                break;
            }
        }

        double[] values = new double[1 << nNeighbors];

        for (int configurationIdx = 0; configurationIdx < values.length; configurationIdx++) {
            final int bitCount = Integer.bitCount(configurationIdx);

            if (bitCount == 0) {
                // condition == 0, dependents == 0
                values[configurationIdx] = 0;
            } else if (bitCount == 2
                    && ((1 << conditionIdx) & configurationIdx) > 0) {
                // condition == 1, one dependent == 1
                values[configurationIdx] = 0;
            } else {
                values[configurationIdx] = op.getWorstValue();
            }
        }

        return values;
    }
}