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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class WeightingFactor<T> extends ProxyFactor<T> {

    private Map<T, Double> potential = new HashMap<T, Double>();

    public WeightingFactor(Factor<T> innerFactor) {
        super(innerFactor);
    }

    /**
     * Remove all potential costs.
     */
    public void clearPotentials() {
        potential.clear();
    }

    /**
     * Get the cost/utility of activating the variable shared with the given
     * neighbor.
     *
     * @param neighbor neighbor to consider
     * @return cost of activating the given neighbor
     */
    public double getPotential(T neighbor) {
        if (!potential.containsKey(neighbor)) {
            throw new IllegalArgumentException("Requested potential for a non-existant neighbor");
        }
        return potential.get(neighbor);
    }

    /**
     * Remove the cost associated to activating the given factor.
     *
     * @param neighbor factor to consider
     * @return previous cost of activating the given factor
     */
    public Double removePotential(T neighbor) {
        return potential.remove(neighbor);
    }

    /**
     * Set the independent cost of activating the variable that corresponds to
     * the given neighbor.
     *
     * @param neighbor
     * @param value cost/utility of activating this neighbor
     */
    public void setPotential(T neighbor, double value) {
        potential.put(neighbor, value);
    }

    @Override
    public void receive(double message, T sender) {
        super.receive(message + getPotential(sender), sender);
    }

    @Override
    public void send(double message, T recipient) {
        super.send(message + getPotential(recipient), recipient);
    }



}
