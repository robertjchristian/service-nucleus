package com.netflix.karyon.spi;

/**
 * A set of property names that Karyon uses.
 *
 * @author Nitesh Kant (nkant@netflix.com)
 */
public class PropertyNames {

    public static final String KARYON_PROPERTIES_PREFIX = "com.netflix.karyon.";

    public static final String EXPLICIT_APPLICATION_CLASS_PROP_NAME = KARYON_PROPERTIES_PREFIX + "app.class";

    public static final String DISABLE_APPLICATION_DISCOVERY_PROP_NAME = KARYON_PROPERTIES_PREFIX + "disable.app.discovery";

    public static final String EUREKA_PROPERTIES_NAME_PREFIX_PROP_NAME = KARYON_PROPERTIES_PREFIX + "eureka.properties.prefix";

    public static final String EUREKA_DATACENTER_TYPE_PROP_NAME = KARYON_PROPERTIES_PREFIX + "eureka.datacenter.type";

    public static final String EXPLICIT_COMPONENT_CLASSES_PROP_NAME = KARYON_PROPERTIES_PREFIX + "component.classes";

    public static final String SERVER_BOOTSTRAP_CLASS_OVERRIDE = KARYON_PROPERTIES_PREFIX + "server.bootstrap.class";

    public static final String SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE = KARYON_PROPERTIES_PREFIX + "server.base.packages";

    public static final String EUREKA_COMPONENT_NAME = "eureka";
}
