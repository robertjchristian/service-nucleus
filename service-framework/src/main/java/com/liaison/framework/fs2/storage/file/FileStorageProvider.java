package com.liaison.framework.fs2.storage.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import com.liaison.framework.fs2.api.CoreFS2Utils;
import com.liaison.framework.fs2.api.FS2Configuration;
import com.liaison.framework.fs2.api.FS2MetaSnapshot;
import com.liaison.framework.fs2.api.FS2MetaSnapshotImpl;
import com.liaison.framework.fs2.api.FS2ObjectAlreadyExistsException;
import com.liaison.framework.fs2.api.FS2ObjectHeaders;
import com.liaison.framework.fs2.api.FS2ObjectNotFoundException;
import com.liaison.framework.fs2.api.FS2PayloadNotFoundException;
import com.liaison.framework.fs2.api.FS2StorageProvider;

/**
 * File implementation
 * 
 * Note this is a bare minimum implementation and there is work to be done in
 * terms of hardening for high throughput, concurrency, error handling, retry,
 * 
 * @author robert.christian
 */
public final class FileStorageProvider implements FS2StorageProvider {

  FS2Configuration config;
  File mountPoint;

  public FileStorageProvider() {
  }

  // TODO we can support move natively here.
  // should push move into storage provider for more efficient implementations

  @Override
  public void addHeader(URI uri, String name, String value) throws FS2ObjectNotFoundException, Exception {
    FS2MetaSnapshot m = fetchObjectMeta(uri);
    m.getHeaders().addHeader(name, value);

    // update meta
    persistMeta(uri, m.toJSON());
  }

  @Override
  public FS2MetaSnapshot createObjectEntry(URI uri) throws FS2ObjectAlreadyExistsException {
    FS2MetaSnapshotImpl meta = new FS2MetaSnapshotImpl(uri, new Date(), getClass().getName());
    return createObjectEntry(uri, meta.toJSON(), (InputStream) null);
  }

  @Override
  public FS2MetaSnapshot createObjectEntry(URI uri, String jsonMeta, byte[] payload) throws FS2ObjectAlreadyExistsException {
    return createObjectEntry(uri, jsonMeta, null == payload ? null : new ByteArrayInputStream(payload));
  }

  @Override
  public FS2MetaSnapshot createObjectEntry(URI uri, String jsonMeta, InputStream payload) throws FS2ObjectAlreadyExistsException {

    // create the node reference
    File node = new File(mountPoint, uri.getPath());

    if (node.exists()) { throw new FS2ObjectAlreadyExistsException(uri); }

    // make sure any interim directories exist, and recursively create
    if (!node.getParentFile().exists()) {
      createObjectEntry(CoreFS2Utils.parseParentURI(uri));
    }

    if (!node.mkdir()) { throw new RuntimeException("Failed creating node " + uri); }

    persistMeta(uri, jsonMeta);

    if (null != payload) {
      persistPayload(uri, payload);
    }

    return CoreFS2Utils.fromJSON(jsonMeta, FS2MetaSnapshotImpl.class);
  }

  protected void persistMeta(URI uri, String jsonMeta) {
    // write metadata to file
    File node = new File(mountPoint, uri.getPath());
    File metaFile = new File(node, ".meta");
    CoreFS2Utils.bytesToFile(jsonMeta.getBytes(), metaFile);
  }

  protected void persistPayload(URI uri, InputStream payload) {
    // write payload to file
    File node = new File(mountPoint, uri.getPath());
    File payloadFile = new File(node, ".payload");
    CoreFS2Utils.streamToFile(payload, payloadFile);
  }

  @Override
  public void delete(URI uri) {
    if (listChilden(uri).size() > 0) { throw new RuntimeException("Cannot delete " + uri
            + " because it is not empty.  It has a payload and/or children.  Consider using deleteReccursive."); }

    // we know we're a leaf so it's safe
    deleteRecursive(uri);
  }

