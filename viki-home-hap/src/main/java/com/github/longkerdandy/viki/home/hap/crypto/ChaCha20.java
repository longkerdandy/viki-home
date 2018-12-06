package com.github.longkerdandy.viki.home.hap.crypto;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A stream cipher based on RFC7539 (i.e., uses 96-bit random nonces)
 * https://tools.ietf.org/html/rfc7539
 *
 * Inspired by Google Tink Project: https://github.com/google/tink https://github.com/google/tink/blob/master/java/src/main/java/com/google/crypto/tink/subtle/ChaCha20.java
 */
public class ChaCha20 extends Snuffle {

  /**
   * Create a new instance of {@link ChaCha20}
   *
   * @param key A 256-bit key, treated as a concatenation of eight 32-bit little-endian integers.
   * @param initialCounter A 32-bit initial counter.
   */
  public ChaCha20(final byte[] key, int initialCounter) {
    super(key, initialCounter);
  }

  private static void setSigma(int[] state) {
    System.arraycopy(Snuffle.SIGMA, 0, state, 0, SIGMA.length);
  }

  private static void setKey(int[] state, final byte[] key) {
    int[] keyInt = toIntArray(ByteBuffer.wrap(key));
    System.arraycopy(keyInt, 0, state, 4, keyInt.length);
  }

  /**
   * Algorithm
   */
  protected static void shuffleState(final int[] state) {
    for (int i = 0; i < 10; i++) {
      quarterRound(state, 0, 4, 8, 12);
      quarterRound(state, 1, 5, 9, 13);
      quarterRound(state, 2, 6, 10, 14);
      quarterRound(state, 3, 7, 11, 15);
      quarterRound(state, 0, 5, 10, 15);
      quarterRound(state, 1, 6, 11, 12);
      quarterRound(state, 2, 7, 8, 13);
      quarterRound(state, 3, 4, 9, 14);
    }
  }

  /**
   * Algorithm
   */
  protected static void quarterRound(int[] x, int a, int b, int c, int d) {
    x[a] += x[b];
    x[d] = rotateLeft(x[d] ^ x[a], 16);
    x[c] += x[d];
    x[b] = rotateLeft(x[b] ^ x[c], 12);
    x[a] += x[b];
    x[d] = rotateLeft(x[d] ^ x[a], 8);
    x[c] += x[d];
    x[b] = rotateLeft(x[b] ^ x[c], 7);
  }

  /**
   * Returns the initial state from {@code nonce} and {@code counter}.
   *
   * <p>ChaCha20 has a different logic than XChaCha20, because the former uses a 12-byte nonce, but
   * the later uses 24-byte.
   */
  private int[] createInitialState(final byte[] nonce, int counter) {
    // Set the initial state based on https://tools.ietf.org/html/rfc7539#section-2.3
    int[] state = new int[Snuffle.BLOCK_SIZE_IN_INTS];
    setSigma(state);
    setKey(state, key.getBytes());
    state[12] = counter;
    System.arraycopy(toIntArray(ByteBuffer.wrap(nonce)), 0, state, 13, nonceSizeInBytes() / 4);
    return state;
  }

  /**
   * A 96-bit nonce.
   */
  @Override
  protected int nonceSizeInBytes() {
    return 12;
  }

  /**
   * The ChaCha20 block function
   *
   * @param nonce A 96-bit nonce, treated as a concatenation of three 32-bit little-endian
   * integers.
   * @param counter A 32-bit block count parameter, treated as a 32-bit little-endian integer.
   * @return The output is 64 random-looking bytes.
   */
  @Override
  protected ByteBuffer getKeyStreamBlock(final byte[] nonce, int counter) {
    int[] state = createInitialState(nonce, counter);
    int[] workingState = state.clone();
    shuffleState(workingState);
    for (int i = 0; i < state.length; i++) {
      state[i] += workingState[i];
    }
    ByteBuffer out = ByteBuffer.allocate(BLOCK_SIZE_IN_BYTES).order(ByteOrder.LITTLE_ENDIAN);
    out.asIntBuffer().put(state, 0, BLOCK_SIZE_IN_INTS);
    return out;
  }
}
