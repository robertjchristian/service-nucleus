package com.liaison.service.resource.examples;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class FS2ResourceTest {
	
	public static final int LISTEN_PORT_DEFAULT = 8989;
    private static final int httpRetries = 10;
    private static final long sleepTimeout = 1000;    
    private static final String fs2EndPoint  = "/hello-world/#/fs2";

   
    
    @Test
    public void testFS2() throws Exception {
    	
    		HttpClient client = new DefaultHttpClient();
	   		HttpGet fs2URL = new HttpGet("http://localhost:" + LISTEN_PORT_DEFAULT + fs2EndPoint);	   		
	    	HttpResponse response = testPing(client, fs2URL, httpRetries);
	    	AssertJUnit.assertEquals( 200, response.getStatusLine().getStatusCode());
    	
    }
    
    protected HttpResponse testPing(HttpClient client, HttpGet urlPage, int retries) throws Exception {
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
                response = testPing(client, urlPage, --retries);
            }
        }
        return response;
    }

}
