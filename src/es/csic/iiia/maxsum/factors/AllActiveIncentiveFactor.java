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

import es.csic.iiia.maxsum.MaxOperator;
import es.csic.iiia.maxsum.util.BestValuesTracker;

/**
 * A factor that (de)incentivizes a group of variables (from being) to be all on.
 *
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



    /**
     * Runs an iteration of this factor, computing and sending messages to all neighbors.
     *
     * The messages are computed using the following derivation:
     *
     * \nu_{f->x_i} = max(0, incentive + \sum_{j \in N; j \neq i} [ \nu_{x_j->f} ] -
     *                \sum_{j \in N; j \neq i} [ max(\nu_{x_j->f}, 0) ]
     *
     * @return number of Constraint Checks performed by this node.
     */
    @Override
    public long run() {

        double fullSum = 0;
        double onlyGoodsSum = 0;
        worstValuesTracker.reset();

        for (T neighbor : getNeighbors()) {
            final double message = getMessage(neighbor);
            fullSum += message;
            onlyGoodsSum += getMaxOperator().max(0, message);
            worstValuesTracker.track(neighbor, message);
        }

        for (T neighbor : getNeighbors()) {
            final double inMessage = getMessage(neighbor);

            // all = \sum_{j \in N; j \neq i} [ \nu_{x_j->f} ]
            //     = \sum_{j \in N} [ \nu_{x_j->f} ] - \nu_{x_i->f}
            double all = fullSum - inMessage;

            // positives = \sum_{j \in N; j \neq i} [ max(\nu_{x_j->f}, 0) ]
            //           = \sum_{j \in N} [ max(\nu_{x_j->f}, 0) ] - max(\nu_{x_i->f},0)
            double goods = onlyGoodsSum - getMaxOperator().max(0, inMessage);

            double goodsMinusOne = goods;
            if (goods == all) {
                goodsMinusOne -= worstValuesTracker.getComplementary(neighbor);
            }

            final double message = getMaxOperator().max(goodsMinusOne, incentive + all)
                    - goods;
            send(message, neighbor);
        }

        return 2*getNeighbors().size();
    }

}
