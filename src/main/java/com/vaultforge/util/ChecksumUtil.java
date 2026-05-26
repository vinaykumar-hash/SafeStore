package com.vaultforge.util;

import java.nio.charset.StandardCharsets;

public final class ChecksumUtil {
  private ChecksumUtil() {}

  public static String checksumFromString(String value) {
    return HashingUtil.sha256(value.getBytes(StandardCharsets.UTF_8));
  }
}