  @Override
  public void deletePayload(URI uri) {
    File node = makeFileReference(uri);
    File payload = new File(node, ".payload");
    if (!payload.delete()) { throw new RuntimeException("Failed to delete payload at " + uri); }
  }

  @Override
  public void deleteRecursive(URI uri) {
    try {
      FileUtils.deleteDirectory(makeFileReference(uri));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean exists(URI uri) {
    return makeFileReference(uri).exists();
  }

  @Override
  public InputStream fetchFS2PayloadInputStream(URI uri) {
    File payload = new File(makeFileReference(uri), ".payload");
    return new FilePayloadInputStream(payload);
  }

  @Override
  public OutputStream fetchFS2PayloadOutputStream(URI uri) {
    File payload = new File(makeFileReference(uri), ".payload");
    return new FilePayloadOutputStream(payload);
  }

  @Override
  public FS2MetaSnapshot fetchObjectMeta(URI uri) throws FS2PayloadNotFoundException {

    // create the node reference
    File node = new File(mountPoint, uri.getPath());

    // meta reference
    File metaFile = new File(node, ".meta");

    if (!node.exists() || !metaFile.exists()) { throw new FS2PayloadNotFoundException(uri); }

    // hydrate meta from disk
    String jsonMeta = new String(CoreFS2Utils.bytesFromFile(metaFile));
    FS2MetaSnapshot m = CoreFS2Utils.fromJSON(jsonMeta, FS2MetaSnapshotImpl.class);

    return m;

  }

  @Override
  public Long getPayloadSizeInBytes(URI uri) {
    File f = makeFileReference(uri);
    return FileUtils.sizeOf(f);
  }

  @Override
  public boolean hasDescendants(URI uri) {
    // could be more efficient :)
    return listDescendants(uri).size() > 0;
  }

  @Override
  public void init(FS2Configuration config) {
    // create
    this.config = config;

    String mp = config.getStorageProviderProperties().getProperty("mount-point", "");
    mountPoint = new File(mp);

    // useful ie in test
    String deleteExistingData = config.getStorageProviderProperties().getProperty("delete-existing-data", "true");
    if (Boolean.parseBoolean(deleteExistingData)) {
      try {
        FileUtils.deleteDirectory(mountPoint);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

    }

    mountPoint.mkdirs();
    if (!mountPoint.isDirectory()) { throw new RuntimeException(mountPoint.getAbsolutePath() + " is not a directory."); }

  }

  @Override
  public Set<URI> listChilden(URI uri) {
    try {
      Set<URI> s = new HashSet<URI>();
      File node = makeFileReference(uri);

      // sanity check
      if (!node.isDirectory()) { throw new RuntimeException(uri + " does not exist."); }

      File[] folders = node.listFiles((FileFilter) FileFilterUtils.directoryFileFilter());
      for (File f : folders) {
        s.add(makeURIReference(f));
      }

      return s;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Set<URI> listDescendants(URI uri) {

    Set<URI> descendants = new HashSet<URI>();
    for (URI child : listChilden(uri)) {
      walk(child, descendants);
    }
    return descendants;
  }

  private void walk(URI node, Set<URI> descendants) {
    // add visited node
    descendants.add(node);

    // traverse
    for (URI child : listChilden(node)) {
      walk(child, descendants);
    }
  }

  @Override
  public URI listParent(URI uri) {
    File parent = makeFileReference(uri).getParentFile();
    URI parentURI = makeURIReference(parent);
    return parentURI;
  }

  protected File makeFileReference(URI uri) {
    // create the node reference
    File node = new File(mountPoint, uri.getPath());
    return node;
  }

  protected URI makeURIReference(File f) {
    // ie convert /tmp/fs2/a/b/c to fs2:/a/b/c
    String relativePath = f.getAbsolutePath().replace(mountPoint.getAbsolutePath(), "");
    URI u = CoreFS2Utils.genURIFromPath(relativePath);
    return u;
  }

  // TODO override from interface
  protected FS2MetaSnapshot updateObjectMeta(URI uri, String jsonMeta) throws FS2ObjectAlreadyExistsException {

    // create the node reference

    File node = new File(mountPoint, uri.getPath());

    if (!node.exists()) { throw new RuntimeException("Object not found."); }

    // write metadata and payload to file
    File metaFile = new File(node, ".meta");

    // expected: overwrite
    CoreFS2Utils.bytesToFile(jsonMeta.getBytes(), metaFile);

    return CoreFS2Utils.fromJSON(jsonMeta, FS2MetaSnapshotImpl.class);
  }

  protected FS2MetaSnapshot createObject(URI uri, String jsonMeta, InputStream payload) throws FS2ObjectAlreadyExistsException {

    // create the node reference
    // TODO make this a method and pull from props
    File node = new File(mountPoint, uri.getPath());

    if (!node.exists()) { throw new RuntimeException("Object not found."); }

    // write metadata and payload to file
    File metaFile = new File(node, ".meta");

    // expected: overwrite
    CoreFS2Utils.bytesToFile(jsonMeta.getBytes(), metaFile);

    if (null != payload) {
      File payloadFile = new File(node, ".payload");
      CoreFS2Utils.streamToFile(payload, payloadFile);
    }

    return CoreFS2Utils.fromJSON(jsonMeta, FS2MetaSnapshotImpl.class);
  }

  @Override
  public void copy(URI fromURI, URI toURI) {
    // DEEP copy - TODO Design question: should copy always be deep?
    File from = new File(mountPoint, fromURI.getPath());
    File to = new File(mountPoint, toURI.getPath());
    try {
      FileUtils.copyDirectory(from, to);
    } catch (IOException e) {
      // TODO design question: how do we want to handle exceptions like these?
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setHeaders(URI uri, FS2ObjectHeaders headers) {
    try {
      FS2MetaSnapshotImpl meta = (FS2MetaSnapshotImpl) fetchObjectMeta(uri);
      meta.setHeaders(headers);
      updateObjectMeta(uri, meta.toJSON());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void move(URI fromURI, URI toURI) throws FS2ObjectAlreadyExistsException {
    File from = new File(mountPoint, fromURI.getPath());
    File to = new File(mountPoint, toURI.getPath());
    if (to.exists()) { throw new FS2ObjectAlreadyExistsException(toURI); }
    try {
      // FileUtils.moveDirectoryToDirectory(from, to, true);

      // with this method, move a/b to c/d results in b's contents residing at
      // c/d,
      // not c/d/b (iow works like rename)
      FileUtils.moveDirectory(from, to);

      // must also update URI in meta data
      // TODO design decisions:
      // * should be consistent with how meta is updated (best practice)
      // * this code could be abstracted
      // * should be transactional (what if moved but fails? should probably
      // delete and revert?)
      // * lots of unanswered questions at this moment...
      FS2MetaSnapshot m = fetchObjectMeta(toURI);
      m = new FS2MetaSnapshotImpl(toURI, new Date(), getClass().getName(), m.getHeaders());
      updateObjectMeta(toURI, m.toJSON());

      // make sure to do the same for all sub directories recursive
      // It is also possible to filter the list of returned files.
      // This example does not return any files that start with `.'.
      Set<URI> d = listDescendants(toURI);
      for (URI u : d) {
        m = fetchObjectMeta(u); // fetch and clone, adjusting for the new URI
        m = new FS2MetaSnapshotImpl(u, new Date(), getClass().getName(), m.getHeaders());
        updateObjectMeta(u, m.toJSON());
      }

    } catch (FileExistsException fee) {
      throw new FS2ObjectAlreadyExistsException(fee.getLocalizedMessage());
    } catch (Exception e) {
      // TODO design question: how do we want to handle exceptions like these?
      throw new RuntimeException(e);
    }

  }

}