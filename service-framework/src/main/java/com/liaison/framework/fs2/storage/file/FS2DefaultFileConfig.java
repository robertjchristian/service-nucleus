package com.liaison.framework.fs2.storage.file;

import java.io.File;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.liaison.framework.fs2.api.FS2Configuration;
import com.liaison.framework.fs2.api.FS2DefaultConfiguration;

public class FS2DefaultFileConfig extends FS2DefaultConfiguration implements FS2Configuration {

  final Properties props;

  public FS2DefaultFileConfig() {
    File defaultMount = FileUtils.getTempDirectory();
    defaultMount = new File(defaultMount, "fs2");
    props = new Properties();
    props.setProperty("mount-point", defaultMount.getAbsolutePath());
  }

  @Override
  public String getStorageProvider() {
    return "file";
  }

  @Override
  public Properties getStorageProviderProperties() {
    return props;
  }

}