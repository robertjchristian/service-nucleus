package com.liaison.framework.fs2.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.Set;

import org.junit.Test;

/**
 * Positive case exercise of the FS2 API.
 * 
 * For each concrete storage provider implementation (and various configurations
 * within), this test can be extended.
 * 
 * For each provider passing this tests says
 * "Looks decent from a high level, positive case perspective"
 * 
 * @author robert.christian
 * 
 */

public abstract class AbstractAPITest {

  FlexibleStorageSystem FS2 = getFS2Instance();

  protected abstract FlexibleStorageSystem getFS2Instance();

  @Test
  public void testBasicCreateAndDelete() throws Exception {
    // create a new entry and assert it exists
    FS2MetaSnapshot m = FS2.createObjectEntry("/foo");
    assertTrue(FS2.exists(m.getURI()));

    // delete it, and assert it does not exist
    FS2.delete(m.getURI());
    assertFalse(FS2.exists(m.getURI()));
  }

  @Test
  public void testFetchByPath() throws Exception {

    // create a new entry and assert it exists
    FS2MetaSnapshot m = FS2.createObjectEntry("/test/fetch/by/path");
    assertTrue(FS2.exists(m.getURI()));

    // also make sure it can be fetched by path
    m = FS2.fetchObject("/test/fetch/by/path");
    assertTrue(null != m); // actually throws, so we wouldn't see this

    // delete it, and assert it does not exist
    FS2.delete(m.getURI());
    assertFalse(FS2.exists(m.getURI()));
  }

  @Test
  public void testBasicListChildren() throws Exception {

    // basic test... should list child and not grandchild
    FS2MetaSnapshot foo = FS2.createObjectEntry("/foo");
    FS2.createObjectEntry("/foo/bar");
    FS2.createObjectEntry("/foo/bar/bat");

    Set<URI> rs = FS2.listChildrenURIs(foo.getURI());
    assertTrue(rs.size() == 1 && rs.toArray()[0].toString().equals("fs2:/foo/bar"));
  }

  @Test
  public void testCreateAndDelete() throws Exception {
    FS2MetaSnapshot m = FS2.createObjectEntry("/foo");
    assertTrue(FS2.exists(m.getURI()));

    FS2.delete(m.getURI());
    assertFalse(FS2.exists(m.getURI()));

  }

  @Test
  public void testCreateNoNameProvided() throws Exception {

    // sometimes we want to create an object but don't care abut the object name
    // like in the case of temp storage
    FS2MetaSnapshot node = FS2.createObjectEntry();
    
    // make sure exists
    assertTrue(FS2.exists(node.getURI()));
    
    FS2.delete(node.getURI());    
  }
  
  @Test
  public void testGroupCreate() throws Exception {

    // create five nodes
    FS2MetaSnapshot[] nodes = FS2.createObjectEntries("/foo", "/foo/bar", "/foo/baz", "/foo/bar/bam", "/foo/bar/moo");

    // can access meta like this
    FS2MetaSnapshot foo = nodes[0];
    FS2MetaSnapshot bar = nodes[1];

    // list descendants of foo whose names begin with "m" (expect moo)
    Set<FS2MetaSnapshot> fooDescendants = FS2.listDescendants(foo.getURI(), ".*/m.*");
    assertTrue(fooDescendants.size() == 1);
    assertTrue(fooDescendants.iterator().next().getURI().toString().endsWith("/moo"));

    // also test no filter (expect all four returned)
    fooDescendants = FS2.listDescendants(foo.getURI(), null);
    assertTrue(fooDescendants.size() == 4);

    // delete bar and bar/bam
    FS2.deleteRecursive(bar.getURI());

    // get foo's remaining descendants. (expect just baz)
    Set<FS2MetaSnapshot> d = FS2.listDescendants(nodes[0].getURI());
    assertTrue(d.size() == 1);

    // refetch baz
    foo = FS2.fetchObject("/foo/baz");
  }

