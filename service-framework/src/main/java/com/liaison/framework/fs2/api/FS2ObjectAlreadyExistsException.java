package com.liaison.framework.fs2.api;

import java.net.URI;

/**
 * Thrown if a requested object entry already exists in the repository.
 * 
 * IE, specified uuid already in use.
 * 
 * @author robert.christian
 */
public class FS2ObjectAlreadyExistsException extends FS2Exception {

  private static final long serialVersionUID = -3336628024317980037L;

  public FS2ObjectAlreadyExistsException(String s) {
    super(s);
  }

  public FS2ObjectAlreadyExistsException(URI uri) {
    super(uri.toString());
  }

}
