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
package es.csic.iiia.bms.factors;

import es.csic.iiia.bms.factors.twosided.EqualFactor;

/**
 * Max-sum "Two-sided equality" factor.
 *
 * Given two sets of neighbors A = {a_1, ..., a_p} and B = {b_1, ..., b_q}, this
 * factor tries to ensure that there are the same number of neighbors from the
 * first set chosen than from the second one. That is, f(a_1, ..., a_p, b_1,
 * ..., b_q) = 0 if (\sum_i a_i) = (\sum_j b_j) or (-)\infty otherwise.
 * <p/>
 * Outgoing messages are computed in <em>O(n log(n))</em> time. Where <em>n</em>
 * is the number of elements in the biggest set. That is, n = max(p, q).
 *
 * @param <T> Type of the factor's identity.
 * @author Toni Penya-Alba <tonipenya@iiia.csic.es>
 * @deprecated use {@link es.csic.iiia.bms.factors.twosided.EqualFactor} instead.
 */
@Deprecated
public class TwoSidedEqualityFactor<T> extends EqualFactor<T> {
}
