package com.liaison.framework.dynamic;

/**
 * Dynamic Bindings
 * <p/>
 * <P>POJO Model of the Dynamic Services bindings JSON descriptor
 *
 * @author Robert.Christian
 * @version 1.0
 */
class DynamicBinding {

    public DynamicBinding(String serviceName, About about, Operation[] operations, String baseURI, String scriptLocation) {
        this.serviceName = serviceName;
        this.about = about;
        this.operations = operations;
        this.baseURI = baseURI;
        this.scriptLocation = scriptLocation;
    }

    public About about;
    public Operation[] operations;
    public String baseURI;
    public String scriptLocation;
    public String serviceName;

}

class About {
    public About(String author, String description) {
        this.author = author;
        this.description = description;
    }

    public String author;
    public String description;
}

class Operation {
    public Operation(String operationUrl, String[] allowedMethods) {
        this.operationUrl = operationUrl;
        this.allowedMethods = allowedMethods;
    }

    public String operationUrl;
    public String[] allowedMethods;
}

public class DynamicBindings {

    public DynamicBindings(DynamicBinding[] bindings) {
        this.bindings = bindings;
    }

    public DynamicBinding[] bindings;

    // development-time utility method to build example bindings
    public static DynamicBindings buildMockBindings() {
        Operation[] operations = new Operation[]{
                new Operation("multiply/{a}/{b}", new String[]{"GET", "POST"}),
                new Operation("divide/{a}/{b}", new String[]{"GET", "POST"})
        };
        About a = new About("rchristian@liaison", "simple hello world com.liaison.service");
        DynamicBinding binding = new DynamicBinding("HelloWorldService", a, operations, "/v1/math", "classpath://scripts/foo.js");
        DynamicBinding[] bindings = new DynamicBinding[]{binding, binding};
        DynamicBindings db = new DynamicBindings(bindings);
        return db;
    }

}
