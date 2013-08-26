package com.liaison.framework.fs2.api;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Basic key/value metadata holder for storage objects
 * 
 * @author robertchristian
 */

public class FS2ObjectHeaders {

  private HashMap<String, ArrayList<String>> headers = new HashMap<String, ArrayList<String>>();

  public FS2ObjectHeaders() {
  }

  public void addHeader(String name, String value) {
    if (!headers.containsKey(name)) {
      // not in map yet, create list
      headers.put(name, new ArrayList<String>());
    }
    headers.get(name).add(value);
  }

  public HashMap<String, ArrayList<String>> getHeaders() {
    return headers;
  }

  public void removeHeader(String name) {
    headers.remove(name);
  }

  public void setHeaders(HashMap<String, ArrayList<String>> headers) {
    this.headers = headers;
  }

  // clone these headers
  @Override
  public FS2ObjectHeaders clone() {
    HashMap<String, ArrayList<String>> fromHeaders = headers;
    HashMap<String, ArrayList<String>> toHeaders = new HashMap<String, ArrayList<String>>();

    for (String key : fromHeaders.keySet()) {
      toHeaders.put(key, new ArrayList<String>());
      for (String value : fromHeaders.get(key)) {
        toHeaders.get(key).add(value);
      }
    }

    FS2ObjectHeaders f = new FS2ObjectHeaders();
    f.setHeaders(toHeaders);
    return f;
  }

}