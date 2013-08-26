package com.liaison.framework.fs2.api;

import java.net.URI;

/**
 * Thrown if a requested object entry does not exist in the repository.
 * 
 * @author robert.christian
 */
public class FS2ObjectNotFoundException extends FS2Exception {

  private static final long serialVersionUID = -3336628024317980037L;

  public FS2ObjectNotFoundException() {
    super();
  }

  public FS2ObjectNotFoundException(URI uri) {
    super(uri.toString());
  }

  public FS2ObjectNotFoundException(URI uri, Throwable t) {
    super(uri.toString(), t);
  }

}
