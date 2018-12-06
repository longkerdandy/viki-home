package com.github.longkerdandy.viki.home.hap.crypto;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * Abstract base class for class of ChaCha20 and its variants.
 *
 * <p>Variants of Snuffle have two differences: the size of the nonce and the block function that
 * produces a key stream block from a key, a nonce, and a counter. Subclasses of this class
 * specifying these two information by overriding {@link #nonceSizeInBytes} and {@link
 * #getKeyStreamBlock}.
 *
 * Inspired by Google Tink Project: https://github.com/google/tink https://github.com/google/tink/blob/master/java/src/main/java/com/google/crypto/tink/subtle/Snuffle.java
 */
public abstract class Snuffle {

  public static final int BLOCK_SIZE_IN_INTS = 16;
  public static final int BLOCK_SIZE_IN_BYTES = BLOCK_SIZE_IN_INTS * 4;
  public static final int KEY_SIZE_IN_INTS = 8;
  public static final int KEY_SIZE_IN_BYTES = KEY_SIZE_IN_INTS * 4;
  public static final int[] SIGMA = toIntArray(ByteBuffer.wrap(
      new byte[]{'e', 'x', 'p', 'a', 'n', 'd', ' ', '3', '2', '-', 'b', 'y', 't', 'e', ' ', 'k'}));
  protected final ImmutableByteArray key;
  protected final int initialCounter;

  /**
   * Create a new instance of {@link Snuffle}
   *
   * @param key A 256-bit key, treated as a concatenation of eight 32-bit little-endian integers.
   * @param initialCounter A 32-bit initial counter. This can be set to any number, but will usually
   * be zero or one.
   */
  public Snuffle(final byte[] key, int initialCounter) {
    if (key.length != KEY_SIZE_IN_BYTES) {
      throw new IllegalArgumentException("The key length in bytes must be 32.");
    }
    this.key = ImmutableByteArray.of(key);
    this.initialCounter = initialCounter;
  }

  protected static int rotateLeft(int x, int y) {
    return (x << y) | (x >>> -y);
  }

  protected static int[] toIntArray(ByteBuffer in) {
    IntBuffer intBuffer = in.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
    int[] ret = new int[intBuffer.remaining()];
    intBuffer.get(ret);
    return ret;
  }

  /**
   * Returns a key stream block from {@code nonce} and {@code counter}.
   *
   * <p>From this function, the Snuffle encryption function can be constructed using the counter
   * mode of operation. For example, the ChaCha20 block function and how it can be used to construct
   * the ChaCha20 encryption function are described in section 2.3 and 2.4 of RFC 7539.
   */
  protected abstract ByteBuffer getKeyStreamBlock(final byte[] nonce, int counter);

  /**
   * The size of the nonces.
   *
   * <p>ChaCha20 uses 12-byte nonces, but XSalsa20 and XChaCha20 use 24-byte nonces.
   */
  protected abstract int nonceSizeInBytes();

  /**
   * Encryption
   *
   * @param plaintext An arbitrary-length plaintext
   * @param nonce A {@link Snuffle#nonceSizeInBytes} nonce. In some protocols, this is known as the
   * Initialization Vector.
   * @return The output is an encrypted message, or "ciphertext", of the same length.
   */
  public byte[] encrypt(final byte[] plaintext, final byte[] nonce) {
    if (nonce.length != nonceSizeInBytes()) {
      throw new IllegalArgumentException("nonce's length must be " + nonceSizeInBytes());
    }
    ByteBuffer ciphertext = ByteBuffer.allocate(plaintext.length);
    encrypt(ciphertext, plaintext, nonce);
    return ciphertext.array();
  }

  /**
   * Encryption
   *
   * @param plaintext An arbitrary-length plaintext
   * @param nonce A {@link Snuffle#nonceSizeInBytes} nonce. In some protocols, this is known as the
   * Initialization Vector.
   * @param output The output is an encrypted message, or "ciphertext", of the same length.
   */
  protected void encrypt(ByteBuffer output, final byte[] plaintext, final byte[] nonce) {
    if (output.remaining() < plaintext.length) {
      throw new IllegalArgumentException("Given ByteBuffer output is too small");
    }
    process(nonce, output, ByteBuffer.wrap(plaintext));
  }

  /**
   * Decryption
   *
   * @param ciphertext An arbitrary-length encrypted message, or "ciphertext"
   * @param nonce A {@link Snuffle#nonceSizeInBytes} nonce.
   * @return he output is plaintext of the same length.
   */
  public byte[] decrypt(final byte[] ciphertext, final byte[] nonce) {
    return decrypt(ByteBuffer.wrap(ciphertext), nonce);
  }

  /**
   * Decryption
   *
   * @param ciphertext An arbitrary-length encrypted message, or "ciphertext"
   * @param nonce A {@link Snuffle#nonceSizeInBytes} nonce.
   * @return he output is plaintext of the same length.
   */
  protected byte[] decrypt(ByteBuffer ciphertext, final byte[] nonce) {
    ByteBuffer plaintext = ByteBuffer.allocate(ciphertext.remaining());
    process(nonce, plaintext, ciphertext);
    return plaintext.array();
  }

  /**
   * Algorithm
   */
  private void process(final byte[] nonce, ByteBuffer output, ByteBuffer input) {
    int length = input.remaining();
    int numBlocks = (length / BLOCK_SIZE_IN_BYTES) + 1;
    for (int i = 0; i < numBlocks; i++) {
      ByteBuffer keyStreamBlock = getKeyStreamBlock(nonce, i + initialCounter);
      if (i == numBlocks - 1) {
        // last block
        Bytes.xor(
            output,
            input,
            keyStreamBlock,
            length % BLOCK_SIZE_IN_BYTES);
      } else {
        Bytes.xor(
            output,
            input,
            keyStreamBlock,
            BLOCK_SIZE_IN_BYTES);
      }
    }
  }
}