  @Test
  public void testDeleteParentAlsoDeletesDescendants() throws Exception {

    // create a tree of objects
    FS2MetaSnapshot root = FS2.createObjectEntry("/root");
    FS2MetaSnapshot a = FS2.createObjectEntry("/root/A");
    FS2MetaSnapshot b = FS2.createObjectEntry("/root/B");
    FS2MetaSnapshot c = FS2.createObjectEntry("/root/C");
    FS2MetaSnapshot d = FS2.createObjectEntry("/root/C/D");
    FS2MetaSnapshot e = FS2.createObjectEntry("/root/C/E");
    FS2MetaSnapshot f = FS2.createObjectEntry("/root/C/E/F");

    // sanity check their existence
    assertTrue(FS2.exists(root.getURI(), a.getURI(), b.getURI(), c.getURI(), d.getURI(), e.getURI(), f.getURI()));

    // delete node e and assert that both e and f are deleted
    FS2.deleteRecursive(e.getURI());
    assertFalse(FS2.exists(e.getURI(), f.getURI()));
    assertTrue(FS2.exists(root.getURI(), a.getURI(), b.getURI(), c.getURI(), d.getURI())); // sanity

    // delete node c and make sure c and all of its descendants are deleted
    FS2.deleteRecursive(c.getURI());
    assertFalse(FS2.exists(c.getURI(), d.getURI(), e.getURI(), f.getURI()));
    assertTrue(FS2.exists(root.getURI(), a.getURI(), b.getURI())); // sanity

    // delete root and make sure all are gone
    FS2.deleteRecursive(root.getURI());
    assertFalse(FS2.exists(root.getURI(), a.getURI(), b.getURI(), c.getURI(), d.getURI(), e.getURI(), f.getURI()));

  }

  @Test
  public void testDeleteVersusDeleteRecursive() throws Exception {
    // create a tree of objects
    FS2MetaSnapshot root = FS2.createObjectEntry("/root");
    FS2MetaSnapshot a = FS2.createObjectEntry("/root/A");
    FS2MetaSnapshot b = FS2.createObjectEntry("/root/A/B");
    FS2MetaSnapshot c = FS2.createObjectEntry("/root/A/B/C");

    // sanity check their existence
    assertTrue(FS2.exists(root.getURI(), a.getURI(), b.getURI(), c.getURI()));

    // test delete with child
    try {
      FS2.delete(b.getURI());
      fail("Expected that delete b fails because b has a child (c).");
    } catch (Exception exception) {
        // satisfying PMD "no empty catch rule", sigh...
        assertTrue(true);
    }

    // test delete with child and descendants
    try {
      FS2.delete(root.getURI());
      fail("Expected that delete root fails because root has descendants (a, b, c).");
    } catch (Exception exception) {
      // satisfying PMD "no empty catch rule", sigh...
      assertTrue(true);
    }

    // test delete recursive
    FS2.deleteRecursive(b.getURI());
    FS2.deleteRecursive(root.getURI());

  }

  @Test
  public void testDeleteWithPayload() throws Exception {
    // create a new entry and assert it exists
    FS2MetaSnapshot m = FS2.createObjectEntry("/foo");
    assertTrue(FS2.exists(m.getURI()));

    // create a payload, assert that it exists and that contents are as expected
    FS2.writePayloadFromBytes(m.getURI(), "foo".getBytes());
    assertTrue(new String(FS2.readPayloadToBytes(m.getURI())).equals("foo"));

    FS2.delete(m.getURI());
    assertFalse(FS2.exists(m.getURI()));

    // TODO create a child and test delete fail / delete recursive

    try {
      FS2.getFS2PayloadInputStream(m.getURI());
      fail("Expected exception... " + m + " should not exist.");
    } catch (FS2PayloadNotFoundException e) {
        // satisfying PMD "no empty catch rule", sigh...
        assertTrue(true);
    }
  }

  public void testExample() throws Exception {
    // get an instance of fs2 with default properties, using the in-memory
    // storage provider
    FlexibleStorageSystem FS2 = FS2Factory.newInstance("mem");

    // build a "root" bucket and create three objects within it
    // use convenience method to create several at one time
    FS2MetaSnapshot[] tree = FS2.createObjectEntries("/root", "/root/A", "/root/B", "/root/C");
    FS2MetaSnapshot root = tree[0];
    FS2MetaSnapshot b = tree[2];

    // add a custom field to object root
    FS2.addHeader(root.getURI(), "isContainer", "true");

    // add contents to B
    // note that if we were using a non-memory storage provider,
    // we would use the stream interface here instead
    FS2.writePayloadFromBytes(b.getURI(), "hello world".getBytes());

    // delete everything we just created
    FS2.deleteRecursive(root.getURI());

  }

  @Test
  public void testImplicitCreate() throws Exception {
    FS2.createObjectEntries("/implicit/a/b/c");
    assertTrue(FS2.exists("/implicit"));
  }

