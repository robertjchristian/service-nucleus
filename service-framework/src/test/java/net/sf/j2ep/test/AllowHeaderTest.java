/*
 * Copyright 2000,2004 Anders Nyman.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.j2ep.test;

import junit.framework.TestCase;
import net.sf.j2ep.model.AllowedMethodHandler;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;

public class AllowHeaderTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testSetAllowed() {
        String allow = "OPTIONS,PROPFIND,OP,PUT";
        AllowedMethodHandler.setAllowedMethods(allow);
        assertEquals("Checking that allow is set", allow, AllowedMethodHandler.getAllowHeader());
        
        assertTrue("Checking that OPTIONS is allowed", AllowedMethodHandler.methodAllowed("OPTIONS"));
        assertTrue("Checking that PROPFIND is allowed", AllowedMethodHandler.methodAllowed("PROPFIND"));
        assertTrue("Checking that OP is allowed", AllowedMethodHandler.methodAllowed("OP"));
        assertTrue("Checking that PUT is allowed", AllowedMethodHandler.methodAllowed("PUT"));
        assertFalse("Checking that PROP isn't allowed", AllowedMethodHandler.methodAllowed("PROP"));
        
        assertTrue("Checking OPTIONS method", AllowedMethodHandler.methodAllowed(new OptionsMethod()));
        assertFalse("Checking GET method", AllowedMethodHandler.methodAllowed(new GetMethod()));
    }
    
    public void testprocessAllowHeader() {
        String allow = "OPTIONS,PROPFIND,OP,PUT";
        String correct = "OPTIONS,PUT,";
        AllowedMethodHandler.setAllowedMethods("OPTIONS,GET,PUT,HEJ");
        String returned = AllowedMethodHandler.processAllowHeader(allow);
        assertEquals("Checking factory implementation for allow header", correct, returned);
    }

}
