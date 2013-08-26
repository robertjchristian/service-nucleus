package com.liaison.framework.fs2.storage.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Streams a payload from the object repository.
 * <P>
 * <i>This class has package-level visibility.</i>
 * 
 * @author robert.christian
 */
final class FilePayloadInputStream extends InputStream {

  private InputStream is = null;

  public FilePayloadInputStream(File f) {
    try {
      this.is = new BufferedInputStream(new FileInputStream(f));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int available() throws IOException {
    return this.is.available();
  }

  @Override
  public void close() throws java.io.IOException {
    if (this.is != null) {
      this.is.close();
      this.is = null;
    }
  }

  @Override
  public int read() throws IOException {
    int iRet = -1;
    if (this.is != null) {
      iRet = this.is.read();
    }
    return iRet;
  }

  @Override
  public int read(byte[] b) throws IOException {
    int iRet = -1;
    if (this.is != null) {
      iRet = this.is.read(b);
    }
    return iRet;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    int iRet = -1;
    if (this.is != null) {
      iRet = this.is.read(b, off, len);
    }
    return iRet;
  }

  @Override
  public void reset() throws IOException {
    if (this.is == null) { throw new IOException("Inputstream is closed.  It cannot be reset."); }
    this.is.reset();
  }

}
