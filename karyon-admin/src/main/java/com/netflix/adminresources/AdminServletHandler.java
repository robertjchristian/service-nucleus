package com.netflix.adminresources;

/*import org.slf4j.Logger;
import org.slf4j.LoggerFactory;*/

import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import com.netflix.karyon.spi.Component;

@Component
public class AdminServletHandler extends ServletModule {
	//private static final Logger logger = LoggerFactory.getLogger(AdminServletHandler.class);
	public AdminServletHandler() {
	
	}
	
	@Override
	protected void configureServlets() {
		bindConstant().annotatedWith(Names.named("port")).to(8080);
		filter("/*").through(RedirectFilter.class);
		filter("/*").through(LoggingFilter.class);
		filter("/*").through(AdminResourcesFilter.class);
		serve("/*").with(AdminResourcesFilter.class);
	}
}