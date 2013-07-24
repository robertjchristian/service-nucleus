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

import net.sf.j2ep.rules.CompositeRule;
import net.sf.j2ep.rules.DirectoryRule;
import net.sf.j2ep.rules.IPRule;

public class CompositeRuleTest extends TestCase {

    private CompositeRule rule;

    protected void setUp() throws Exception {
        super.setUp();
        rule = new CompositeRule();
    }

    /*
     * Test method for
     * 'net.sf.j2ep.rules.CompositeRule.matches(HttpServletRequest)'
     */
    public void testMatches() {
        IPRule ipRule = new IPRule();
        ipRule.setStartRange("10.0.0.1");
        ipRule.setEndRange("11.0.0.25");
        DirectoryRule dirRule = new DirectoryRule();
        dirRule.setDirectory("/test/");

        rule.addRule(ipRule);
        rule.addRule(dirRule);

        assertTrue("This request should match both rules", rule
                .matches(new MockHttpServletRequest() {
                    public String getRemoteAddr() {
                        return "11.0.0.0";
                    }

                    public String getServletPath() {
                        return "/test/hejsan.html";
                    }
                }));

        assertFalse("This request shouldn't match the DirectoryRule", rule
                .matches(new MockHttpServletRequest() {
                    public String getRemoteAddr() {
                        return "11.0.0.14";
                    }

                    public String getServletPath() {
                        return "/tesssst/index.html";
                    }
                }));
    }

    public void testEmptyRule() {
        assertTrue(
                "An empty compositeRule should return true since all the rules are infact matched",
                rule.matches(new MockHttpServletRequest() {
                }));
    }

}