  @Test
  public void testFilteredListChildren() throws Exception {

    // create a small tree
    FS2MetaSnapshot baz = FS2.createObjectEntry("/baz");
    FS2MetaSnapshot moo = FS2.createObjectEntry("/baz/moo");
    FS2MetaSnapshot boo = FS2.createObjectEntry("/baz/boo");
    FS2MetaSnapshot mar = FS2.createObjectEntry("/baz/mar");
    FS2MetaSnapshot car = FS2.createObjectEntry("/baz/mar/car");

    // baz has three children, mar, boo, and mar. we're matching on everything
    // here so we should see the same result as if we called listChildren
    // without a filter
    Set<URI> rs = FS2.listChildrenURIs(baz.getURI(), ".*");
    assertTrue(rs.size() == 3 && rs.contains(moo.getURI()) && rs.contains(boo.getURI()) && rs.contains(mar.getURI()));

    // now test for baz children ending with letters oo, expecting just moo and
    // boo
    rs = FS2.listChildrenURIs(baz.getURI(), ".*oo");
    assertTrue(rs.size() == 2 && rs.contains(moo.getURI()) && rs.contains(boo.getURI()));

    // look for children of mar matching /baz/mar/car literally, expecting that
    // exact result
    rs = FS2.listChildrenURIs(mar.getURI(), "/baz/mar*/car");
    assertTrue(rs.size() == 1 && rs.contains(car.getURI()));

  }

  @Test
  public void testHeadersAndPayload() throws Exception {

    // create object with headers and payload
    FS2MetaSnapshot m = FS2.createObjectEntry("/foo");
    FS2.addHeader(m.getURI(), "a", "b");
    FS2.writePayloadFromBytes(m.getURI(), "payload".getBytes());
    assertTrue(new String(FS2.readPayloadToBytes(m.getURI())).equals("payload"));

    // test read headers
    String[] mh = FS2.getHeader(m.getURI(), "a");
    assertTrue(mh[0].equals("b"));
    Set<String> names = FS2.getHeaderNames(m.getURI());
    assertTrue(names.contains("a"));
    assertTrue(names.size() == 1);

    // test multiple headers (order not guaranteed)
    FS2.addHeader(m.getURI(), "a", "c");
    String[] values = FS2.getHeader(m.getURI(), "a");

    // note: see the difference between refetching from persistence store,
    // and querying the object itself... here we refetch
    assertTrue(values[0].equals("c") || values[1].equals("c"));

    // test remove headers

    // get the original headers
    FS2ObjectHeaders h = FS2.getHeaders(m.getURI());
    h.removeHeader("a"); // overwrite/remove

    // update
    FS2.updateHeaders(m.getURI(), h);

    assertTrue(FS2.getHeaderNames(m.getURI()).size() == 0);

    // test remove payload
    FS2.deletePayload(m.getURI());

    try {
      FS2.getFS2PayloadInputStream(m.getURI());
      fail("Expected exception obtaining payload.");
    } catch (Exception e) {
        // satisfying PMD "no empty catch rule", sigh...
        assertTrue(true);
    }

    // cleanup
    FS2.delete(m.getURI());
  }

  @Test
  public void testListDescendants() throws Exception {

    // for this test, we'll create a tree called baz
    URI root = CoreFS2Utils.createObjectURI(new String[] { "baz" });

    // precondition: make sure baz does not already exist
    if (FS2.exists(root)) {
      FS2.deleteRecursive(root);
    }

    // create the tree
    FS2.createObjectEntry("/baz/mar/car/baz/moo/foo");
    FS2.createObjectEntry("/baz/jar/car/baz/moo/foo");
    FS2.createObjectEntry("/baz/foo");

    // at this point, the root (baz) has the following 11 descendants:
    //
    // /baz/mar
    // /baz/mar/car
    // /baz/mar/car/baz
    // /baz/mar/car/baz/moo
    // /baz/mar/car/baz/foo
    // /baz/jar
    // /baz/jar/car
    // /baz/jar/car/baz
    // /baz/jar/car/baz/moo
    // /baz/jar/car/baz/foo
    // /baz/foo

    // with a match everything filter, we expect all 11 descendants
    Set<URI> rs = FS2.listDescendantURIs(root, ".*");

    assertTrue(rs.size() == 11);

    // match nodes named foo
    rs = FS2.listDescendantURIs(root, ".*/foo");
    assertTrue(rs.size() == 3);

    // match nodes named baz
    rs = FS2.listDescendantURIs(root, ".*/baz");
    assertTrue(rs.size() == 2);

    // match nodes named car or contained by car
    rs = FS2.listDescendantURIs(root, ".*/car.*");
    assertTrue(rs.size() == 8);

  }

