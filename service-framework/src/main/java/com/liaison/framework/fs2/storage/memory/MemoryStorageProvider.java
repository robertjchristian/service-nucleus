package com.liaison.framework.fs2.storage.memory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.liaison.framework.fs2.api.CoreFS2Utils;
import com.liaison.framework.fs2.api.FS2Configuration;
import com.liaison.framework.fs2.api.FS2MetaSnapshot;
import com.liaison.framework.fs2.api.FS2MetaSnapshotImpl;
import com.liaison.framework.fs2.api.FS2ObjectAlreadyExistsException;
import com.liaison.framework.fs2.api.FS2ObjectHeaders;
import com.liaison.framework.fs2.api.FS2PayloadNotFoundException;
import com.liaison.framework.fs2.api.FS2StorageProvider;

// TODO lock on container when moving

// bind metadata and object in memory
final class FS2InMemoryObjectContainer {
  MemoryMetadataImpl metadata = null;
  byte[] payload = null;

  @Override
  public FS2InMemoryObjectContainer clone() {
    FS2InMemoryObjectContainer clone = new FS2InMemoryObjectContainer();
    clone.metadata = metadata.clone();
    if (null != payload) {
      clone.payload = payload.clone();
    }
    return clone;
  }

  @Override
  public String toString() {
    return metadata.toString();
  }

}

// local in memory implementation allows us to update the snapshot
class MemoryMetadataImpl extends FS2MetaSnapshotImpl {

  // copy constructor
  public MemoryMetadataImpl(FS2MetaSnapshot ms) {
    super(ms.getURI(), ms.createdOn(), "MemoryStorageProvider", ms.getHeaders());
  }

  public MemoryMetadataImpl(URI uri, Date creationDate, FS2ObjectHeaders headers, Long payloadSize) {
    super(uri, creationDate, "MemoryStorageProvider", headers);
  }

  protected void addHeader(String key, String value) {
    headers.addHeader(key, value);
    updateSnapshot();
  }

  protected void updateSnapshot() {
    cachedJSON = toJSON(true);
    snapshotCreatedOn = new Date();
  }

  @Override
  public MemoryMetadataImpl clone() {
    MemoryMetadataImpl m = new MemoryMetadataImpl(this);
    return m;
  }
}

final class MemoryPayloadOutputStream extends OutputStream {

  private OutputStream os = new ByteArrayOutputStream();
  private FS2InMemoryObjectContainer fs2ObjCont = null;

  public MemoryPayloadOutputStream(FS2InMemoryObjectContainer fs2ObjCont) {
    this.fs2ObjCont = fs2ObjCont;
  }

