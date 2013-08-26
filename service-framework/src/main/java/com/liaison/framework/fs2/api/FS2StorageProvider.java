package com.liaison.framework.fs2.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Set;

/**
 * Concrete persistence implementations will back this interface.
 * 
 * @author robert.christian
 */
public interface FS2StorageProvider {

  void addHeader(URI uri, String name, String value) throws FS2PayloadNotFoundException, Exception;

  // Hint: Implement these in the order they appear...

  FS2MetaSnapshot createObjectEntry(URI uri) throws FS2ObjectAlreadyExistsException;

  FS2MetaSnapshot createObjectEntry(URI uri, String jsonMeta, byte[] payload) throws FS2ObjectAlreadyExistsException;

  FS2MetaSnapshot createObjectEntry(URI uri, String jsonMeta, InputStream payload) throws FS2ObjectAlreadyExistsException;

  void delete(URI uri);

  void deletePayload(URI uri);

  void deleteRecursive(URI uri);

  boolean exists(URI uri);

  InputStream fetchFS2PayloadInputStream(URI uri);

  OutputStream fetchFS2PayloadOutputStream(URI uri);

  FS2MetaSnapshot fetchObjectMeta(URI uri) throws FS2PayloadNotFoundException;

  Long getPayloadSizeInBytes(URI uri);

  boolean hasDescendants(URI uri);

  void init(FS2Configuration config);

  Set<URI> listChilden(URI uri);

  Set<URI> listDescendants(URI uri);

  URI listParent(URI uri);

  void copy(URI fromURI, URI toURI);

  public void move(URI oldURI, URI newURI) throws FS2ObjectAlreadyExistsException;

  void setHeaders(URI uri, FS2ObjectHeaders headers);

}
