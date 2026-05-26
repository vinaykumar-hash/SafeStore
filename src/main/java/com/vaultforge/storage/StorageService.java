package com.vaultforge.storage;

import java.io.InputStream;

public interface StorageService {
  void putChunk(String objectKey, InputStream data, long size);
  InputStream getChunk(String objectKey);
  void deleteChunk(String objectKey);
}