  @Override
  public void close() throws IOException {
    if (os != null) {
      os.close();
      byte[] ba = ((ByteArrayOutputStream) os).toByteArray();
      try {
        fs2ObjCont.payload = ba;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void write(byte[] buffer, int offset, int length) throws IOException {
    if (offset < 0 || length < 0 || offset + length > buffer.length) { throw new ArrayIndexOutOfBoundsException(); }

    if (os != null) {
      os.write(buffer, offset, length);
    }
  }

  @Override
  public void write(int i) throws IOException {
    if (os != null) {
      os.write(i);
    }
  }
}

/**
 * This in-memory implementation may be useful as a default for development and
 * testing, a feature rich cache with streamed objects, metadata, and tree
 * support, and as an example implementation.
 * 
 * @author robert.christian
 */
public final class MemoryStorageProvider implements FS2StorageProvider {

  private Map<URI, FS2InMemoryObjectContainer> objectResolver = new HashMap<URI, FS2InMemoryObjectContainer>();

  FS2Configuration config;

  @Override
  public void addHeader(URI uri, String key, String value) {
    objectResolver.get(uri).metadata.addHeader(key, value);
  }

  @Override
  public FS2MetaSnapshot createObjectEntry(URI uri) throws FS2ObjectAlreadyExistsException {

    // *************
    // create entry
    // *************
    Date creationDate = new Date();
    MemoryMetadataImpl i = new MemoryMetadataImpl(uri, creationDate, new FS2ObjectHeaders(), 0L);

    // register the uri with the new metadata
    FS2InMemoryObjectContainer c = new FS2InMemoryObjectContainer();
    c.metadata = i;
    objectResolver.put(uri, c);

    return i;
  }

  @Override
  public FS2MetaSnapshot createObjectEntry(URI uri, String jsonMeta, byte[] payload) {
    return createObjectEntry(uri, jsonMeta, new ByteArrayInputStream(payload));
  }

  @Override
  public FS2MetaSnapshot createObjectEntry(URI uri, String jsonMeta, InputStream payload) {
    try {

      FS2MetaSnapshot m = CoreFS2Utils.fromJSON(jsonMeta, FS2MetaSnapshotImpl.class);
      MemoryMetadataImpl mem = new MemoryMetadataImpl(m);

      FS2InMemoryObjectContainer c = new FS2InMemoryObjectContainer();
      c.metadata = mem;
      if (null != payload) {
        c.payload = CoreFS2Utils.streamToBytes(payload);
      }
      objectResolver.put(uri, c);
      return c.metadata;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void delete(URI uri) {

    if (hasDescendants(uri)) { throw new RuntimeException("Cannot delete " + uri + " because it has descendants.  Consider using deleteRecursive()."); }

    objectResolver.remove(uri);

  }

  @Override
  public void deletePayload(URI uri) {
    objectResolver.get(uri).payload = null;
  }

  @Override
  public void deleteRecursive(URI uri) {

    // delete is a little funny because we want to delete this and all
    // descendants...
    // otherwise we would have to either throw error if descendants or remove
    // and
    // re-attach children to parent

    // remove descendants
    Set<URI> descendants = new HashSet<URI>();
    for (URI candidate : objectResolver.keySet()) {
      if (candidate.toString().startsWith(uri.toString())) {
        descendants.add(candidate);
      }
    }

    objectResolver.keySet().removeAll(listDescendants(uri));

    // remove parent
    objectResolver.remove(uri);

  }

  @Override
  public boolean exists(URI uri) {
    return objectResolver.containsKey(uri);
  }

  @Override
  public InputStream fetchFS2PayloadInputStream(URI uri) {
    byte[] payload = objectResolver.get(uri).payload;
    return new ByteArrayInputStream(payload);
  }

  @Override
  public OutputStream fetchFS2PayloadOutputStream(URI uri) {
    if (objectResolver.get(uri).payload != null) {
      // payloads are immutable by design...
      throw new RuntimeException("Object has already been written.");
    }

    return new MemoryPayloadOutputStream(objectResolver.get(uri));
  }

  @Override
  public FS2MetaSnapshot fetchObjectMeta(URI uri) throws FS2PayloadNotFoundException {
    try {
      FS2MetaSnapshot m = objectResolver.get(uri).metadata;
      return m;
    } catch (Throwable t) {
      throw new FS2PayloadNotFoundException(uri, t);
    }
  }

  @Override
  public Long getPayloadSizeInBytes(URI uri) {
    return new Long(objectResolver.get(uri).payload.length);
  }

  @Override
  public boolean hasDescendants(URI uri) {
    for (URI candidate : objectResolver.keySet()) {
      if (!candidate.equals(uri) && candidate.toString().startsWith(uri.toString())) { return true; }
    }
    return false;
  }

  @Override
  public void init(FS2Configuration config) {
    // set configuration
    this.config = config;

    // this is a memory implementation, so should be clear upon init()
    objectResolver.clear();

  }

  @Override
  public Set<URI> listChilden(URI uri) {

    // the length of this uri
    int ulen = CoreFS2Utils.getPathAsArray(uri).length;

    // remove descendants
    Set<URI> children = new HashSet<URI>();

    for (URI candidate : objectResolver.keySet()) {
      if (!candidate.equals(uri) && candidate.toString().startsWith(uri.toString())) {
        // also needs to be direct child
        int clen = CoreFS2Utils.getPathAsArray(candidate).length;
        if (clen == ulen + 1) {
          children.add(candidate);
        }
      }
    }
    return children;
  }

  @Override
  public Set<URI> listDescendants(URI uri) {

    Set<URI> descendants = new HashSet<URI>();

    for (URI candidate : objectResolver.keySet()) {
      if (!candidate.equals(uri) && candidate.toString().startsWith(uri.toString())) {
        descendants.add(candidate);
      }
    }

    return descendants;
  }

  @Override
  public URI listParent(URI uri) {
    URI parent = CoreFS2Utils.parseParentURI(uri);

    // rather than simply parse, check for existence
    try {
      return objectResolver.get(parent).metadata.getURI();
    } catch (Exception e) {
      throw new RuntimeException("Unable to find parent.", e);
    }
  }

  @Override
  public void copy(URI fromURI, URI toURI) {
    // TODO - Determine how copy should work. Currently copy and move are
    // different. Copy works like an OS where "if destination exists, put
    // it underneath. else name if for destination. IE cp a/b n would create
    // just n... or if n already exists, then it would create n/b.
    // I don't think FS2 needs this level of complexity. Move is implemented
    // more simply, fast fail if destination exists.

    // DEEP copy
    boolean toRootPreExists = exists(toURI);

    URI newRootURI = CoreFS2Utils.createCopiedURI(toRootPreExists, fromURI, toURI, fromURI);

    // copy root
    copySingleNode(fromURI, newRootURI);

    // copy descendants (tree-walking order does not matter for memory
    // implementation)
    Set<URI> descendants = listDescendants(fromURI);
    for (URI descendant : descendants) {
      // there is some complexity determining the new location, based around
      // whether the target pre-exists
      URI descendantToURI = CoreFS2Utils.createCopiedURI(toRootPreExists, fromURI, toURI, descendant);
      copySingleNode(descendant, descendantToURI);
    }
  }

  public void copySingleNode(URI fromURI, URI toURI) {
    FS2InMemoryObjectContainer fmoc = objectResolver.get(fromURI);
    FS2InMemoryObjectContainer clone = fmoc.clone();
    clone.metadata.setUri(toURI); // update uri
    objectResolver.put(toURI, clone);
  }

  @Override
  public void setHeaders(URI uri, FS2ObjectHeaders headers) {
    objectResolver.get(uri).metadata.setHeaders(headers);
  }

  @Override
  public void move(URI fromURI, URI toURI) throws FS2ObjectAlreadyExistsException {
    if (exists(toURI)) { throw new FS2ObjectAlreadyExistsException(toURI); }
    copy(fromURI, toURI);
    deleteRecursive(fromURI);
  }

}
