package com.liaison.framework.fs2.api;

import java.net.URI;
import java.util.Date;

import junit.framework.TestCase;

/**
 * Test meta to/from JSON.
 * 
 * @author robert.christian
 * 
 */
public class MetadataSerializationTest extends TestCase {

  public void testEqualityAfterRehydration() throws Exception {

    // create an fs2Obj
    URI uri = CoreFS2Utils.createURI("test", new String[] { "a", "b", "c" });

    FS2ObjectHeaders headers = new FS2ObjectHeaders();
    headers.addHeader("foo", "bar");
    FS2MetaSnapshotImpl foo = new FS2MetaSnapshotImpl(uri, new Date(), this.getName(), headers);

    String s = foo.toJSON();

    // read it back
    FS2MetaSnapshot bar = CoreFS2Utils.fromJSON(s, FS2MetaSnapshotImpl.class);

    boolean asssertSuccess = foo.toJSON().equals(bar.toJSON());

    assert asssertSuccess;

  }

}
