package com.liaison.framework.fs2.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.liaison.framework.fs2.storage.file.FileStorageProvider;
import com.liaison.framework.fs2.storage.memory.MemoryStorageProvider;

/**
 * FS2 Configuration
 * 
 * General configuration that isn't related to any specific persistence
 * implementation.
 * 
 * @author robert.christian
 */
public class FS2DefaultConfiguration implements FS2Configuration {

  @Override
  public boolean doCalcCRC() {
    return true;
  }

  @Override
  public boolean doCalcMD5() {
    return true;
  }

  @Override
  public boolean doCalcPayloadSize() {
    return true;
  }

  @Override
  public String getCrcHeaderName() {
    return "FS2-CRC-Header";
  }

  @Override
  public String getMD5HeaderName() {
    return "FS2-MD5-Header";
  }

  @Override
  public String getPayloadSizeHeaderName() {
    return "FS2-Payload-Size";
  }

  @Override
  public String getStorageProvider() {
    return "mem";
  }

  @Override
  public Map<String, String> getStorageProviderMonikers() {
    // Core fs2 supports memory and file storage providers...
    // additional implementations should add to this list
    Map<String, String> monikers = new HashMap<String, String>();
    monikers.put("mem", MemoryStorageProvider.class.getName());
    monikers.put("file", FileStorageProvider.class.getName());
    return monikers;
  }

  @Override
  public Properties getStorageProviderProperties() {
    return null;
  }

}