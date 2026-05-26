package com.vaultforge.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashingUtil {
  private HashingUtil() {}

  public static String sha256(byte[] data) {
    MessageDigest digest = sha256Digest();
    byte[] hash = digest.digest(data);
    return toHex(hash);
  }

  public static MessageDigest sha256Digest() {
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }

  public static String toHex(byte[] hash) {
    StringBuilder builder = new StringBuilder();
    for (byte b : hash) {
      builder.append(String.format("%02x", b));
    }
    return builder.toString();
  }
}
