package com.netflix.adminresources;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
/* author Senthilvel
This is a Listener(monitor program for Guice Container class) which needs 
to be configured in the web.xml
If the embedded jetty server is stopped , this class will take care of the admin console 
this is equavellent to starting the embedded jetty server & monitoring in the default port.
*/

public class MyGuiceServletConfig extends GuiceServletContextListener
{    
	@Override   
	protected Injector getInjector() { 
	    return Guice.createInjector(new ServletModule());
	} 
}