package com.netflix.adminresources;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.inject.Injector;
import com.netflix.config.ConfigurationManager;
import com.netflix.karyon.server.KaryonServer;
import com.netflix.karyon.server.eureka.SyncHealthCheckInvocationStrategy;
import com.netflix.karyon.spi.PropertyNames;



public class HelloWorldTest {
	
    //Comment this when embedded jetty code is completly removed
	public static final int LISTEN_PORT_DEFAULT = 8077;
    
	//Un-Comment this when embedded jetty code is completly removed
	//public static final int LISTEN_PORT_DEFAULT = 8080;
	    
    private static final int httpRetries = 10;
    private static final long sleepTimeout = 1000;
    private static final String helloEndPoint  = "/hello-world/rest/v1/hello/to/me";
    private static final String fs2EndPoint  = "/hello-world/#/fs2";
    private static KaryonServer server;
    
    @BeforeClass
    public void setUp() throws Exception {
        System.setProperty(PropertyNames.SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE, "com.test");
        System.setProperty(PropertyNames.HEALTH_CHECK_TIMEOUT_MILLIS, "60000");
        System.setProperty(PropertyNames.HEALTH_CHECK_STRATEGY, SyncHealthCheckInvocationStrategy.class.getName());
        System.setProperty(PropertyNames.DISABLE_EUREKA_INTEGRATION, "true");
    }

    @AfterClass
    public void tearDown() throws Exception {
        ConfigurationManager.getConfigInstance().clearProperty(PropertyNames.DISABLE_APPLICATION_DISCOVERY_PROP_NAME);
        ConfigurationManager.getConfigInstance().clearProperty(PropertyNames.EXPLICIT_APPLICATION_CLASS_PROP_NAME);
        server.close();
    }
    
    @Test
    public void testHelloWorld() throws Exception {
    	try {
    		HttpClient client = new DefaultHttpClient();
	   		HttpGet helloWorld = new HttpGet("http://localhost:" + LISTEN_PORT_DEFAULT + helloEndPoint);
	   		startServer();
	    	HttpResponse response = doPingHelloWorld(client, helloWorld, httpRetries);
	    	Assert.assertEquals( 500, response.getStatusLine().getStatusCode());
    	} finally {
	    	if (server != null) {
	    		server.close();
	    	}
    	}
    }
    
    @Test
    public void testFS2() throws Exception {
    	try {
    		HttpClient client = new DefaultHttpClient();
	   		HttpGet fs2URL = new HttpGet("http://localhost:" + LISTEN_PORT_DEFAULT + helloEndPoint);
	   		startServer();
	    	HttpResponse response = doPingHelloWorld(client, fs2URL, httpRetries);
	    	Assert.assertEquals( 500, response.getStatusLine().getStatusCode());
    	} finally {
	    	if (server != null) {
	    		server.close();
	    	}
    	}
    }
    
    protected HttpResponse doPingHelloWorld(HttpClient client, HttpGet urlPage, int retries) throws Exception {
    	if (retries < 0) {
            throw new Exception("Failed to connect. Retries exceeded.");
        }
    	HttpResponse response = null;
        try {
            Thread.sleep(sleepTimeout); 
            response = client.execute(urlPage);
        } catch (HttpHostConnectException e) {
            try {
                response = client.execute(urlPage);
            } catch (HttpHostConnectException e2) {
                response = doPingHelloWorld(client, urlPage, --retries);
            }
        }
        return response;
    }
    
   private Injector startServer() throws Exception {
        server = new KaryonServer();
        Injector injector = server.initialize();
        server.start();
        return injector;
    }    
    
}