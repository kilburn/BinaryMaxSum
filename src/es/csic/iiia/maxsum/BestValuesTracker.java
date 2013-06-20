/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2012 Marc Pujol <mpujol@iiia.csic.es>.
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
package es.csic.iiia.maxsum;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utitlity class to compute the two best objects among a couple of them.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class BestValuesTracker<T> {
    private static final Logger LOG = Logger.getLogger(BestValuesTracker.class.getName());

    private MaxOperator operator;
    private final double[] values;
    private final Object[] objects;
    private int count = 0;

    public BestValuesTracker(MaxOperator operator) {
        values = new double[2];
        objects = new Object[2];
        this.operator = operator;
    }

    public void reset() {
        LOG.finest("Tracking start");
        values[0] = values[1] = operator.getWorstValue();
        objects[0] = null; objects[1] = null;
        count = 0;
    }

    public double getComplementary(T t) {
        if (count == 0) {
            return 0;
        }

        if (t == objects[0]) {
            return count == 1 ? 0 : values[1];
        }

        return count > 0 ? values[0] : 0;
    }

    public T getBest() {
        return (T)objects[0];
    }

    public void track(T t, double value) {
        count++;

        LOG.log(Level.FINEST, "BestValues tracking {0}", value);

        if (operator.max(value, values[0]) == value) {
            values[1]  = values[0];     values[0]  = value;
            objects[1] = objects[0];    objects[0] = t;
            return;
        }

        if (operator.max(value, values[1]) == value) {
            values[1]  = value;
            objects[1] = t;
        }

    }

    @Override
    public String toString() {
        return "Best(" + values[0] + "," + values[1] + ")[" + count + "]";
    }

}