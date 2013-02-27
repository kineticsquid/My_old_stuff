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

package com.hp.hpl.jena.assembler.exceptions;

import com.hp.hpl.jena.rdf.model.Resource;

/**
    Exception used to report an unknown reification mode.
    @author kers
*/
public class UnknownStyleException extends AssemblerException
    {
    protected final Resource style;
    
    public UnknownStyleException( Resource root, Resource style )
        {
        super( root, makeMessage( root, style ) );
        this.style = style;
        }

    private static String makeMessage( Resource root, Resource style )
        {
        return
            "the object " + nice( root )
            + " has an unknown reification mode " + nice( style )
            ;
        }

    /**
        Answer the resource that is the unknown style.
    */
    public Resource getStyle()
        { return style; }
    }
