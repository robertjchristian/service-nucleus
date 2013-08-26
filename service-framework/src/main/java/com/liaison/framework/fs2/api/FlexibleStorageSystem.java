package com.liaison.framework.fs2.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Set;

/**
 * The FlexibleStorageSystem (FS2) is a stream-centric storage repository for
 * arbitrarily large data objects.
 * <P>
 * Objects are stored and retrieved via URI.
 * 
 * @author robert.christian
 */
public interface FlexibleStorageSystem {

  // default persistence store (ie file, in-memory, or database )
  // String FS2_DEFAULT_REPOSITORY_VALUE = Repositories.DEFAULT.name();
  String FS2_DEFAULT_REPOSITORY_VALUE = null;

  String FS2_URI_SCHEME = "fs2";

  FS2MetaSnapshot addHeader(URI uri, String key, String value) throws FS2Exception;

  // convenience, expects complete path
  FS2MetaSnapshot createObjectEntry(String path) throws FS2Exception;

  // convenience, allows for creation when don't care about naming
  // ie for temp storage
  FS2MetaSnapshot createObjectEntry() throws FS2Exception;
  
  // convenience, create many entries at once. return array of entries ordered
  // same as parameter list.
  // inherently creates missing nodes along the path
  // warning: "/foo, /foo/bar" works, but "/foo/bar, /foo" will throw exception
  // since the first parameter
  // will implicitly create /foo, and the second explicit attempt to create /foo
  // will fail since it already exists
  FS2MetaSnapshot[] createObjectEntries(String... path) throws FS2Exception;

  FS2MetaSnapshot createObjectEntry(URI uri) throws FS2Exception;

  FS2MetaSnapshot createObjectEntry(URI uri, String jsonMeta, InputStream payload) throws FS2Exception;

  // delete node pointed to by uri
  void delete(URI uri) throws FS2Exception;

  // remove only the payload
  void deletePayload(URI uri);

  // call delete on all descendants, then on this uri
  void deleteRecursive(URI uri) throws FS2Exception;

  boolean exists(String... uriPaths) throws FS2Exception;

  boolean exists(URI... uri) throws FS2Exception;

  FS2MetaSnapshot fetchObject(URI uri) throws FS2Exception;

  FS2MetaSnapshot fetchObject(String path) throws FS2Exception;

  InputStream getFS2PayloadInputStream(URI uri) throws FS2PayloadNotFoundException;

  OutputStream getFS2PayloadOutputStream(URI uri) throws FS2Exception;

  // header methods

  /**
   * @return unbound <b>SNAPSHOT</b> of headers (to update, use updateHeaders
   *         function)
   */
  FS2ObjectHeaders getHeaders(URI uri) throws FS2Exception;

  /**
   * @return unbound <b>SNAPSHOT</b> of headers (to update, use updateHeaders
   *         function)
   */
  String[] getHeader(URI uri, String key) throws FS2Exception;

  /**
   * @return unbound <b>SNAPSHOT</b> of headers (to update, use updateHeaders
   *         function)
   */
  Set<String> getHeaderNames(URI uri) throws FS2Exception;

  /**
   * Use this to affect the underlying object
   * 
   * @param m
   * @param h
   * @throws FS2Exception
   */
  void updateHeaders(URI u, FS2ObjectHeaders h) throws FS2Exception;

  long getPayloadSizeInBytes(URI uri) throws FS2Exception;

  // list all children
  Set<FS2MetaSnapshot> listChildren(URI uri) throws FS2Exception;

  Set<URI> listChildrenURIs(URI uri) throws FS2Exception;

  Set<URI> listChildrenURIs(URI uri, String filter) throws FS2Exception;

  // list all descendants
  Set<FS2MetaSnapshot> listDescendants(URI uri) throws FS2Exception;

  Set<FS2MetaSnapshot> listDescendants(URI uri, String filter) throws FS2Exception;
  
  Set<URI> listDescendantURIs(URI uri) throws FS2Exception;

  Set<URI> listDescendantURIs(URI uri, String filter) throws FS2Exception;

  // move object from one uri to another
  void move(URI oldURI, URI newURI) throws FS2Exception;

  byte[] readPayloadToBytes(URI uri);

  void writePayloadFromBytes(URI uri, byte[] bytes);

  void writePayloadFromStream(URI uri, InputStream is);

  // always a deep copy
  public void copy(URI fromURI, URI toURI);

  // ... same, by path
  public void copy(String fromPath, String toPath);

}