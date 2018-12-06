package com.github.longkerdandy.viki.home.hap.crypto;

import static com.github.longkerdandy.viki.home.hap.crypto.Poly1305.MAC_KEY_SIZE_IN_BYTES;
import static com.github.longkerdandy.viki.home.hap.crypto.Poly1305.MAC_TAG_SIZE_IN_BYTES;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.GeneralSecurityException;

/**
 * An Aead construction with a {@link Snuffle} and {@link Poly1305}, following RFC 7539, section
 * 2.8.
 *
 * <p>This implementation produces ciphertext with the following format:
 * {actual_ciphertext || tag} and only decrypts the same format.
 *
 * Inspired by Google Tink Project: https://github.com/google/tink https://github.com/google/tink/blob/master/java/src/main/java/com/google/crypto/tink/subtle/SnufflePoly1305.java
 */
public abstract class SnufflePoly1305 {

  private final Snuffle snuffle;
  private final Snuffle macKeysnuffle;

  /**
   * Create a new instance of {@link SnufflePoly1305}
   *
   * AEAD_CHACHA20_POLY1305 is an authenticated encryption with additional data algorithm.
   *
   * @param key A 256-bit key
   */
  public SnufflePoly1305(final byte[] key) {
    this.snuffle = createSnuffleInstance(key, 1);
    this.macKeysnuffle = createSnuffleInstance(key, 0);
  }

  /**
   * Prepares the input to MAC, following RFC 7539, section 2.8.
   */
  private static byte[] macDataRfc7539(final byte[] aad, ByteBuffer ciphertext) {
    int aadPaddedLen = (aad.length % 16 == 0) ? aad.length : (aad.length + 16 - aad.length % 16);
    int ciphertextLen = ciphertext.remaining();
    int ciphertextPaddedLen =
        (ciphertextLen % 16 == 0) ? ciphertextLen : (ciphertextLen + 16 - ciphertextLen % 16);
    ByteBuffer macData =
        ByteBuffer.allocate(aadPaddedLen + ciphertextPaddedLen + 16).order(ByteOrder.LITTLE_ENDIAN);
    macData.put(aad);
    macData.position(aadPaddedLen);
    macData.put(ciphertext);
    macData.position(aadPaddedLen + ciphertextPaddedLen);
    macData.putLong(aad.length);
    macData.putLong(ciphertextLen);
    return macData.array();
  }

  /**
   * Create a new instance of {@link Snuffle}
   *
   * @param key A 256-bit key, treated as a concatenation of eight 32-bit little-endian integers.
   * @param initialCounter A 32-bit initial counter. This can be set to any number, but will usually
   * be zero or one.
   */
  protected abstract Snuffle createSnuffleInstance(final byte[] key, int initialCounter);

  /**
   * Encrypts the {@code plaintext} with Poly1305 authentication based on {@code nonce} and {@code
   * additionalData}.
   *
   * @param plaintext An arbitrary length plaintext
   * @param nonce A 96-bit nonce -- different for each invocation with the same key
   * @param additionalData Arbitrary length additional authenticated data (AAD)
   * @return The ciphertext with the following format {actual_ciphertext || tag}
   */
  public byte[] encrypt(final byte[] plaintext, final byte[] nonce, final byte[] additionalData) {
    if (plaintext.length > Integer.MAX_VALUE - MAC_TAG_SIZE_IN_BYTES) {
      throw new IllegalArgumentException("plaintext too long");
    }
    ByteBuffer ciphertext = ByteBuffer.allocate(plaintext.length + MAC_TAG_SIZE_IN_BYTES);
    encrypt(ciphertext, plaintext, nonce, additionalData);
    return ciphertext.array();
  }

  /**
   * Encrypts the {@code plaintext} with Poly1305 authentication based on {@code nonce} and {@code
   * additionalData}.
   *
   * @param plaintext An arbitrary length plaintext
   * @param nonce A 96-bit nonce
   * @param additionalData Arbitrary length additional authenticated data (AAD)
   * @param output The ciphertext with the following format {actual_ciphertext || tag}
   */
  protected void encrypt(ByteBuffer output, final byte[] plaintext, final byte[] nonce,
      final byte[] additionalData) {
    if (output.remaining() < plaintext.length + MAC_TAG_SIZE_IN_BYTES) {
      throw new IllegalArgumentException("Given ByteBuffer output is too small");
    }
    int firstPosition = output.position();
    this.snuffle.encrypt(output, plaintext, nonce);
    output.position(firstPosition);
    output.limit(output.limit() - MAC_TAG_SIZE_IN_BYTES);
    byte[] aad = additionalData;
    if (aad == null) {
      aad = new byte[0];
    }
    byte[] tag = Poly1305.computeMac(getMacKey(nonce), macDataRfc7539(aad, output));
    output.limit(output.limit() + MAC_TAG_SIZE_IN_BYTES);
    output.put(tag);
  }

  /**
   * Decrypts {@code ciphertext} with the following format: {actual_ciphertext || tag}
   *
   * @param ciphertext with format {actual_ciphertext || tag}
   * @param nonce nonce A 96-bit nonce
   * @param additionalData Arbitrary length additional authenticated data (AAD)
   * @return plaintext if authentication is successful.
   * @throws GeneralSecurityException when ciphertext is shorter than nonce size + tag size or when
   * computed tag based on {@code ciphertext} and {@code additionalData} does not match the tag
   * given in {@code ciphertext}.
   */
  public byte[] decrypt(final byte[] ciphertext, final byte[] nonce, final byte[] additionalData)
      throws GeneralSecurityException {
    return decrypt(ByteBuffer.wrap(ciphertext), nonce, additionalData);
  }

  /**
   * Decrypts {@code ciphertext} with the following format: {ctual_ciphertext || tag}
   *
   * @param ciphertext with format {actual_ciphertext || tag}
   * @param nonce nonce
   * @param associatedData associated authenticated data
   * @return plaintext if authentication is successful
   * @throws GeneralSecurityException when tag verification failed
   */
  private byte[] decrypt(ByteBuffer ciphertext, final byte[] nonce, final byte[] associatedData)
      throws GeneralSecurityException {
    if (ciphertext.remaining() < MAC_TAG_SIZE_IN_BYTES) {
      throw new IllegalArgumentException("ciphertext too short");
    }
    int firstPosition = ciphertext.position();
    byte[] tag = new byte[MAC_TAG_SIZE_IN_BYTES];
    ciphertext.position(ciphertext.limit() - MAC_TAG_SIZE_IN_BYTES);
    ciphertext.get(tag);
    // rewind to read ciphertext and compute tag.
    ciphertext.position(firstPosition);
    ciphertext.limit(ciphertext.limit() - MAC_TAG_SIZE_IN_BYTES);
    byte[] aad = associatedData;
    if (aad == null) {
      aad = new byte[0];
    }

    // verify tag
    Poly1305.verifyMac(getMacKey(nonce), macDataRfc7539(aad, ciphertext), tag);

    // rewind to decrypt the ciphertext.
    ciphertext.position(firstPosition);
    return this.snuffle.decrypt(ciphertext, nonce);
  }

  /**
   * The MAC key is the first 32 bytes of the first key stream block
   */
  private byte[] getMacKey(final byte[] nonce) {
    ByteBuffer firstBlock = macKeysnuffle.getKeyStreamBlock(nonce, 0 /* counter */);
    byte[] result = new byte[MAC_KEY_SIZE_IN_BYTES];
    firstBlock.get(result);
    return result;
  }
}
