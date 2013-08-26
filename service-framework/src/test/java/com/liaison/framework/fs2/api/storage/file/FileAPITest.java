package com.liaison.framework.fs2.api.storage.file;

import com.liaison.framework.fs2.api.AbstractAPITest;
import com.liaison.framework.fs2.api.FS2Configuration;
import com.liaison.framework.fs2.api.FS2Factory;
import com.liaison.framework.fs2.api.FlexibleStorageSystem;
import com.liaison.framework.fs2.storage.file.FS2DefaultFileConfig;

/**
 * Exercises the FS2 API using the file implementation
 * 
 * @author robert.christian
 */
public class FileAPITest extends AbstractAPITest {

  @Override
  protected FlexibleStorageSystem getFS2Instance() {
    FS2Configuration config = new FS2DefaultFileConfig();
    return FS2Factory.newInstance(config);
  }

}
