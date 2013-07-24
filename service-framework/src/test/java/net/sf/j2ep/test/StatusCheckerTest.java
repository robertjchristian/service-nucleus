package net.sf.j2ep.test;

import junit.framework.TestCase;
import net.sf.j2ep.model.Server;
import net.sf.j2ep.servers.BaseServer;
import net.sf.j2ep.servers.ServerStatusChecker;
import net.sf.j2ep.servers.ServerStatusListener;

public class StatusCheckerTest extends TestCase {
    
    private ServerStatusChecker statusChecker;
    private TestStatusListener listener;
    private BaseServer server;

    protected void setUp() throws Exception {
        listener = new TestStatusListener();
        statusChecker = new ServerStatusChecker(listener, 1);
        statusChecker.start();
        server = new BaseServer();
    }
    
    public void testAddServer() {
        server.setDomainName("localhost:8080");
        server.setPath("/test-response");
        listener.makeReady();
        statusChecker.addServer(server);
        while (!listener.gotResponse()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
        }
        assertEquals("Should be getting the expected server", server, listener.getNextOnline());
        
        //Taking server offline
        listener.makeReady();
        server.setDomainName("locallkjlkjlkjhost:8080");
        statusChecker.interrupt();
        while (!listener.gotResponse()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
        }
        assertEquals("Should be getting the expected server", server, listener.getNextOffline());
        
        // Taking server online
        listener.makeReady();
        server.setDomainName("localhost:8080");
        statusChecker.interrupt();
        while (!listener.gotResponse()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
        }
        assertEquals("Should be getting the expected server", server, listener.getNextOnline());
    }
    
    protected void tearDown() {
        statusChecker = null;
    }

    private class TestStatusListener implements ServerStatusListener {
        
        private Server offline;
        private Server online;
        private volatile boolean gotResponse;
        
        public synchronized void serverOnline(Server theServer) {
            online = theServer;
            gotResponse = true;
        }

        public synchronized void serverOffline(Server theServer) {
            offline = theServer;
            gotResponse = true;
        }

        public Server getNextOffline() {
            return offline;
        }

        public Server getNextOnline() {
            return online;
        }
        
        public boolean gotResponse() {
            return gotResponse;
        }
        
        public void makeReady() {
            gotResponse = false;
            online = null;
            offline = null;
        }

    }
}
