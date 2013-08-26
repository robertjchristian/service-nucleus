package com.liaison.framework.fs2.api;

import java.io.PrintStream;
import java.net.URI;
import java.util.Date;
import java.util.Set;

/**
 * This is un-editable on purpose. To update, refetch from fs2.
 * 
 * Represents a snapshot of a "Object" entry residing in a persistent data
 * store.
 * 
 * @author robert.christian
 */
public interface FS2MetaSnapshot {

  Date createdOn();

  String createdBy();
  
  void dump(PrintStream printStream);

  // obtain a single header
  String[] getHeader(String key);

  // get the set of all header names
  Set<String> getHeaderNames();

  // set of key/value pairs providing metadata for this object
  FS2ObjectHeaders getHeaders();

  URI getURI();

  // when was this snapshot taken?
  Date snapshotTime();

  // TODO add these later... should be pluggable config...
  // boolean isEncrypted();
  // boolean isCompressed();

  // JSON representation of this snapshot
  String toJSON();

}
