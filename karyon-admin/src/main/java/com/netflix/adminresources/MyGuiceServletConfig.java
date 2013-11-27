package com.netflix.adminresources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.servlet.GuiceServletContextListener;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.governator.annotations.Configuration;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import com.netflix.karyon.server.eureka.HealthCheckInvocationStrategy;
import com.netflix.karyon.spi.PropertyNames;

public class MyGuiceServletConfig extends GuiceServletContextListener 
{    
	
	private static final Logger logger = LoggerFactory.getLogger(AdminResourcesContainer.class);

    public static final String DEFAULT_PAGE_PROP_NAME = PropertyNames.KARYON_PROPERTIES_PREFIX + "admin.default.page";

    public static final DynamicStringProperty DEFAULT_PAGE =
            DynamicPropertyFactory.getInstance().getStringProperty(DEFAULT_PAGE_PROP_NAME, "/healthcheck");

    public static final String CONTAINER_LISTEN_PORT = "netflix.platform.admin.resources.port";
    public static final int LISTEN_PORT_DEFAULT = 8077;
    private static final String JERSEY_CORE_PACKAGES = "netflix.platform.admin.resources.core.packages";
    public static final String JERSEY_CORE_PACKAGES_DEAULT = "com.netflix.adminresources;com.netflix.explorers.resources;com.netflix.explorers.providers";
    private String coreJerseyPackages = JERSEY_CORE_PACKAGES_DEAULT;
    
	@Inject
    public MyGuiceServletConfig(Provider<HealthCheckInvocationStrategy> healthCheckInvocationStrategyProvider) {
        this.healthCheckInvocationStrategyProvider = healthCheckInvocationStrategyProvider;
    }
	private final Provider<HealthCheckInvocationStrategy> healthCheckInvocationStrategyProvider;
	
	@Configuration(
	          value = JERSEY_CORE_PACKAGES,
	          documentation = "Property defining the list of core packages which contains jersey resources for karyon admin. com.netflix.adminresources is always added to this."
	)
	   
	@Override   
	protected Injector getInjector() { 
		Injector injector = LifecycleInjector
                .builder()
                .usingBasePackages("com.netflix.explorers")
                .withModules(new AdminResourcesModule(healthCheckInvocationStrategyProvider)).createInjector();
        try {
            injector.getInstance(LifecycleManager.class).start();
            logger.info("injector.getInstance()");
            AdminResourcesFilter adminResourcesFilter = injector.getInstance(AdminResourcesFilter.class);
            logger.info("injector.getInstance(AdminResourceFilter class");
            adminResourcesFilter.setPackages(coreJerseyPackages);
            logger.info("adminResourcesFilter.setPackages(coreJerseyPackages)"); 
        } catch (Exception e) {
            logger.error("Exception in building AdminResourcesContainer ", e);
        }
		return Guice.createInjector(new AdminResourcesModule(healthCheckInvocationStrategyProvider));   
	} 
	
	
}