/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2013-2014 Marc Pujol <mpujol@iiia.csic.es>
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

import es.csic.iiia.maxsum.Factor;
import es.csic.iiia.maxsum.MaxOperator;

/**
 * Tests for the independent factor.
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class IndependentFactorTest extends CrossFactorTestAbstract {

    @Override
    public Factor[] buildFactors(MaxOperator op, Factor[] neighbors) {
        double[] potential = new double[neighbors.length];
        for (int i=0; i<potential.length; i++) {
            potential[i] = getRandomValue()-.5;
        }

        return new Factor[]{
            buildSpecificFactor(potential, op, neighbors),
            buildStandardFactor(potential, op, neighbors),
        };
    }

    private Factor buildSpecificFactor(double[] potential, MaxOperator op, Factor[] neighbors) {
        IndependentFactor factor = new IndependentFactor();
        factor.setMaxOperator(op);
        link(factor, neighbors);
        for (int i=0; i<neighbors.length; i++) {
            factor.setPotential(neighbors[i], potential[i]);
        }
        return factor;
    }

    private Factor buildStandardFactor(double[] potential, MaxOperator op, Factor[] neighbors) {
        StandardFactor factor = new StandardFactor();
        factor.setMaxOperator(op);
        link(factor, neighbors);
        factor.setPotential(buildPotential(potential, neighbors));
        return factor;
    }

    public double[] buildPotential(double[] potential, Factor[] neighbors) {
        final int nNeighbors = neighbors.length;
        final int nConfigurations = 1 << nNeighbors;
        double[] values = new double[nConfigurations];

        // index is the number of "row" in the potential table
        for (int index=0; index<nConfigurations; index++) {
            // this checks every variable to see whether it is active in that row, and adds
            // the potential if it is.
            for (int i=0; i<nNeighbors; i++) {
                final int mask = 1 << (nNeighbors-1-i);
                if ((index & mask) > 0) {
                    values[index] += potential[i];
                }
            }
        }

        return values;
    }
}
