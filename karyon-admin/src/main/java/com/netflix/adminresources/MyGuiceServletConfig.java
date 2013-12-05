package com.netflix.adminresources;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

public class MyGuiceServletConfig extends GuiceServletContextListener
{    
	@Override   
	protected Injector getInjector() { 
	    return Guice.createInjector(new ServletModule());
	} 
}