package com.netflix.adminresources;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.inject.Injector;
import com.netflix.config.ConfigurationManager;
import com.netflix.karyon.server.KaryonServer;
import com.netflix.karyon.server.eureka.SyncHealthCheckInvocationStrategy;
import com.netflix.karyon.spi.PropertyNames;

public class AdminURLSTest {

	public static final int LISTEN_PORT_DEFAULT = 8077;
	private static final int httpRetries = 10;
	private static final long sleepTimeout = 1000;
	// comment the below when the embedded jetty component is completely removed.
	private static final String adminEndPoint = "/admin";
	private static final String adminJars = "/admin#view=jars&";
	private static final String adminJmx = "/admin#view=jmx&";
	private static final String adminProps = "/admin#view=props&";
	private static final String adminEureka = "/admin#view=eureka&";
	private static final String healthCheck = "/healthcheck";

	// Un-comment the below , when the embedded jetty component is completely removed 
	/*
	 * private static final String adminEndPoint = "/hello-world/rest/admin";
	 * private static final String adminJars = "/hello-world/rest/admin#view=jars&"; 
	 * private static final String adminJmx = "/hello-world/rest/admin#view=jmx&"; 
	 * private static final String adminProps = "/hello-world/rest/admin#view=props&"; 
	 * private static final String adminEureka = "/hello-world/rest/admin#view=eureka&";
	 * private static final String healthCheck = "/healthcheck";
	 */

	private static KaryonServer server;

	@BeforeClass
	public void setUp() throws Exception {
		System.setProperty(PropertyNames.SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE,"com.test");
		System.setProperty(PropertyNames.HEALTH_CHECK_TIMEOUT_MILLIS, "60000");
		System.setProperty(PropertyNames.HEALTH_CHECK_STRATEGY,SyncHealthCheckInvocationStrategy.class.getName());
		System.setProperty(PropertyNames.DISABLE_EUREKA_INTEGRATION, "true");
	}

	@org.testng.annotations.AfterClass
	public void tearDown() throws Exception {
		ConfigurationManager.getConfigInstance().clearProperty(PropertyNames.DISABLE_APPLICATION_DISCOVERY_PROP_NAME);
		ConfigurationManager.getConfigInstance().clearProperty(PropertyNames.EXPLICIT_APPLICATION_CLASS_PROP_NAME);
		server.close();
	}

	@Test
	public void testAdmin() throws Exception {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet URLAdmin = new HttpGet("http://localhost:"+ LISTEN_PORT_DEFAULT + adminEndPoint);
			startServer();
			HttpResponse response = doPingAdmin(client, URLAdmin, httpRetries);
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		} finally {
			if (server != null) {
				server.close();
			}
		}
	}

	@Test
	public void testAdminJars() throws Exception {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet URLAdminJars = new HttpGet("http://localhost:"+ LISTEN_PORT_DEFAULT + adminJars);
			startServer();
			HttpResponse response = doPingAdmin(client, URLAdminJars,httpRetries);
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		} finally {
			if (server != null) {
				server.close();
			}
		}
	}

	@Test
	public void testAdminJMX() throws Exception {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet URLAdminJmx = new HttpGet("http://localhost:"+ LISTEN_PORT_DEFAULT + adminJmx);
			startServer();
			HttpResponse response = doPingAdmin(client, URLAdminJmx,httpRetries);
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		} finally {
			if (server != null) {
				server.close();
			}
		}
	}

	@Test
	public void testAdminProps() throws Exception {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet URLAdminProps = new HttpGet("http://localhost:"+ LISTEN_PORT_DEFAULT + adminProps);
			startServer();
			HttpResponse response = doPingAdmin(client, URLAdminProps,httpRetries);
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		} finally {
			if (server != null) {
				server.close();
			}
		}
	}

	@Test
	public void testAdminEureka() throws Exception {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet URLAdminEureka = new HttpGet("http://localhost:"+ LISTEN_PORT_DEFAULT + adminEureka);
			startServer();
			HttpResponse response = doPingAdmin(client, URLAdminEureka,httpRetries);
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		} finally {
			if (server != null) {
				server.close();
			}
		}
	}

	@Test
	public void testHealthCheck() throws Exception {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet URLHealthCheck = new HttpGet("http://localhost:"+ LISTEN_PORT_DEFAULT + healthCheck);
			startServer();
			HttpResponse response = doPingAdmin(client, URLHealthCheck,httpRetries);
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		} finally {
			if (server != null) {
				server.close();
			}
		}
	}

	protected HttpResponse doPingAdmin(HttpClient client, HttpGet URLPage,int retries) throws Exception {
		if (retries < 0) {
			throw new Exception("Failed to connect. Retries exceeded.");
		}
		HttpResponse response = null;
		try {
			Thread.sleep(sleepTimeout);
			response = client.execute(URLPage);
		} catch (HttpHostConnectException e) {
			try {
				response = client.execute(URLPage);
			} catch (HttpHostConnectException e2) {
				response = doPingAdmin(client, URLPage, --retries);
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