/*
 * Copyright 2005 Anders Nyman.
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

import net.sf.j2ep.rules.DirectoryRule;

public class DirectoryRuleTest extends TestCase {

    private DirectoryRule dirRule;
    
    protected void setUp() throws Exception {
        dirRule = new DirectoryRule();
    }
    
    public void testSetDirectory() {
        dirRule.setDirectory("/test/");
        assertEquals("The directory didn't get saves properly", "/test/", dirRule.getDirectory());
        
        dirRule.setDirectory("/test");
        assertEquals("The DirectoryRule should add a slash at the end if there isn't one in the input", "/test/", dirRule.getDirectory());
        
        dirRule.setDirectory("test");
        assertEquals("The DirectoryRule should add slash at start and end", "/test/", dirRule.getDirectory());
        
        dirRule.setDirectory("");
        assertEquals("The DirectoryRule should make this '/'", "/", dirRule.getDirectory());
        
        try {
            dirRule.setDirectory(null);
            fail("Should throw exceptions, directory can't be null");
        } catch (IllegalArgumentException e) {
            
        }
    }
    
    public void testMatch() {
        dirRule.setDirectory("/");
        
        assertTrue("This URI should be matched", dirRule.matches( new MockHttpServletRequest() {
            public String getServletPath() {
                return "/test/hej.html";
            }
        }));
        
        dirRule.setDirectory("test/");
        
        assertTrue("This URI should be matched", dirRule.matches( new MockHttpServletRequest() {
            public String getServletPath() {
                return "/test/hej.html";
            }
        }));
        
        assertFalse("This URI shouldn't be matched", dirRule.matches( new MockHttpServletRequest() {
            public String getServletPath() {
                return "../test/hej.html";
            }
        }));
        
        assertFalse("This URI shouldn't be matched", dirRule.matches( new MockHttpServletRequest() {
            public String getServletPath() {
                return "test/.html";
            }
        }));
    }
    
    public void testProcess() {
        /* In this test it is assumed that the input to process is indeed matched.
         * This means that I will not send in illegal input to process.
         */
        dirRule.setDirectory("/");
        assertEquals("The URI should be the same as before", "/test/hej.html", dirRule.process("/test/hej.html"));
        
        dirRule.setDirectory("/test/");
        assertEquals("The initial directory should be removed", "/hej.html", dirRule.process("/test/hej.html"));
        
        dirRule.setDirectory("/test/hej/");
        assertEquals("The initial directory should be removed", "/hej.html", dirRule.process("/test/hej/hej.html"));
        
        dirRule.setDirectory("/../test/hej/");
        assertEquals("The initial directory should be removed", "/hej.html", dirRule.process("/../test/hej/hej.html"));
    }
    
    public void testRevert() {
        dirRule.setDirectory("/");
        assertEquals("The URI should be the same as before", "hej.html", dirRule.revert("hej.html"));
        assertEquals("The URI should be the same as before", "/test/hej.html", dirRule.revert("/test/hej.html"));
        
        dirRule.setDirectory("/test/");
        assertEquals("The URI should be the same as before", "http://www.hej.com", dirRule.revert("http://www.hej.com"));
        assertEquals("The URI should be the same as before", "C:/windows", dirRule.revert("C:/windows"));
        assertEquals("The URI should be the same as before", "hej.html", dirRule.revert("hej.html"));
        assertEquals("The URI should be the same as before", "../hej.html", dirRule.revert("../hej.html"));
        assertEquals("The URI should be rewritten", "/test/hej.html", dirRule.revert("/hej.html"));
        
        dirRule.setDirectory("/test/test2/");
        assertEquals("The URI should be rewritten", "/test/test2/hum/hej.html", dirRule.revert("/hum/hej.html"));
    }

}
