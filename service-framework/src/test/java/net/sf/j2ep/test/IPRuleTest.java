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

import net.sf.j2ep.rules.IPRule;

public class IPRuleTest extends TestCase {

    private MockIPRule ipRule;

    protected void setUp() throws Exception {
        ipRule = new MockIPRule();
        super.setUp();
    }

    /*
     * Test method for
     * 'net.sf.j2ep.rules.IPRule.setStartRange(String)'
     */
    public void testSetStartRange() {
        try {
            ipRule.setStartRange("46548754.22.22.22");
            fail("Should throw Exception, this is not a correct IP");
        } catch (IllegalArgumentException e) {

        }
        try {
            ipRule.setStartRange(null);
            fail("Should throw Exception, this is not a correct IP");
        } catch (IllegalArgumentException e) {

        }
        try {
            ipRule.setEndRange("10.0.0.0");
            ipRule.setStartRange("10.0.0.1");
            fail("Should throw Exception, startRange has to come before endRange");
        } catch (IllegalArgumentException e) {

        }
    }

    /*
     * Test method for
     * 'org.apache.webapp.reverseproxy.rules.IPRule.getStartRange()'
     */
    public void testGetStartRange() {
        ipRule.setStartRange("12.0.4.0");
        assertEquals("The given startRange wasn't stored", "12.0.4.0", ipRule
                .getStartRange());
    }

    /*
     * Test method for
     * 'org.apache.webapp.reverseproxy.rules.IPRule.setEndRange(String)'
     */
    public void testSetEndRange() {
        try {
            ipRule.setStartRange("hej.22.22.22");
            fail("Should throw Exception, this is not a correct IP");
        } catch (IllegalArgumentException e) {

        }
        try {
            ipRule.setStartRange(null);
            fail("Should throw Exception, this is not a correct IP");
        } catch (IllegalArgumentException e) {

        }
        try {
            ipRule.setEndRange("192.45.244.255");
            ipRule.setStartRange("192.45.245.2");
            fail("Should throw Exception, endRange has to come after startRange");
        } catch (IllegalArgumentException e) {

        }

    }

    /*
     * Test method for
     * 'org.apache.webapp.reverseproxy.rules.IPRule.matches(HttpServletRequest
     * request)'
     */
    public void testMatches() {
        class IPTest extends MockHttpServletRequest {
            private String addr;

            public IPTest(String addr) {
                this.addr = addr;
            }

            public String getRemoteAddr() {
                return addr;
            }
        }
        ipRule.setStartRange("10.0.0.1");
        ipRule.setEndRange("10.0.0.5");

        assertTrue("10.0.0.1 should be allowed", ipRule.matches(new IPTest(
                "10.0.0.1")));
        assertTrue("10.0.0.5 should be allowed", ipRule.matches(new IPTest(
                "10.0.0.5")));
        assertTrue("10.0.0.3 should be allowed", ipRule.matches(new IPTest(
                "10.0.0.3")));
        assertFalse("10.0.0.0 shouldn't be allowed", ipRule.matches(new IPTest(
                "10.0.0.0")));
        assertFalse("10.0.0.6 shouldn't be allowed", ipRule.matches(new IPTest(
                "10.0.0.6")));
    }

    /*
     * Test method for
     * 'org.apache.webapp.reverseproxy.rules.IPRule.getEndRange()'
     */
    public void testGetEndRange() {
        ipRule.setEndRange("12.0.4.0");
        assertEquals("The given endRange wasn't stored", "12.0.4.0", ipRule
                .getEndRange());
    }

    /*
     * Needed since getStartRange and getEndRange are
     * private in the IPRule.
     */
    private static class MockIPRule extends IPRule {
        public String getStartRange() {
            return super.getStartRange();
        }

        public String getEndRange() {
            return super.getEndRange();
        }
    }

}
