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
package es.csic.iiia.maxsum.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * A bounded-size TreeSet. New elements inserted into this BoundedTreeSet evict the greatest one
 * (according to the Comparator used by this collection).
 * <p>
 * This implementation also provides a constant-time {@code contains} method by using an auxiliary
 * HashSet of the members.
 *
 * @param <E> Type of the elements contained in this bounded set.
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class BoundedTreeSet<E> extends TreeSet<E> {
    private static final long serialVersionUID = 1L;

    /**
     * The (fixed) capacity of this bounded set.
     */
    public final int capacity;

    private final HashSet<E> members;

    /**
     * Build a new bounded set of the specified capacity.
     *
     * @param capacity maximum number of elements in this set.
     */
    public BoundedTreeSet(int capacity) {
        super();
        members = new HashSet<E>(capacity);
        this.capacity = capacity;
    }

    /**
     * Build a new bounded set of the specified capacity, and fill it with the elements of the
     * given collection.
     *
     * @param capacity maximum number of elements in this set.
     * @param c collection of elements to pre-fill the set with.
     */
    public BoundedTreeSet(int capacity, Collection<? extends E> c) {
        super();
        members = new HashSet<E>(capacity);
        this.capacity = capacity;
        addAll(c);
    }

    /**
     * Build a new bounded set of the specified capacity, where elements will be sorted in the order
     * defined by the given comparator. The comparator also controls the eviction policy (worst
     * values are evicted first).
     *
     * @param capacity maximum number of elements in this set.
     * @param comparator comparator used to order the elements in this set.
     */
    public BoundedTreeSet(int capacity, Comparator<? super E> comparator) {
        super(comparator);
        members = new HashSet<E>(capacity);
        this.capacity = capacity;
    }

    @Override
    public boolean add(E e) {
        boolean result = super.add(e);
        if (result) {
            members.add(e);
        }
        if (size() > capacity) {
            members.remove(pollLast());
        }
        return result;
    }

    @Override
    public final boolean addAll(Collection<? extends E> c) {
        boolean change = false;
        for (E e : c) {
            change = change || add(e);
        }
        return change;
    }

    @Override
    public boolean contains(Object o) {
        return members.contains(o);
    }

}
