package com.liaison.framework.fs2.api;

import java.util.Map;
import java.util.Properties;

/**
 * API-level configuration.
 * 
 * Controls turning on/off additional features to be minded by the underlying
 * storage provider implementations. For the most part, this consists of (a)
 * asking "whether to perform function xyz" and (b) asking "what should the
 * metadata header name be for storing the result of the xyz"
 * 
 * NOTE: The features configured here should be agnostic of persistence-layer-
 * specific parameters such as "what is the filesystem mountpoint" or "what is
 * the database password" ... those belong in their respective storage-provider
 * specific implementations.
 * 
 * @author robertchristian
 */
public interface FS2Configuration {

  // TODO to/from JSON enables us to serialize
  // TODO include compression, encryption

  // whether to calculate crc upon close of object output stream,
  // and name of the associated header used to store the result
  boolean doCalcCRC();

  // whether to calculate md5 upon close of object output stream,
  // and name of the associated header used to store the result
  boolean doCalcMD5();

  // whether to calculate size upon close of object output stream,
  // and name of the associated header used to store the result
  boolean doCalcPayloadSize();

  String getCrcHeaderName();

  String getMD5HeaderName();

  String getPayloadSizeHeaderName();

  // moniker of backing storage provider
  String getStorageProvider();

  // maps storage provider monikers to class names
  // note: we don't model with class because fs2 is
  // only compiled and bundled with memory and file
  // implementations
  Map<String, String> getStorageProviderMonikers();

  Properties getStorageProviderProperties();

}