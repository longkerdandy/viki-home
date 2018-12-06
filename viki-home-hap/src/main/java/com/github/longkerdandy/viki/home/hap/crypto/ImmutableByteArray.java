package com.github.longkerdandy.viki.home.hap.crypto;

/**
 * Immutable Wrapper around a byte array.
 *
 * <p>Wrap a bytearray so it prevents callers from modifying its contents. It does this by making a
 * copy upon initialization, and also makes a copy if the underlying bytes are requested.
 *
 * Inspired by Google Tink Project: https://github.com/google/tink https://github.com/google/tink/blob/master/java/src/main/java/com/google/crypto/tink/subtle/ImmutableByteArray.java
 */
public final class ImmutableByteArray {

  private final byte[] data;

  private ImmutableByteArray(final byte[] buf, final int start, final int len) {
    data = new byte[len];
    System.arraycopy(buf, start, data, 0, len);
  }

  /**
   * @param data the byte array to be wrapped.
   * @return an immutable wrapper around the provided bytes.
   */
  public static ImmutableByteArray of(final byte[] data) {
    if (data == null) {
      return null;
    } else {
      return of(data, 0, data.length);
    }
  }

  /**
   * Wrap an immutable byte array over a slice of a bytearray
   *
   * @param data the byte array to be wrapped.
   * @param start the starting index of the slice
   * @param len the length of the slice. start + len must be less than the length of the array.
   * @return an immutable wrapper around the bytes in the slice from {@code start} to {@code start +
   * len}
   */
  public static ImmutableByteArray of(final byte[] data, final int start, final int len) {
    return new ImmutableByteArray(data, start, len);
  }

  /**
   * @return a copy of the bytes wrapped by this object.
   */
  public byte[] getBytes() {
    byte[] result = new byte[data.length];
    System.arraycopy(data, 0, result, 0, data.length);
    return result;
  }

  /**
   * @return the length of the bytes wrapped by this object.
   */
  public int getLength() {
    return data.length;
  }
}
