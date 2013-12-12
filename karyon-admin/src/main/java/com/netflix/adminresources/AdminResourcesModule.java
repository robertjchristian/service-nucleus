package com.netflix.adminresources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import com.netflix.explorers.AppConfigGlobalModelContext;
import com.netflix.explorers.ExplorerManager;
import com.netflix.explorers.ExplorersManagerImpl;
import com.netflix.explorers.context.GlobalModelContext;
import com.netflix.explorers.providers.FreemarkerTemplateProvider;
import com.netflix.karyon.server.HealthCheckModule;
import com.netflix.karyon.server.eureka.HealthCheckInvocationStrategy;
class AdminResourcesModule extends ServletModule {
	private final Provider<HealthCheckInvocationStrategy> healthCheckInvocationStrategyProvider;
		
	public AdminResourcesModule(Provider<HealthCheckInvocationStrategy> healthCheckInvocationStrategyProvider) {
		this.healthCheckInvocationStrategyProvider = healthCheckInvocationStrategyProvider;
	}
	
	private static final Logger logger = LoggerFactory.getLogger(AdminResourcesModule.class);
	public static final String JERSEY_CORE_PACKAGES_DEAULT = "com.netflix.adminresources;com.netflix.explorers.resources;com.netflix.explorers.providers";

	@Override
	protected void configureServlets() {

		try {
			logger.info("inside configureServlets() of AdminResourcesModule");
			bind(String.class).annotatedWith(Names.named("explorerAppName"))
					.toInstance("admin");
			
			bind(GlobalModelContext.class).to(AppConfigGlobalModelContext.class);
			bind(ExplorerManager.class).to(ExplorersManagerImpl.class);
			bind(AdminResourceExplorer.class);
			bind(AdminResourcesFilter.class).asEagerSingleton();
			bind(FreemarkerTemplateProvider.class);
			if (healthCheckInvocationStrategyProvider != null) {
				 bind(HealthCheckInvocationStrategy.class).toProvider(
				 healthCheckInvocationStrategyProvider);
			} else {
				 install(new HealthCheckModule());
			}
		} catch (Exception e) {
			logger.error("Exception in building AdminResourcesContainer ", e);
		}
	}
}