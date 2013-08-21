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

package com.liaison.service;

import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.Monitors;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * HelloworldResource
 *
 * <P>Statically defines service endpoints using Jersey.
 *
 * <P></>For dynamically described endpoints, @see com.liaison.framework.dynamic.DynamicServicesServlet
 *
 * @author Robert.Christian
 * @version 1.0
 */

@Path("v1/hello")
public class HelloworldResource {

    private static final Logger logger = LoggerFactory.getLogger(HelloworldResource.class);

    // For a more thorough metrics example, see
    // https://github.com/cfregly/fluxcapacitor/blob/master/flux-edge/src/main/java/com/fluxcapacitor/edge/jersey/resources/EdgeResource.java
    // ... this example includes reporting via Hystrix as well

    // here is another:
    // https://github.com/cfregly/fluxcapacitor/blob/master/flux-middletier/src/main/java/com/fluxcapacitor/middletier/jersey/resources/MiddleTierResource.java

    @Monitor(name = "failureCounter", type = DataSourceType.COUNTER)
    private final static AtomicInteger failureCounter = new AtomicInteger(0);

    @Monitor(name = "serviceCallCounter", type = DataSourceType.COUNTER)
    private final static AtomicInteger serviceCallCounter = new AtomicInteger(0);

    public HelloworldResource() {
        DefaultMonitorRegistry.getInstance().register(Monitors.newObjectMonitor(this));
    }

    @Path("to/{name}")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response helloTo(@PathParam("name") String name) {
        serviceCallCounter.addAndGet(1);
        JSONObject response = new JSONObject();
        try {
            response.put("Message", "Hello " + name + "!");
            return Response.ok(response.toString()).build();
        } catch (JSONException e) {
            failureCounter.addAndGet(1);
            logger.error("Error creating json response.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response hello() {
        serviceCallCounter.addAndGet(1);
        JSONObject response = new JSONObject();
        try {
            response.put("Message", "Hello world!");
            return Response.ok(response.toString()).build();
        } catch (JSONException e) {
            failureCounter.addAndGet(1);
            logger.error("Error creating json response.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}