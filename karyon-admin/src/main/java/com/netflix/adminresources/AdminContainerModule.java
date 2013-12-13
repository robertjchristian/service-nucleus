/*
 * Copyright 2013 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package com.netflix.adminresources;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.governator.annotations.Configuration;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import com.netflix.karyon.server.eureka.HealthCheckInvocationStrategy;
import com.netflix.karyon.spi.Component;
import com.netflix.karyon.spi.PropertyNames;

/* 
This class Will be treated as Guice Module (extends AbstractModule)
it is a part of AdminResourcesContainer is used to initiate the google injector 
with the specified package (com.netflix.explorers) & for healthCheckInvocationStrategyProvider
& starting the LifeCycleManager.
*/

@Component
public class AdminContainerModule extends AbstractModule{

	private static final Logger logger = LoggerFactory
			.getLogger(AdminContainerModule.class);

	public static final String DEFAULT_PAGE_PROP_NAME = PropertyNames.KARYON_PROPERTIES_PREFIX
			+ "admin.default.page";

	public static final DynamicStringProperty DEFAULT_PAGE = DynamicPropertyFactory
			.getInstance().getStringProperty(DEFAULT_PAGE_PROP_NAME,
					"/healthcheck");

	public static final String CONTAINER_LISTEN_PORT = "netflix.platform.admin.resources.port";
	public static final int LISTEN_PORT_DEFAULT = 8077;
	private static final String JERSEY_CORE_PACKAGES = "netflix.platform.admin.resources.core.packages";
	public static final String JERSEY_CORE_PACKAGES_DEAULT = "com.netflix.adminresources;com.netflix.explorers.resources;com.netflix.explorers.providers";

	private final Provider<HealthCheckInvocationStrategy> healthCheckInvocationStrategyProvider;

	@Configuration(value = JERSEY_CORE_PACKAGES, documentation = "Property defining the list of core packages which contains jersey resources for karyon admin. com.netflix.adminresources is always added to this.")
	private String coreJerseyPackages = JERSEY_CORE_PACKAGES_DEAULT;

	@Configuration(value = CONTAINER_LISTEN_PORT, documentation = "Property defining the listen port for admin resources.", ignoreTypeMismatch = true)
	private int listenPort = LISTEN_PORT_DEFAULT;

	private Server server;

	@Inject
	public AdminContainerModule(
			Provider<HealthCheckInvocationStrategy> healthCheckInvocationStrategyProvider) {
		this.healthCheckInvocationStrategyProvider = healthCheckInvocationStrategyProvider;

	}

	/**
	 * Starts the container and hence the embedded jetty server.
	 * 
	 * @throws Exception
	 *             if there is an issue while starting the server
	 */
	@PostConstruct
	public void configure() {
		 /*server = new Server(listenPort); */
		logger.info("inside init method of AdminResources Container");
		Injector injector = LifecycleInjector
				.builder()
				.usingBasePackages("com.netflix.explorers")
				.withModules(new AdminResourcesModule(healthCheckInvocationStrategyProvider)).withModules(new AdminServletHandler())
				.createInjector();
		try {
			injector.getInstance(LifecycleManager.class).start();
			logger.info("injector.getInstance()");
			AdminResourcesFilter adminResourcesFilter = injector
					.getInstance(AdminResourcesFilter.class);
			
			logger.info("injector.getInstance(AdminResourceFilter class");
			adminResourcesFilter.setPackages(coreJerseyPackages);
			logger.info("adminResourcesFilter.setPackages(coreJerseyPackages)");

		} catch (Exception e) {
			logger.error("Exception in building AdminResourcesContainer ", e);
		}
	}

	@PreDestroy
	public void shutdown() {
		try {
			server.stop();
		} catch (Throwable t) {
			logger.warn("Error while shutting down Admin resources server", t);
		}
	}
}
