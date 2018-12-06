package com.github.longkerdandy.viki.home.util;

import java.util.Random;

/**
 * Fancy ID generator that creates 20-character string identifiers with the following properties:
 *
 * 1. They're based on timestamp so that they sort *after* any existing ids.
 *
 * 2. They contain 72-bits of random data after the timestamp so that IDs won't collide with other
 * clients' IDs.
 *
 * 3. They sort *lexicographically* (so the timestamp is converted to characters that will sort
 * properly).
 *
 * 4. They're monotonically increasing.  Even if you generate more than one in the same timestamp,
 * the latter ones will sort after the former ones.  We do this by using the previous random bits
 * but "incrementing" them by 1 (only in the case of a timestamp collision).
 *
 * Inspired by Firbase's PushId.
 *
 * Blog post: https://firebase.googleblog.com/2015/02/the-2120-ways-to-ensure-unique_68.html?m=1
 * Github gist: https://gist.github.com/mikelehen/3596a30bd69384624c11
 */
public class Ids {

  // Modeled after base64 web-safe chars, but ordered by ASCII.
  private final static String ID_CHARS = "-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz";

  // Pseudorandom number generator
  private final Random random;

  // Timestamp of last push, used to prevent local collisions if you push twice in one ms.
  private long lastTimestamp = 0L;

  // We generate 72-bits of randomness which get turned into 12 characters and appended to the
  // timestamp to prevent collisions with other clients.  We store the last characters we
  // generated because in the event of a collision, we'll use those same characters except
  // "incremented" by one.
  private int[] lastRandChars = new int[12];

  /**
   * Create a new IdGenerator with specific Random instance
   *
   * @param random Random, SecureRandom or ThreadLocalRandom
   */
  public Ids(Random random) {
    this.random = random;
  }

  /**
   * Generate next chronological 20-character unique id
   *
   * @return New ID
   */
  public String nextId() {
    long now = System.currentTimeMillis();
    char[] result = new char[20];

    // random chars
    int[] randChars = compareAndUpdate(now);
    for (int i = 0; i < 12; i++) {
      result[i + 8] = ID_CHARS.charAt(randChars[i]);
    }

    // timestamp chars
    for (int i = 7; i >= 0; i--) {
      final Long module = now % 64;
      result[i] = ID_CHARS.charAt(module.intValue());
      now = (long) Math.floor(now / 64f);
    }
    if (now != 0) {
      throw new AssertionError("We should have converted the entire timestamp.");
    }

    return new String(result);
  }

  /**
   * Compare the timestamp and update last record state. To be thread safe, all the compare and
   * update operation are included in this method
   *
   * @param timestamp Timestamp
   * @return Random Chars should be used
   */
  private synchronized int[] compareAndUpdate(long timestamp) {
    if (timestamp != this.lastTimestamp) {
      // If the timestamp has changed since last request, create a new random number.
      this.lastTimestamp = timestamp;
      for (int i = 0; i < 12; i++) {
        this.lastRandChars[i] = this.random.nextInt(64);
      }
    } else {
      // If the timestamp hasn't changed since last request, use the same random number, except incremented by 1.
      int i;
      for (i = 11; i >= 0 && lastRandChars[i] == 63; i--) {
        this.lastRandChars[i] = 0;
      }
      if (i < 0) {
        i = 11;
      }
      this.lastRandChars[i]++;
    }
    // return a new array
    return this.lastRandChars.clone();
  }
}
