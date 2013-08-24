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

package com.liaison.service.examples.resources;

import org.apache.commons.codec.binary.Base64;
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
 * CodecResource
 *
 * <P>Simple encoding service (also included in UI components)
 *
 * <P>For dynamically described endpoints, @see com.liaison.framework.dynamic.DynamicServicesServlet
 *
 * @author Robert.Christian
 * @version 1.0
 */

@Path("v1/codec")
public class CodecResource {

    private static final Logger logger = LoggerFactory.getLogger(MetricsResource.class);

    @Path("decode/{input}")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response decode(@PathParam("input") String input) {

        JSONObject response = new JSONObject();
        try {
            // WARNING:  USES DEFAULT ENCODING
            byte[] decoded = new Base64().decode(input.getBytes());
            response.put("Decoded", new String(decoded));
            return Response.ok(response.toString()).build();
        } catch (Exception e) {
            logger.error("Error creating json response.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path("encode/{input}")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response encode(@PathParam("input") String input) {

        JSONObject response = new JSONObject();
        try {
            // WARNING:  USES DEFAULT ENCODING
            byte[] encoded = new Base64().encode(input.getBytes());
            response.put("Encoded", new String(encoded));
            return Response.ok(response.toString()).build();
        } catch (Exception e) {
            logger.error("Error creating json response.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}