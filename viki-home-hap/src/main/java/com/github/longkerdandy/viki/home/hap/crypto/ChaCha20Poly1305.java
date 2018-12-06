package com.github.longkerdandy.viki.home.hap.crypto;

/**
 * ChaCha20-Poly1305, as described in <a href="https://tools.ietf.org/html/rfc7539#section-2.8">RFC
 * 7539, section 2.8</a>.
 *
 * Inspired by Google Tink Project: https://github.com/google/tink https://github.com/google/tink/blob/master/java/src/main/java/com/google/crypto/tink/subtle/ChaCha20Poly1305.java
 */
public class ChaCha20Poly1305 extends SnufflePoly1305 {

  /**
   * Create a new instance of {@link ChaCha20Poly1305}
   *
   * AEAD_CHACHA20_POLY1305 is an authenticated encryption with additional data algorithm.
   *
   * @param key A 256-bit key
   */
  public ChaCha20Poly1305(final byte[] key) {
    super(key);
  }

  @Override
  protected Snuffle createSnuffleInstance(final byte[] key, int initialCounter) {
    return new ChaCha20(key, initialCounter);
  }
}
