package net.sf.j2ep.test;

import java.io.IOException;

import javax.servlet.ServletException;

import net.sf.j2ep.ProxyFilter;

import org.apache.cactus.FilterTestCase;
import org.apache.cactus.WebRequest;
import org.apache.cactus.WebResponse;

public class ClusterTest extends FilterTestCase {

    private ProxyFilter proxyFilter;
    
    public void setUp() {   
        proxyFilter = new ProxyFilter();
        config.setInitParameter("dataUrl", "/WEB-INF/classes/net/sf/j2ep/test/testData.xml");
        try {
            proxyFilter.init(config);
        } catch (ServletException e) {
            fail("Problem with init, error given was " + e.getMessage());
        }
    }
    
    public void beginFirstServer(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/testCluster/serverId.jsp", null, null);
        theRequest.addHeader("Cookie", "JSESSIONID=somesessionid.server0");
    }
    
    public void testFirstServer() throws IOException, ServletException {
        proxyFilter.doFilter(request, response, filterChain);      
    }
    
    public void endFirstServer(WebResponse theResponse) {
        assertEquals("Targeting first server", 0, Integer.parseInt(theResponse.getText()));
    }
    
    public void beginSecondServer(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/testCluster/serverId.jsp", null, null);
        theRequest.addHeader("Cookie", "JSESSIONID=somesessionid.server1");
    }
    
    public void testSecondServer() throws IOException, ServletException {
        proxyFilter.doFilter(request, response, filterChain);
    }
    
    public void endSecondServer(WebResponse theResponse) {
        assertEquals("Targeting second server", 1, Integer.parseInt(theResponse.getText()));
    }
    
    public void beginSessionRewriting(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/testCluster/createSession.jsp", null, null);
    }
    
    public void testSessionRewriting() throws IOException, ServletException {
        proxyFilter.doFilter(request, response, filterChain);
    }
    
    public void endSessionRewriting(WebResponse theResponse) {
        String session = theResponse.getCookie("JSESSIONID").getValue();
        assertTrue("Checking that we have rewritten the session", session.indexOf(".server")>-1);
    }
    
    public void beginServerRemoving(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/testCluster/viewSession.jsp", null, null);
        theRequest.addHeader("Cookie", "JSESSIONID=somesessionid.server1");
    }
    
    public void testServerRemoving() throws IOException, ServletException {
        proxyFilter.doFilter(request, response, filterChain);
    }
    
    public void endServerRemoving(WebResponse theResponse) {
        assertEquals("Checking that the session is included", "somesessionid", theResponse.getText().trim());
    }
    
    public void beginNonExistingServer(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/testCluster/serverId.jsp", null, null);
        theRequest.addHeader("Cookie", "JSESSIONID=somesessionid.server54");
    }
    
    public void testNonExistingServer() throws IOException, ServletException {
        proxyFilter.doFilter(request, response, filterChain);
    }
    
    public void endNonExistingServer(WebResponse theResponse) {
        int id = Integer.parseInt(theResponse.getText());
        String session = theResponse.getCookie("JSESSIONID").getValue();
        assertTrue("Checking that the cluster did send the response to some server anyways", id == 0 || id == 1);
        assertTrue("Checking that we now have a new server", session.endsWith(".server"+id));
    }
    
}
