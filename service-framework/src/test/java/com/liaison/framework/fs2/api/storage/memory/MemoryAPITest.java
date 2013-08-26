package com.liaison.framework.fs2.api.storage.memory;

import com.liaison.framework.fs2.api.AbstractAPITest;
import com.liaison.framework.fs2.api.FS2Factory;
import com.liaison.framework.fs2.api.FlexibleStorageSystem;

/**
 * Exercises the FS2 API using the in memory implementation
 * 
 * @author robert.christian
 */
public class MemoryAPITest extends AbstractAPITest {

  @Override
  protected FlexibleStorageSystem getFS2Instance() {
    return FS2Factory.newInstance("mem");
  }

}
