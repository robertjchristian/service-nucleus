package com.liaison.framework.fs2.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Flexible Storage Service (FS2) is a stream-centric storage repository for
 * Internet objects and arbitrarily large object payloads.
 * 
 * Objects are stored and retrieved via URI.
 * 
 * @author robert.christian
 */
public class FlexibleStorageSystemImpl implements FlexibleStorageSystem {

  protected final FS2StorageProvider sp;
  protected final FS2Configuration config;

  /**
   * Protected constructor... use FS2Factory to obtain instances
   */
  protected FlexibleStorageSystemImpl(FS2Configuration config) {

    // determine storage provider from config
    sp = determineStorageProvider(config);

    this.config = config;
  }

  @Override
  public FS2MetaSnapshot addHeader(URI uri, String key, String value) {
    try {
      validateURI(uri);
      // TODO we're currently updating and refetching in most cases...
      // this is not ideal. we should change the interface to return
      // the updated object and let the implementation determine whether
      // how to construct the new snapshot (ie prefer shallow copy and
      // update to re-fetching)
      sp.addHeader(uri, key, value);
      return fetchObject(uri);
    } catch (Throwable t) {
      // TODO better exception here
      throw new RuntimeException(t);
    }
  }

  @Override
  public FS2MetaSnapshot createObjectEntry(String path) throws FS2Exception {

    URI u = CoreFS2Utils.genURIFromPath(path);
    return createObjectEntry(u);
  }

  @Override
  public FS2MetaSnapshot createObjectEntry(URI uri) throws FS2Exception {

    // create ancestors as necessary
    if (!CoreFS2Utils.isRoot(uri)) {
      URI parent = CoreFS2Utils.parseParentURI(uri);
      if (!exists(parent)) {
        createObjectEntry(parent);
      }
    }

    return sp.createObjectEntry(uri);
  }

  @Override
  public FS2MetaSnapshot createObjectEntry(URI uri, String jsonMeta, InputStream payload) throws FS2Exception {
    return sp.createObjectEntry(uri, jsonMeta, payload);
  }

  @Override
  public void delete(URI uri) throws FS2Exception {
    try {
      URI safeURI = CoreFS2Utils.ensureNoTrailingSlash(uri);
      sp.delete(safeURI);
    } catch (Exception e) {
      throw new FS2Exception(e);
    }
  }

  @Override
  public void deletePayload(URI uri) {
    sp.deletePayload(uri);
  }


  @Override
  public void deleteRecursive(URI uri) throws FS2Exception {
    try {
      URI safeURI = CoreFS2Utils.ensureNoTrailingSlash(uri);
      sp.deleteRecursive(safeURI);
    } catch (Exception e) {
      throw new FS2Exception(e);
    }
  }

  // helpers:
  protected FS2StorageProvider determineStorageProvider(FS2Configuration config) {
    if (null == config.getStorageProvider()) { throw new RuntimeException("StorageProvider cannot be null."); }

    if (null == config.getStorageProviderMonikers()) { throw new RuntimeException("Missing required configuration StorageProviderMonikers"); }

    String fqn = config.getStorageProviderMonikers().get(config.getStorageProvider());
    if (null == fqn) { throw new RuntimeException("Cannot determine StorageProvider fqn from alias " + config.getStorageProvider()); }

    try {
      Object o = Class.forName(fqn).newInstance();
      FS2StorageProvider fs2sp = (FS2StorageProvider) o;
      fs2sp.init(config);
      return fs2sp;
    } catch (Throwable t) {
      throw new RuntimeException("Failed to create new instance of " + fqn, t);
    }
  }

