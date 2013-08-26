package com.liaison.service.resources.examples;

import com.liaison.framework.fs2.api.CoreFS2Utils;
import com.liaison.framework.fs2.api.FS2Factory;
import com.liaison.framework.fs2.api.FS2MetaSnapshot;
import com.liaison.framework.fs2.api.FlexibleStorageSystem;
import com.liaison.framework.fs2.storage.file.FS2DefaultFileConfig;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import javax.mail.internet.MimeMultipart;
import java.io.InputStream;
import java.net.URI;
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
    private static final Logger logger = LoggerFactory.getLogger(MetricsResource.class);

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
    public Response listFile() {

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
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(final MimeMultipart file) {

        // TODO map headers too

        if (file == null)
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Must supply a valid file.").build();

        FS2MetaSnapshot object = null;

        try {
            // TODO with this implementation, the last "part"
            // is the only part that gets written... need
            // to build more robust solution here...
            for (int i = 0; i < file.getCount(); i++) {
                javax.mail.BodyPart bp = file.getBodyPart(i);

                URI newURI = CoreFS2Utils.appendLeaf(rootURI, "/" + bp.getFileName());

                object = FS2.createObjectEntry(newURI);

                InputStream part = bp.getInputStream();

                FS2.writePayloadFromStream(object.getURI(), part);

            }
            return Response.ok("FS2 wrote " + object.getURI()).build();
        } catch (final Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e)
                    .build();
        }
    }

    /*  TODO Had trouble with this method (see stack overflow)
    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_JSON})
    public Response uploadFile(
            @FormDataParam("file") InputStream is,
            @FormDataParam("file2") FormDataContentDisposition detail) {

        String name = detail.getFileName();

        FS2MetaSnapshot object = null;
        try {
            // create entry
            object = fileRepo.createObjectEntry("/temp/" + name);
            // write file
            fileRepo.writePayloadFromStream(object.getURI(), is);
        } catch (Exception e) {
            logger.error("Error creating json response.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        String output = "File uploaded to : " + object.getURI();

        return Response.status(200).entity(output).build();
    }
    */

}
