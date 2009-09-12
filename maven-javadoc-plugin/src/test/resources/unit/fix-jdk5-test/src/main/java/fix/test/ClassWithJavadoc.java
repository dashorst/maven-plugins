package fix.test;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.List;
import java.util.Map;

/**
 * To add default class tags.
 */
public class ClassWithJavadoc
    implements InterfaceWithJavadoc
{
    /**
     * Empty constructor.
     */
    public ClassWithJavadoc()
    {
    }

    /**
     * Constructor with generic param.
     */
    public ClassWithJavadoc( List<String> list )
    {
    }

    /**
     * QDOX-155
     */
    public <T extends String> List<String> withGenericParameter( T request )
        throws Exception
    {
        return null;
    }

    // ----------------------------------------------------------------------
    // New methods to be found by Clirr.
    // ----------------------------------------------------------------------

    /**
     * Correct generic doclets.
     *
     * @param <K>  The Key type for the method
     * @param <V>  The Value type for the method
     * @param name The name.
     * @return A map configured.
     */
    public <K, V> Map<K, V> withGenericDoclet( String name )
    {
        return null;
    }

    /**
     * Wrong generic doclets.
     *
     * @param <T>  The Key type for the method
     * @param <U>  The Value type for the method
     * @param name The name.
     * @return A map configured.
     */
    public <K, V> Map<K, V> withGenericDoclet2( String name )
    {
        return null;
    }

    /**
     * New class method to be found by Clirr.
     */
    public <K, V> Map<K, V> newClassMethod( List<String> aList, Map<Map<String, List<String>>, List<String>> aMap )
    {
        return null;
    }

    // ----------------------------------------------------------------------
    // Inheritance
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public void withGenericParameters( List<String> aList, Map<Map<String, List<String>>, List<String>> aMap )
    {
    }

    /** {@inheritDoc} */
    public <K, V> Map<K, V> newInterfaceMethod( List<String> aList, Map<Map<String, List<String>>, List<String>> aMap )
    {
        return null;
    }
}
