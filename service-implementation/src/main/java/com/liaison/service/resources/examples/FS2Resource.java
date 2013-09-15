package com.liaison.service.resources.examples;

import com.liaison.framework.fs2.api.*;
import com.liaison.framework.fs2.storage.file.FS2DefaultFileConfig;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Header;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import javax.mail.internet.MimeMultipart;
import java.io.InputStream;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: rob
 * Date: 8/24/13
 * Time: 8:44 PM
 * To change this template use File | Settings | File Templates.
 */

@Path("v1/fs2")
public class FS2Resource {

    // override default and force an instance backed by the "file" storage provider
    private static final FlexibleStorageSystem FS2 = FS2Factory.newInstance(new FS2DefaultFileConfig());
    private static final Logger logger = LoggerFactory.getLogger(FS2Resource.class);

    // root mount point for this resource
    private static final URI rootURI;

    static {
        try {
            rootURI = FS2.createObjectEntry("FS2Resource").getURI();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    // TODO for ui, prefer websocket for repo browsing (list)
    // TODO instead of polling this service
    public Response listFiles() {

        JSONObject response = new JSONObject();

        try {

            Set<URI> uris = FS2.listDescendantURIs(rootURI);
            for (URI u : uris) {

                // TODO better (explicit) check for "is directory" (HACK!)
               try {
                    FS2.getFS2PayloadInputStream(u);
               } catch (Exception e) {
                 // skip returning buckets
                 continue;
               }

                // copy headers over
                java.util.Map<String, String> entry = new HashMap<String, String>();

                FS2ObjectHeaders headers = FS2.getHeaders(u);
                for (String key : headers.getHeaders().keySet()) {
                    // TODO only returning first value
                    String value = headers.getHeaders().get(key).get(0);
                    entry.put(key, value);
                }

                response.put(u.toASCIIString(), entry);

            }
            return Response.ok(response.toString()).build();
        } catch (final Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e)
                    .build();
        }
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    // Allows fetch of resource via POST
    public InputStream getResource(JSONObject obj) {
        try {

            String requestedURI = obj.getString("uri");
            logger.error("Client requested: " + requestedURI);

            // build URI, supporting both absolute and relative URI
            URI u = null;
            if (FS2.exists(new URI(requestedURI))) {
               u = new URI(requestedURI);
            } else if (FS2.exists(CoreFS2Utils.appendLeaf(rootURI, requestedURI))) {
               u = CoreFS2Utils.appendLeaf(rootURI, requestedURI);
            }

            InputStream is = FS2.getFS2PayloadInputStream(u);

            return is;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    // Allows fetch of resource via POST
    public Response deleteResource(JSONObject obj) {
        try {

            String requestedURI = obj.getString("uri");
            logger.error("Client requested to delete: " + requestedURI);

            // build URI, supporting both absolute and relative URI
            // TODO move to convenience method
            URI u = null;
            if (FS2.exists(new URI(requestedURI))) {
                u = new URI(requestedURI);
            } else if (FS2.exists(CoreFS2Utils.appendLeaf(rootURI, requestedURI))) {
                u = CoreFS2Utils.appendLeaf(rootURI, requestedURI);
            }

            FS2.delete(u);

            JSONObject response = new JSONObject();
            response.put("Deleted: ", u);
            return Response.ok(response.toString()).build();


        } catch(Exception e) {
            // TODO friendly error handling (TODO applies to entire resource)
            throw new RuntimeException(e);
        }
    }


    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("download")
    // Allows fetch of resource via GET
    public InputStream getResource(@Context UriInfo uriInfo) {
        try {
            String requestedURI = uriInfo.getQueryParameters().getFirst("uri");

            // build URI, supporting both absolute and relative URI
            URI u = null;
            if (FS2.exists(new URI(requestedURI))) {
                u = new URI(requestedURI);
            } else if (FS2.exists(CoreFS2Utils.appendLeaf(rootURI, requestedURI))) {
                u = CoreFS2Utils.appendLeaf(rootURI, requestedURI);
            }

            logger.error("Client requested: " + u.toASCIIString());
            InputStream is = FS2.getFS2PayloadInputStream(u);
            return is;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(@Context HttpHeaders headers, final MimeMultipart parts) {

        FS2MetaSnapshot object = null;

        try {

            /*
            Make sure we have an MPM with a file
             */

            // expect exactly one part, containing the file
            javax.mail.BodyPart bp = parts.getBodyPart(0);
            String fileName = bp.getFileName();

            if (null == fileName) {
                throw new RuntimeException("Expected MultipartMime with a file part.  Mising filename.");
            }

            /*
            Create the FS2 object
             */

            // determine uri (determined by header, fallback to filename)
            String relativeUri = headers.getRequestHeaders().getFirst("fs2-uri");
            logger.error("Relative uri:  " + relativeUri);
            relativeUri = (null == relativeUri) ? fileName : relativeUri;

            if (!relativeUri.startsWith("/")) {
                relativeUri = "/" + relativeUri;
            }

            URI newURI = CoreFS2Utils.appendLeaf(rootURI, relativeUri);

            object = FS2.createObjectEntry(newURI);


            /*
            Copy payload (file) and metadata to FS2 object
             */

            // payload
            InputStream part = bp.getInputStream();
            FS2.writePayloadFromStream(object.getURI(), part);

            // meta
            MultivaluedMap<String, String> headerMap = headers.getRequestHeaders();
            for (String name : headerMap.keySet()) {
                if (name.startsWith("fs2-")) {
                    // TODO do we want to copy all values if > 1?
                    String value = headerMap.getFirst(name);
                    FS2.addHeader(object.getURI(), name, value);
                }

            }
            // special header retaining filename (TODO: should model explicitly?)
            FS2.addHeader(object.getURI(), "fs2-original-filename", fileName);

            return Response.ok("FS2 wrote " + object.getURI()).build();

        } catch (final Exception e) {
            logger.error(e.getLocalizedMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e)
                    .build();
        }
    }

}