  @Test
  public void testMoveSingleNode() throws Exception {

    // create entry and add payload
    FS2MetaSnapshot m = FS2.createObjectEntry("/foo");
    FS2.writePayloadFromBytes(m.getURI(), "xyz".getBytes());

    // move it
    URI newURI = CoreFS2Utils.createObjectURI("bar", "mars");

    FS2.move(m.getURI(), newURI);

    // old entry should be gone
    assertFalse(FS2.exists(m.getURI()));

    // test entry and payload exist at new location
    m = FS2.fetchObject(newURI);
    assertNotNull(m);
    assertTrue(new String(FS2.readPayloadToBytes(m.getURI())).equals("xyz"));

    // cleanup
    FS2.delete(m.getURI());

  }

  @Test
  public void testMoveToExistingURI() throws Exception {
    // create entries
    FS2MetaSnapshot m = FS2.createObjectEntry("/baz/bar");
    FS2MetaSnapshot m2 = FS2.createObjectEntry("/moo");

    // move it
    URI newURI = CoreFS2Utils.createObjectURI("moo");

    try {
      FS2.move(m.getURI(), newURI);
      fail("Expected exception moving to existing uri.");
    } catch (FS2ObjectAlreadyExistsException e) {
        // satisfying PMD "no empty catch rule", sigh...
        assertTrue(true);
    }

    // cleanup
    FS2.delete(m.getURI());
    FS2.delete(m2.getURI());
  }

  // this tests not only move with descendants but implicitly the implicit
  // creation of nodes
  @Test
  public void testMoveWithDescendants() throws Exception {

    FS2MetaSnapshot bar = FS2.createObjectEntry("/baz/bar");
    FS2MetaSnapshot foo = FS2.createObjectEntry("/baz/foo");
    FS2MetaSnapshot doo = FS2.createObjectEntry("/baz/foo/doo");

    URI root = CoreFS2Utils.createObjectURI(new String[] { "baz" });
    URI newRoot = CoreFS2Utils.createObjectURI(new String[] { "moo" });

    FS2.move(root, newRoot);
    assertTrue(FS2.exists(newRoot));

    // TODO should be able to fetch with (ie) simply "/moo/bar"
    bar = FS2.fetchObject(CoreFS2Utils.createObjectURI(new String[] { "moo", "bar" }));
    foo = FS2.fetchObject(CoreFS2Utils.createObjectURI(new String[] { "moo", "foo" }));
    doo = FS2.fetchObject(CoreFS2Utils.createObjectURI(new String[] { "moo", "foo", "doo" }));

    assertNotNull(bar);
    assertNotNull(foo);
    assertNotNull(doo);

    // cleanup
    FS2.delete(bar.getURI());
    FS2.delete(doo.getURI());
    FS2.delete(foo.getURI());

  }

  @Test
  public void testCopy() throws Exception {

    FlexibleStorageSystem fs2 = getFS2Instance();

    /**
     * STEP 1: Build a tree at fs2:/f1
     */

    // create a tree with 2 children and 10 total descendants
    FS2MetaSnapshot[] fileMeta = fs2.createObjectEntries("f1/f2/f3/f4/f5/f6/f7/f8", "f1/f2/f3/f4/f5/f6/f7/f9", "f1/f11/f12");

    // create headers on f9
    FS2MetaSnapshot f9 = fileMeta[2];
    fs2.addHeader(f9.getURI(), "name", "f9");

    // create payload on f11
    FS2MetaSnapshot f11 = fs2.fetchObject("f1/f11");
    fs2.writePayloadFromBytes(f11.getURI(), "HelloWorld".getBytes());

    /**
     * STEP 2: Copy fs2:/f1 to fs2:/copy
     */

    fs2.copy("/f1", "/copy");

    FS2MetaSnapshot copiedRoot = fs2.fetchObject("/copy");

    // assert that mem root has the 2 children and ten descendants that we
    // created on the filesystem
    Set<FS2MetaSnapshot> children = fs2.listChildren(copiedRoot.getURI());
    Set<FS2MetaSnapshot> descendants = fs2.listDescendants(copiedRoot.getURI());

    assertTrue(children.size() == 2);
    assertTrue(descendants.size() == 10);

    // test header
    f9 = fs2.fetchObject(f9.getURI());
    String hVal = f9.getHeader("name")[0];
    assertTrue(hVal.equals("f9"));

    // test payload
    f11 = fs2.fetchObject("f1/f11");
    byte[] payload = fs2.readPayloadToBytes(f11.getURI());
    assertTrue(new String(payload).equals("HelloWorld"));

  }

}
