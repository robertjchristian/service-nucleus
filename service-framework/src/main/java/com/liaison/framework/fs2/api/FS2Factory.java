package com.liaison.framework.fs2.api;

/**
 * @author robert.christian
 */
public class FS2Factory {

  public static FlexibleStorageSystem newInstance(final FS2Configuration config) {
    return new FlexibleStorageSystemImpl(config);
  }

  /**
   * create a new FS2 instance with default parameters, overriding only the
   * backing storage provider
   */
  public static FlexibleStorageSystem newInstance(final String moniker) {

    FS2Configuration config = new FS2DefaultConfiguration() {
      @Override
      public String getStorageProvider() {
        return moniker;
      }
    };

    return newInstance(config);
  }
  // TODO move these out into a factory

}
