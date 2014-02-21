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

import es.csic.iiia.bms.MaxOperator;
import es.csic.iiia.bms.util.BestValuesTracker;

import java.util.Map;

/**
 * A factor that (de)incentivizes a group of variables (from being) to be all on.
 *
 * The messages are computed in O(n) time using the following derivation:
 * <pre>
 *      \nu_{f->x_i} = max[ min(0, -v_i^*), \alpha + v_i^- ],
 * where
 *      v_i^* = min_{j | j != i} v_j
 * and
 *      v_i^- = \sum_{j | j != i and v_j < 0} v_j =
 *            = v^- - min(v_i, 0) ,
 *      v^- = \sum_{j} min(v_j, 0)
 * </pre>
 *
 * @param <T> Type of the factor's identity.
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class AllActiveIncentiveFactor<T> extends AbstractFactor<T> {

    private double incentive;
    private BestValuesTracker<T> worstValuesTracker;

    /**
     * Get the incentive (or penalty) introduced when all variables are active
     * @return incentive introduced when all variables are active.
     */
    public double getIncentive() {
        return incentive;
    }

    /**
     * Set the incentive (or penalty) introduced when all variables are active
     *
     * @param incentive incentive introduced when all variables are active.
     */
    public void setIncentive(double incentive) {
        this.incentive = incentive;
    }

    @Override
    public void setMaxOperator(MaxOperator maxOperator) {
        super.setMaxOperator(maxOperator);
        worstValuesTracker = new BestValuesTracker<T>(maxOperator.inverse());
    }

    @Override
    public double eval(Map<T, Boolean> values) {
        for (T neighbor : getNeighbors()) {
            if (!values.get(neighbor)) {
                return 0;
            }
        }
        return incentive;
    }

    @Override
    public long run() {
        final MaxOperator max = getMaxOperator();
        final MaxOperator min = getMaxOperator().inverse();
        worstValuesTracker.reset();

        double v_negative = 0;
        for (T neighbor : getNeighbors()) {
            final double message = getMessage(neighbor);
            worstValuesTracker.track(neighbor, message);
            v_negative += min.max(0, message);
        }

        for (T neighbor : getNeighbors()) {
            final double inMessage = getMessage(neighbor);

            final double v_i_star = worstValuesTracker.getComplementary(neighbor);
            final double v_i_negative = v_negative - min.max(0, inMessage);

            final double message = max.max(min.max(0, -v_i_star), incentive + v_i_negative);
            send(message, neighbor);
        }

        return 2*getNeighbors().size();
    }

}
