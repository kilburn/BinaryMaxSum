/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2014 Marc Pujol <mpujol@iiia.csic.es>.
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

import java.util.Map;

/**
 * Factor that composes (sums) an independent cost/utility for each neighbor with some
 * other <em>inner</em> (non-independent) factor.
 * <p/>
 * The resulting complexity is the same as that of the inner factor.
 *
 * @author Toni Penya-Alba <tonipenya@iiia.csic.es>
 * @param <T> Type of the factor's identity.
 */
public class SingleWeightFactor<T> extends ProxyFactor<T> {

    private double potential = 0d;

    /**
     * Build a new weighting factor with the specified inner factor.
     *
     * @param innerFactor inner factor to compose (sum) with the per-neighbor weights.
     */
    public SingleWeightFactor(Factor<T> innerFactor) {
        super(innerFactor);
    }

    @Override
    public double getMessage(T neighbor) {
        return super.getMessage(neighbor) - getPotential();
    }

    /**
     * Get the cost/utility of activating a variable.
     *
     * @return cost of activating
     */
    public double getPotential() {
        return potential;
    }

    /**
     * Set the independent cost of activating a variable.
     *
     * @param newPotential cost/utility of activating a variable
     */
    public void setPotential(double newPotential) {
        for (T neighbor: getNeighbors()) {
            double originalMessage = getMessage(neighbor);
            super.receive(originalMessage + newPotential, neighbor);
        }

        potential = newPotential;
    }

    @Override
    public double evaluate(Map<T, Boolean> values) {
        double value = super.evaluate(values);
        for (T neighbor : getNeighbors()) {
            if (values.get(neighbor)) {
                value += potential;
            }
        }
        return value;
    }

    @Override
    public void receive(double message, T sender) {
        super.receive(message + potential, sender);
    }

    @Override
    public void send(double message, T recipient) {
        super.send(message + potential, recipient);
    }



}
