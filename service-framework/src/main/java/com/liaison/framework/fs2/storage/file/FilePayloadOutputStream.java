package com.liaison.framework.fs2.storage.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Streams a payload to the object repository.
 * <P>
 * <i>This class has package-level visibility.</i>
 * 
 * @author robert.christian
 */
final class FilePayloadOutputStream extends OutputStream {

  final private BufferedOutputStream os;

  public FilePayloadOutputStream(File file) {

    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    try {
      os = new BufferedOutputStream(new FileOutputStream(file));
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  @Override
  public void close() throws IOException {
    if (os != null) {
      os.close();
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
