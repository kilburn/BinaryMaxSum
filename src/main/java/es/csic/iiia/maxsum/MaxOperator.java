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
package es.csic.iiia.maxsum;

/**
 * Definition of the <em>max</em> operator used by the max-sum algorithm.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public interface MaxOperator {

    /**
     * Return the inverse of this max operator.
     * <p/>
     * For instance, the <em>maximum</em> operator would return the <em>minimum</em> operator.
     *
     * @return inverse of this max operator.
     */
    public MaxOperator inverse();

    /**
     * Operates two values, returning the better one according to this operator
     *
     * @param value1 first value
     * @param value2 second value
     * @return best value between the given ones
     */
    public double max(double value1, double value2);

    /**
     * Compares two values, returning -1 if the first is worst, 0 if they are equal, or 1 if the
     * first value is better.
     * @param first first value to compare
     * @param second second value to compare
     * @return -1 if the first value is worst than the second, 0 if they are equal, or 1 otherwise.
     */
    public int compare(double first, double second);

    /**
     * Returns the worst possible value for this operator.
     * <p/>
     * The returned value is one such that when operated with another value, the
     * result is always the other value:
     *
     * <code>operate(value, getWorstValue()) == value</Code>
     *
     * @return the worst possible value for this operator.
     */
    public double getWorstValue();

}
