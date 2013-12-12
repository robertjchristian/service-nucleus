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

package com.liaison.service.resources.examples;

import com.wordnik.swagger.annotations.*;
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

/**
 * HelloWorldResource
 *
 * <P>Simple HelloWorld REST service example
 *
 * <P>For dynamically described endpoints, @see com.liaison.framework.dynamic.DynamicServicesServlet
 *
 * @author Robert.Christian
 * @version 1.0
 */
@Api(value="v1/hello", description="hello world resource") //swagger resource annotation
@Path("v1/hello")
public class HelloWorldResource {

    private static final Logger logger = LoggerFactory.getLogger(HelloWorldResource.class);

    @ApiOperation(value="hello to given name", notes="this typically returns a string of greeting")
    @Path("to/{name}")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response helloTo(
    		@ApiParam(value="name of the person who is to be greeted", required=true)
    		@PathParam("name") String name) {
        JSONObject response = new JSONObject();
        try {
            response.put("Message", "Hello " + name + "!");
            return Response.ok(response.toString()).build();
        } catch (JSONException e) {

            logger.error("Error creating json response.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ApiOperation(value="hello to the world", notes="this returns a well known programming trope")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response hello() {
        JSONObject response = new JSONObject();
        try {
            response.put("Message", "Hello world!");
            return Response.ok(response.toString()).build();
        } catch (JSONException e) {

            logger.error("Error creating json response.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
