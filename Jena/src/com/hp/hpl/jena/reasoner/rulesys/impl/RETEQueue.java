/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.graph.*;

import java.util.*;

/**
 * Represents one input left of a join node. The queue points to 
 * a sibling queue representing the other leg which should be joined
 * against.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2009-06-29 08:55:33 $
 */
public class RETEQueue implements RETESinkNode, RETESourceNode {
    
    /** A multi-set of partially bound envionments */
    protected HashMap<BindingVector, Count> queue = new HashMap<BindingVector, Count>();
    
    /** A set of variable indices which should match between the two inputs */
    protected byte[] matchIndices;
    
    /** The sibling queue which forms the other half of the join node */
    protected RETEQueue sibling;
    
    /** The node that results should be passed on to */
    protected RETESinkNode continuation;
    
    /** 
     * Constructor. The queue is not usable until it has been bound
     * to a sibling and a continuation node.
     * @param A set of variable indices which should match between the two inputs
     */
    public RETEQueue(byte[] matchIndices) {
        this.matchIndices = matchIndices; 
    }
    
    /** 
     * Constructor. The queue is not usable until it has been bound
     * to a sibling and a continuation node.
     * @param A List of variable indices which should match between the two inputs
     */
    public RETEQueue(List<? extends Byte> matchIndexList) {
        int len = matchIndexList.size();
        matchIndices = new byte[len];
        for (int i = 0; i < len; i++) {
            matchIndices[i] = matchIndexList.get(i).byteValue();
        }
    }
    
    /**
     * Set the sibling for this node.
     */
    public void setSibling(RETEQueue sibling) {
        this.sibling = sibling;
    }
    
    /**
     * Set the continuation node for this node (and any sibling)
     */
    @Override
    public void setContinuation(RETESinkNode continuation) {
        this.continuation = continuation;
        if (sibling != null) sibling.continuation = continuation;
    }

    /** 
     * Propagate a token to this node.
     * @param env a set of variable bindings for the rule being processed. 
     * @param isAdd distinguishes between add and remove operations.
     */
    @Override
    public void fire(BindingVector env, boolean isAdd) {
        // Store the new token in this store
        Count count = queue.get(env);
        if (count == null) {
            // no entry yet
            if (!isAdd) return;
            queue.put(env, new Count(1));
        } else {
            if (isAdd) {
                count.inc();
            } else {
                count.dec();
                if (count.getCount() == 0) {
                    queue.remove(env);
                }
            }
        }
        
        // Cross match new token against the entries in the sibling queue
        for (Iterator<BindingVector> i = sibling.queue.keySet().iterator(); i.hasNext(); ) {
            Node[] candidate = i.next().getEnvironment();
            Node[] envNodes = env.getEnvironment();
            boolean matchOK = true;
            for (int j = 0; j < matchIndices.length; j++) {
                int index = matchIndices[j];
                if ( ! candidate[index].sameValueAs(envNodes[index])) {
                    matchOK = false;
                    break;
                }
            }
            if (matchOK) {
                // Instantiate a new extended environment
                Node[] newNodes = new Node[candidate.length];
                for (int j = 0; j < candidate.length; j++) {
                    Node n = candidate[j];
                    newNodes[j] = (n == null) ? envNodes[j] : n;
                }
                BindingVector newEnv = new BindingVector(newNodes);
                // Fire the successor processing
                continuation.fire(newEnv, isAdd);
            }
        }
    }

    /**
     * Inner class used to represent an updatable count.
     */
    protected static class Count {
        /** the count */
        int count;
        
        /** Constructor */
        public Count(int count) {
            this.count = count;
        }
        
        /** Access count value */
        public int getCount() {
            return count;
        }
        
        /** Increment the count value */
        public void inc() {
            count++;
        }
        
        /** Decrement the count value */
        public void dec() {
            count--;
        }
        
        /** Set the count value */
        public void setCount(int count) {
            this.count = count;
        }
    }
    
    /**
     * Clone this node in the network.
     * @param context the new context to which the network is being ported
     */
    @Override
    public RETENode clone(Map<RETENode, RETENode> netCopy, RETERuleContext context) {
        RETEQueue clone = (RETEQueue)netCopy.get(this);
        if (clone == null) {
            clone = new RETEQueue(matchIndices);
            netCopy.put(this, clone);
            clone.setSibling((RETEQueue)sibling.clone(netCopy, context));
            clone.setContinuation((RETESinkNode)continuation.clone(netCopy, context));
            clone.queue.putAll(queue);
        }
        return clone;
    }
}
