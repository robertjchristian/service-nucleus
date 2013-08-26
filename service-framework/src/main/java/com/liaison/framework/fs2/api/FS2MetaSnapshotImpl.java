package com.liaison.framework.fs2.api;

import java.io.PrintStream;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Represents a "snapshot" of a object entry that is unbound to the underlying
 * persistence store. Includes associated elements such as headers, TTL, and
 * status, at an exact point in time. Representing objects as as *immutable*
 * snapshots allows us to avoid synchronization management and dirty data
 * issues.
 * 
 * @author robert.christian
 */
@JsonIgnoreProperties({ "headerNames" })
public class FS2MetaSnapshotImpl implements FS2MetaSnapshot {

  // time this object was initially created
  protected Date createdOn;

  protected String createdBy;
  
  // time this object was fetched, and this snapshot was taken (time of last
  // read by FS2)
  protected Date snapshotCreatedOn;

  protected FS2ObjectHeaders headers;

  private URI uri;

  // eager-load and maintain an unmutable reference to the toString() value
  protected String cachedJSON;

  /**
   * Methods below this line are here to support de-serialization only.
   */

  public FS2MetaSnapshotImpl() {
  }

  // convenience
  public FS2MetaSnapshotImpl(URI uri, Date createdOn, String createdBy) {
    this(uri, createdOn, createdBy, new FS2ObjectHeaders());
  }

  public FS2MetaSnapshotImpl(URI uri, Date createdOn, String createdBy, FS2ObjectHeaders headers) {
    this.uri = uri;
    headers = null == headers.clone() ? new FS2ObjectHeaders() : headers;
    this.headers = headers;
    this.createdBy = createdBy;
    snapshotCreatedOn = new Date();
  }

  @Override
  public Date createdOn() {
    return createdOn;
  }
  
  @Override
  public String createdBy() {
    return createdBy;
  }

  @Override
  public void dump(PrintStream printStream) {
    printStream.println(this);
  }

  @Override
  public String[] getHeader(String key) {
    List<String> values = headers.getHeaders().get(key);
    return values.toArray(new String[values.size()]);
  }

  @Override
  public Set<String> getHeaderNames() {
    return headers.getHeaders().keySet();
  }

  @Override
  public FS2ObjectHeaders getHeaders() {
    return headers;
  }

  @Override
  public URI getURI() {
    return uri;
  }

  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  public void setDescription(String description) {
    cachedJSON = description;
  }

  public void setHeaders(FS2ObjectHeaders headers) {
    this.headers = headers;
  }

  public void setSnapshotCreatedOn(Date snapshotCreatedOn) {
    this.snapshotCreatedOn = snapshotCreatedOn;
  }

  public void setUri(URI uri) {
    this.uri = uri;
  }

  @Override
  public Date snapshotTime() {
    return snapshotCreatedOn;
  }

  @Override
  public String toJSON() {
    // we set this once because the "snapshot" should be immutable
    // ... the setters are not included in the interface
    if (cachedJSON != null) { return cachedJSON; }
    cachedJSON = CoreFS2Utils.toJSON(this);
    return toJSON();
  }

  // override the cache
  protected String toJSON(boolean refresh) {
    if (refresh) {
      cachedJSON = null;
    }
    return toJSON();
  }

  @Override
  public String toString() {
    // return toJSON();
    return uri.toString();
  }

}