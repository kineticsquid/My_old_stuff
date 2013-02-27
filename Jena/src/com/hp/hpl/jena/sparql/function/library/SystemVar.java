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

package com.hp.hpl.jena.sparql.function.library;

//import org.apache.commons.logging.*;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.ExprException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionBase0 ;
import com.hp.hpl.jena.sparql.util.Symbol ;
import com.hp.hpl.jena.sparql.util.Utils ;

/** Function that returns the value of a system variable. */

public class SystemVar extends FunctionBase0
{
    Symbol systemSymbol ;
    protected SystemVar(Symbol systemSymbol)
    {
        if ( systemSymbol == null )
            throw new ExprException("System symbol is null ptr") ;
        this.systemSymbol = systemSymbol ;
    }
    
    /** Processes evaluated args */
    @Override
    public NodeValue exec()
    {
        Object obj = getContext().get(systemSymbol) ;
        
        if ( obj == null )
            throw new ExprEvalException("null for system symbol: "+systemSymbol) ;
        if ( ! ( obj instanceof Node ) )
            throw new ExprEvalException("Not a Node: "+Utils.className(obj)) ;
        
        Node n = (Node)obj ;
//        if ( n == null )
//            throw new ExprEvalException("No value for system variable: "+systemSymbol) ;  
        // NodeValue.makeNode could have a cache.
        NodeValue nv = NodeValue.makeNode(n) ;
        return nv ;
    }
}
