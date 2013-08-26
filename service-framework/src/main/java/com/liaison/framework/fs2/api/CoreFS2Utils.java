package com.liaison.framework.fs2.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.ObjectWriter;

/**
 * Various utility methods to support FS2
 * 
 * @author robert.christian
 */
public final class CoreFS2Utils {

  // used for JSON serialization
  protected static ObjectMapper JACKSON = new ObjectMapper();

  private static int DEFAULT_STREAM_COPY_BUFFER_SIZE = 8192;

  // Normalize URI by appending a '/'
  public static URI appendPathElement(URI uri, String strPathElement) throws Exception, URISyntaxException {
    String strURI = uri.toString();
    if (strURI.charAt(strURI.length() - 1) != '/') {
      strURI = strURI + "/";
    }
    strURI = strURI + strPathElement;
    return new URI(strURI);
  }

  public static URI appendLeaf(final URI parentURI, final String childName) {
    String newPath = ensureNoTrailingSlash(parentURI).getPath().concat(childName).concat("/");
    try {
      return new URI(parentURI.getScheme(), parentURI.getHost(), newPath, parentURI.getFragment());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] bytesFromFile(File f) {
    try {
      InputStream is = new FileInputStream(f);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      streamToStream(is, baos);
      return baos.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void bytesToFile(byte[] bytes, File f) {
    try {
      // "overwrite" if already exists
      OutputStream os = new FileOutputStream(f, false);
      streamToStream(new ByteArrayInputStream(bytes), os);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static FS2MetaSnapshot bytesToFS2Object(FlexibleStorageSystem FS2, URI uri, byte[] bytes) {
    try {
      FS2MetaSnapshot sm = FS2.createObjectEntry(uri);
      InputStream is = new ByteArrayInputStream(bytes);
      OutputStream os = FS2.getFS2PayloadOutputStream(sm.getURI());
      streamToStream(is, os);
      return sm;
    } catch (Exception ioe) {
      throw new RuntimeException(ioe);
    }
  }

  // creates a URI from a String[] representing a path from root to leaf.
  public static URI createObjectURI(String... pathAsArray) {
    return createURI(FlexibleStorageSystem.FS2_URI_SCHEME, pathAsArray);
  }

  // creates a URI, propagating URISyntaxException unchecked
  public static URI createURI(final String scheme, final String userInfo, final String host, final String path, final String query, final String fragment) {
    if (path == null) { throw new NullPointerException("path cannot be null"); }
    Integer port = 0;
    try {
      return new URI(scheme, userInfo, host, port, path, query, fragment);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

  }

  // creates a URI from a String[] representing a path from root to leaf.
  protected static URI createURI(final String scheme, final String[] pathAsArray) {
    if (pathAsArray == null) { throw new NullPointerException("path = null"); }

    // always start with a slash
    StringBuilder path = new StringBuilder("/");

    int i = 0;
    for (i = 0; i < pathAsArray.length; i++) {

      String pathElement = pathAsArray[i];
      if (0 == i && pathElement.length() == 0) {
        // handle when leading slash was included in the split, resulting
        // in an empty string as first element (ignore)
        continue;
      }

      if (1 == pathAsArray.length && pathElement.equals("/")) {
        // we're on first and only element,
        // and we're saying "root", so do nothing
        continue;
      }

      validateResource(pathElement); // throws
      path.append(pathElement + "/");
    }
    try {
      URI u = new URI(scheme, null, path.toString(), null);
      return ensureNoTrailingSlash(u);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * Determines the new location for a copied node.
   * 
   * <pre>
   * Suppose we have the following existing structure: a/b/c/d/e, a/b/c/d/h/i
   * And we want to copy a/b/c to foo/bar.
   *   
   * There are <b>two</b> possible behaviors here, depending on whether foo/bar already exists (this is implemented like cp in Unix, 
   * among other operating systems) 
   * 
   * Case 1 - foo/bar does *not* aleady exist:
   * 
   * Then a/b/c becomes foo/bar and...
   *  a/b/c/d becomes foo/bar/d
   *  a/b/c/d/e becomes foo/bar/d/e
   *  a/b/c/d/h becomes foo/bar/d/h
   *  a/b/c/d/h/i becomes foo/bar/d/h/i
   *  
   * Case 2 - foo/bar aleady exists:
   * 
   * Then a/b/c becomes foo/bar/c and...
   *  a/b/c/d becomes foo/bar/c/cd
   *  a/b/c/d/e becomes foo/bar/c/d/e
   *  a/b/c/d/h becomes foo/bar/c/d/h
   *  a/b/c/d/h/i becomes foo/bar/c/d/h/i
   * </pre>
   * 
   * TODO Unit test this
   * 
   */
  public static URI createCopiedURI(boolean toRootPreExists, URI fromRoot, URI toRoot, URI fromURI) {
    String fromRootPath = fromRoot.getPath(); // ie a/b/c
    String toRootPath = toRoot.getPath(); // ie foo/bar
    String descendantPath = fromURI.getPath(); // ie a/b/c/d/e

    String removedRootFromDescendant = descendantPath.replaceFirst(fromRootPath, ""); // (ie)

    String newPath = null;
    if (!toRootPreExists) {

      // handle root case
      if (fromRoot.equals(fromURI)) {
        newPath = toRootPath;
      }

      // (ie) we want foo/bar/d/e
      newPath = toRootPath + removedRootFromDescendant;
    } else {
      try {
        String fromLeaf = parseLeaf(fromRoot); // (ie) c -- we want to include
        newPath = toRootPath + fromLeaf + removedRootFromDescendant;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    String[] pathAsArray = newPath.substring(1).split("/");
    URI newURI = createURI(FlexibleStorageSystem.FS2_URI_SCHEME, pathAsArray);
    return newURI;

  }

  public static void main(String args) {

  }

  // make sure uri has no trailing slash
  public static URI ensureNoTrailingSlash(URI rawURI) {

    if (rawURI == null) { throw new NullPointerException("URI path is null."); }

    if (rawURI.getPath().trim().length() == 0) { throw new RuntimeException("URI path cannot be empty."); }

    String path = rawURI.getPath();
    int index = path.length() - 1;

    try {
      // if the path itself is not root, remove the trailing slash
      if (!path.equals("/") && path.charAt(index) == '/') {
        // strip slash
        String newPath = rawURI.getPath().substring(0, index);
        return new URI(rawURI.getScheme(), rawURI.getHost(), newPath, rawURI.getFragment());
      } else {
        return new URI(rawURI.getScheme(), rawURI.getHost(), rawURI.getPath(), rawURI.getFragment());
      }

    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T fromJSON(String json, Class<T> c) {
    try {
      ObjectReader or = JACKSON.reader(c);
      T newObj = or.readValue(json);
      return newObj;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] fs2ObjectToBytes(FlexibleStorageSystem FS2, FS2MetaSnapshot sm) {
    try {
      InputStream is = FS2.getFS2PayloadInputStream(sm.getURI());
      ByteArrayOutputStream os = new ByteArrayOutputStream();

      streamToStream(is, os);
      return os.toByteArray();

    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    } catch (FS2Exception mse) {
      throw new RuntimeException(mse);
    }
  }

  // creates a URI from a String representing a path from root to leaf.
  public static URI genURIFromPath(String path) {
    // be friendly -- prepend a slash if not already prepended
    if (!path.startsWith("/")) {
      path = "/" + path;
    }

    URI u = createURI(FlexibleStorageSystem.FS2_URI_SCHEME, null, null, path, null, null);
    try {
      return ensureNoTrailingSlash(u);
    } catch (Exception e) {
      // TODO check for all of these... reduce throws clauses
      throw new RuntimeException(e);
    }

  }

  // return path from root to leaf as array
  public static String[] getPathAsArray(URI uri) {
    StringBuilder path = new StringBuilder(uri.getPath());

    // we treat URI paths at lists of trees, where the first "node"
    // is a tree root... for this reason, a uri path of "/" is not
    // a root, but an empty path

    // guard against empty path (root)
    if (uri.getPath().equals("/") || uri.getPath().equals("\\")) {
      // do not want to return null, since that indicates
      // uri is a "root"... need to distinguish
      String back = "\"" + "\\" + "\"";
      String forward = "\"" + "/" + "\"";
      String ie = "(ie: " + back + " or " + forward + ")";
      throw new RuntimeException("URI path cannot be a root " + ie);
    }

    // strip leading slash (path will always begin with "/")
    path.deleteCharAt(0);

    stripTrailingSlash(path);

    // obtain path from root to leaf
    String s[] = path.toString().split("/");

    return s;
  }

  // does uri have a parent?
  public static boolean isRoot(URI uri) {
    try {
      return parseParentURI(uri) == null ? true : false;
    } catch (Exception e) {
      throw new RuntimeException();
    }
  }

  public static Set<URI> parseChildNodes(Set<URI> allNodes, final URI uri) {

    Set<URI> children = new HashSet<URI>();
    int len = CoreFS2Utils.getPathAsArray(uri).length;
    if (len == 1) { return children; }

    for (URI n : allNodes) {

      // is on same path and directly underneath?
      if (n.toString().startsWith(uri.toString()) && CoreFS2Utils.getPathAsArray(n).length == len + 1) {
          children.add(n);
      }

    }

    return children;
  }

  // TODO optimize
  public static Set<URI> parseDescendantNodes(Set<URI> allNodes, final URI uri) {

    Set<URI> descendants = new HashSet<URI>();
    int len = CoreFS2Utils.getPathAsArray(uri).length;
    if (len == 1) { return descendants; }

    for (URI n : allNodes) {

      // is on same path?
      if (n.toString().startsWith(uri.toString())) {
        descendants.add(n);
      }
    }

    return descendants;
  }

  public static String parseLeaf(final URI uri) throws Exception {
    String[] path = getPathAsArray(uri);
    return path[path.length - 1];
  }

  // create a new uri representing the parent of the specified uri
  public static URI parseParentURI(final URI uri) {

    String path[] = getPathAsArray(uri);

    // handle root case
    if (path.length == 1) { return null; }

    // obtain leaf (end of flattened path)
    String leaf = path[path.length - 1];

    int index = new StringBuilder(uri.getPath()).lastIndexOf(leaf);

    // remove leaf
    StringBuilder newPath = new StringBuilder(uri.getPath().substring(0, index));

    URI newURI = createURI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), newPath.toString(), uri.getQuery(), uri.getFragment());

    newURI = ensureNoTrailingSlash(newURI);

    return newURI;

  }

  // return the root uri
  public static URI parseRootURI(final URI uri) {
    if (isRoot(uri)) { return uri; }

    return parseRootURI(parseParentURI(uri));
  }

  public static byte[] streamToBytes(InputStream is) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    streamToStream(is, baos, DEFAULT_STREAM_COPY_BUFFER_SIZE);
    return baos.toByteArray();
  }

  public static void streamToFile(InputStream is, File f) {
    try {
      OutputStream os = new FileOutputStream(f);
      streamToStream(is, os);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // for convenience, uses default buffer size
  public static int streamToStream(InputStream is, OutputStream os) throws IOException {
    return streamToStream(is, os, DEFAULT_STREAM_COPY_BUFFER_SIZE);
  }

  /**
   * Reads an input stream using the provided buffer size and writes content to
   * an OutputStream, and closes the OutputStream upon method exit
   * 
   * @return size int number of bytes read
   * @throws IOException
   *           when either streams are null, when there are exception accessing
   *           the streams or when the buffer size is less than 1
   * 
   */
  public static int streamToStream(InputStream is, OutputStream os, int bufferSize) throws IOException {
    int size = 0;
    int iLen = 0;

    if (is == null) { throw new IOException("Input Stream is null"); }
    if (os == null) { throw new IOException("Output Stream is null"); }

    if (bufferSize < 1) { throw new IOException("Invalid buffer size " + bufferSize + ".  Buffer size must be at least 1 or greater"); }

    try {

      byte[] baBuf = new byte[bufferSize];

      while ((iLen = is.read(baBuf)) >= 0) {
        os.write(baBuf, 0, iLen);
        size += iLen;
      }
    } finally {
      try {
        os.flush();
        os.close();
        is.close();
      } catch (Exception e) {
        // TODO log
        e.printStackTrace();
      }
    }

    return size;
  }

  /**********************************************************************************
   * 
   * Stream functions...
   * 
   *********************************************************************************/

  public static String streamToString(InputStream is) throws IOException {
    return new String(streamToBytes(is));
  }

  /**
   * Strips trailing slash from the end of a StringBuilder object, if one
   * exists, and return whether the slash was removed.
   * 
   * @return boolean - whether a trailing slash was removed
   */
  private static boolean stripTrailingSlash(StringBuilder s) {
    final boolean hasTrailingSlash = s.charAt(s.length() - 1) == '/';

    // strip trailing slash
    s = hasTrailingSlash ? s.deleteCharAt(s.length() - 1) : s;

    return hasTrailingSlash;
  }

  public static String toJSON(Object o) {
    try {
      @SuppressWarnings("deprecation")
      ObjectWriter ow = JACKSON.defaultPrettyPrintingWriter();
      String s = ow.writeValueAsString(o);
      return s;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // a resource identifier cannot contain a slash, since the slash is used to
  // delimit resource identifiers (in other words a resource named "foobar" is
  // an atomic resource, adding a slash, "foo/bar) becomes two atomic resources
  private static void validateResource(String resourceId) {
    if (resourceId.indexOf("/") != -1 || resourceId.indexOf("\\") != -1) { throw new RuntimeException("Resource id cannot contain a slash."); }
  }

  // validates that URI is a valid FS2 uri
  public static void validateURI(final URI uri) throws FS2Exception {

    String SCHEME = FlexibleStorageSystem.FS2_URI_SCHEME;

    if (uri == null) { throw new NullPointerException("URI cannot be null."); }

    // make sure scheme is correct
    String strScheme = uri.getScheme();
    if (strScheme == null || !strScheme.equalsIgnoreCase(SCHEME)) { throw new FS2Exception("Expecting " + SCHEME + " in URI. " + "[uri=" + uri.toString() + "]"); }

    if (uri.getPath().split("/").length < 1) { throw new FS2Exception("Path must have at least a root node."); }
  }

  // protect this class from being instantiated
  private CoreFS2Utils() {
  }

}
