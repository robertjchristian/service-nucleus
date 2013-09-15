package com.liaison.service.resources.examples;

import com.liaison.framework.fs2.api.CoreFS2Utils;
import com.liaison.framework.fs2.api.FS2Factory;
import com.liaison.framework.fs2.api.FS2MetaSnapshot;
import com.liaison.framework.fs2.api.FlexibleStorageSystem;
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
    public Response listFiles() {

        JSONObject response = new JSONObject();

        try {
            int i =0;
            Set<URI> uris = FS2.listDescendantURIs(rootURI);
            for (URI u : uris) {
                response.put("uri_" + i++, u.toASCIIString());
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
    public InputStream getResource(JSONObject obj) {
        try {
            URI uri = new URI(obj.getString("uri"));
            logger.error(uri.toASCIIString());
            InputStream is = FS2.getFS2PayloadInputStream(uri);
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

            return Response.ok("FS2 wrote " + object.getURI()).build();

        } catch (final Exception e) {
            logger.error(e.getLocalizedMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e)
                    .build();
        }
    }

}