  @Override
  public boolean exists(URI... uri) throws FS2Exception {
    try {
      for (URI u : uri) {
        URI safeURI = CoreFS2Utils.ensureNoTrailingSlash(u);
        if (sp.exists(safeURI)) { return true; }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return false;
  }

  // lookup a object in this object list
  @Override
  public FS2MetaSnapshot fetchObject(URI uri) throws FS2Exception {
    try {
      validateURI(uri);

      // ensure uri is consistent
      URI safeURI = CoreFS2Utils.ensureNoTrailingSlash(uri);

      return sp.fetchObjectMeta(safeURI);
    } catch (Throwable t) {
      if (t instanceof FS2Exception) {
        throw (FS2Exception) t;
      } else {
        throw new FS2Exception(t);
      }
    }
  }

  protected Date generateExpirationDate(int timeToLive) {
    if (timeToLive < -1) { throw new IllegalArgumentException("Time to live must be a positive integer or -1 (for infinite)."); }

    Calendar calendar = java.util.Calendar.getInstance();

    // -1 implies forever, so we'll add 20 years.
    if (timeToLive == -1) {
      calendar.add(Calendar.YEAR, 20);
    } else {
      // consider timeToLive is in minutes
      calendar.add(Calendar.MINUTE, timeToLive);
    }
    Date expirationDate = calendar.getTime();
    return expirationDate;
  }

  @Override
  public InputStream getFS2PayloadInputStream(URI uri) throws FS2PayloadNotFoundException {
    try {
      // TODO make uniform parameter and method names
      return sp.fetchFS2PayloadInputStream(uri);
    } catch (Exception e) {
      throw new FS2PayloadNotFoundException(uri, e);
    }
  }
 
  @Override
  public OutputStream getFS2PayloadOutputStream(URI uri) throws FS2Exception {
    return sp.fetchFS2PayloadOutputStream(uri);
  }

  @Override
  public String[] getHeader(URI uri, String key) throws FS2Exception {
    return fetchObject(uri).getHeader(key);
  }

  @Override
  public Set<String> getHeaderNames(URI uri) throws FS2Exception {
    return fetchObject(uri).getHeaderNames();
  }

  @Override
  public FS2ObjectHeaders getHeaders(URI uri) throws FS2Exception {
    return fetchObject(uri).getHeaders();
  }

  @Override
  public long getPayloadSizeInBytes(URI objectURI) throws FS2Exception {
    try {
      return sp.getPayloadSizeInBytes(objectURI);
    } catch (Throwable t) {
      if (t instanceof FS2Exception) {
        throw (FS2Exception) t;
      } else {
        throw new FS2Exception(t);
      }
    }

  }

  @Override
  public Set<FS2MetaSnapshot> listChildren(URI parentURI) throws FS2Exception {

    Set<FS2MetaSnapshot> c = new HashSet<FS2MetaSnapshot>();
    for (URI u : listChildrenURIs(parentURI)) {
      c.add(fetchObject(u));
    }

    return c;
  }

  @Override
  public Set<URI> listChildrenURIs(URI uri) throws FS2Exception {
    return sp.listChilden(uri);
  }

  @Override
  public Set<URI> listChildrenURIs(URI uri, String filter) throws FS2Exception {
    // TODO this should be pushed down to the concrete implementation...
    // where it can be executed on the native persistence layer for better
    // performance

    Pattern p = Pattern.compile(filter);

    // get all children
    Set<URI> set = listChildrenURIs(uri);
    Set<URI> match = new HashSet<URI>();
    for (URI u : set) {
      Matcher m = p.matcher(u.getPath());
      if (m.matches()) {
        match.add(u);
      }
    }

    return match;
  }

  @Override
  public Set<FS2MetaSnapshot> listDescendants(URI uri) throws FS2Exception {
    Set<FS2MetaSnapshot> c = new HashSet<FS2MetaSnapshot>();
    for (URI u : listDescendantURIs(uri)) {
      c.add(fetchObject(u));
    }

    return c;
  }

  @Override
  public Set<URI> listDescendantURIs(URI uri) throws FS2Exception {
    return sp.listDescendants(uri);
  }

  @Override
  public Set<URI> listDescendantURIs(URI uri, String filter) throws FS2Exception {
    // TODO this should be pushed down to the concrete implementation...
    // where it can be executed on the native persistence layer for better
    // performance

    Pattern p = null;
    if (null != filter) {
      p = Pattern.compile(filter);
    }

    // get all descendants
    Set<URI> set = listDescendantURIs(uri);
    Set<URI> match = new HashSet<URI>();
    for (URI u : set) {

      if (null == p) {
        match.add(u);
        continue;
      }

      Matcher m = p.matcher(u.getPath());
      if (m.matches()) {
        match.add(u);
      }
    }
    return match;
  }

  @Override
  public void move(URI fromURI, URI toURI) throws FS2Exception {
    sp.move(fromURI, toURI);
  }

  @Override
  public byte[] readPayloadToBytes(URI uri) {
    try {
      InputStream is = getFS2PayloadInputStream(uri);
      byte[] b = CoreFS2Utils.streamToBytes(is);
      return b;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Additional validation on URI for FS2.
   * 
   * @param uri
   * @throws FS2Exception
   */

  private void validateURI(URI uri) throws FS2Exception {

    if (uri == null) { throw new NullPointerException("URI cannot be null."); }

    // make sure scheme is "fs2"
    String strScheme = uri.getScheme();
    if (strScheme == null || !strScheme.equalsIgnoreCase(FS2_URI_SCHEME)) { throw new FS2Exception("Expecting " + FS2_URI_SCHEME + " in URI. " + "[uri="
            + uri.toString() + "]"); }

    // TODO add ensure no backslash here and return the uri

  }

  @Override
  public void writePayloadFromBytes(URI uri, byte[] bytes) {
    writePayloadFromStream(uri, new ByteArrayInputStream(bytes));
  }

  @Override
  public void writePayloadFromStream(URI uri, InputStream is) {
    try {
      OutputStream os = getFS2PayloadOutputStream(uri);
      CoreFS2Utils.streamToStream(is, os);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public FS2MetaSnapshot[] createObjectEntries(String... path) throws FS2Exception {
    FS2MetaSnapshot[] rVal = new FS2MetaSnapshot[path.length];
    for (int i = 0; i < path.length; i++) {
      String p = path[i];
      FS2MetaSnapshot m = createObjectEntry(p);
      rVal[i] = m;
    }
    return rVal;
  }

  @Override
  public boolean exists(String... uriPaths) throws FS2Exception {
    // TODO could be done better

    for (int i = 0; i < uriPaths.length; i++) {

      String uriPath = uriPaths[i];

      String[] path = uriPath.split("/");
      try {
        URI u = CoreFS2Utils.createObjectURI(path);
        if (!exists(u)) { return false; }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return true;
  }

  @Override
  public FS2MetaSnapshot fetchObject(String path) throws FS2Exception {
    // TODO fixing a broken test so not sure if it makes sense to
    // parse the path here... but it seems to make sense at the moment
    URI u = CoreFS2Utils.createObjectURI(path.split("/"));
    return fetchObject(u);
  }

  @Override
  public Set<FS2MetaSnapshot> listDescendants(URI uri, String filter) throws FS2Exception {
    // TODO push this down... code reuse but bad algorithm
    Set<URI> uris = listDescendantURIs(uri, filter);
    Set<FS2MetaSnapshot> metas = new HashSet<FS2MetaSnapshot>();
    for (URI u : uris) {
      metas.add(fetchObject(u));
    }

    return metas;
  }

  @Override
  public void copy(URI fromURI, URI toURI) {
    sp.copy(fromURI, toURI);
  }

  @Override
  public void copy(String fromPath, String toPath) {
    copy(CoreFS2Utils.createObjectURI(fromPath.split("/")), CoreFS2Utils.createObjectURI(toPath.split("/")));
  }

  @Override
  public void updateHeaders(URI u, FS2ObjectHeaders h) throws FS2Exception {
    sp.setHeaders(u, h);
  }

  @Override
  public FS2MetaSnapshot createObjectEntry() throws FS2Exception {
    // TODO ensure unique, offer facility for tmp?  
    // TODO ie with cleanup after thread dies?  or client manages?
    return createObjectEntry("/" + UUID.randomUUID().toString());
  }

}
