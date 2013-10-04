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

import es.csic.iiia.maxsum.Factor;
import es.csic.iiia.maxsum.CommunicationAdapter;
import es.csic.iiia.maxsum.MaxOperator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Skeletal implementation of a Max-Sum factor.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public abstract class AbstractFactor<T> implements Factor<T> {

    private Map<T, Double> messages = new HashMap<T, Double>();
    private List<T> neighbors = new ArrayList<T>();
    private MaxOperator maxOperator;
    private T identity;
    private CommunicationAdapter<T> communicationAdapter;

    @Override
    public MaxOperator getMaxOperator() {
        return maxOperator;
    }

    @Override
    public void setMaxOperator(MaxOperator maxOperator) {
        this.maxOperator = maxOperator;
    }

    @Override
    public T getIdentity() {
        return identity;
    }

    @Override
    public void setIdentity(T identity) {
        this.identity = identity;
    }

    @Override
    public CommunicationAdapter<T> getCommunicationAdapter() {
        return communicationAdapter;
    }

    @Override
    public void setCommunicationAdapter(CommunicationAdapter<T> communicationAdapter) {
        this.communicationAdapter = communicationAdapter;
    }

    @Override
    public void addNeighbor(T factor) {
        neighbors.add(factor);
        messages.put(factor, 0d);
    }

    @Override
    public List<T> getNeighbors() {
        return neighbors;
    }

    /**
     * Get the last message received from the given neighor.
     *
     * @param neighbor neighbor whose message to get.
     * @return message received from the given neighbor.
     */
    public double getMessage(T neighbor) {
        if (!messages.containsKey(neighbor)) {
            throw new RuntimeException("I don't have a message from neighbor " + neighbor);
        }
        return messages.get(neighbor);
    }

    /**
     * Run an iteration of the factor.
     * 
     * @return
     */
    protected abstract long iter();

    @Override
    public final long run() {
        long ccs = iter();
        messages.clear();
        return ccs;
    }

    @Override
    public void receive(double message, T sender) {
        if (!neighbors.contains(sender)) {
            throw new RuntimeException("I (" + getClass().getName() + ", " + getIdentity() + ") received a message from the non-neighbor sender " + sender);
        }
        messages.put(sender, message);
    }

    @Override
    public void send(double message, T recipient) {
        getCommunicationAdapter().send(message, getIdentity(), recipient);
    }

}
