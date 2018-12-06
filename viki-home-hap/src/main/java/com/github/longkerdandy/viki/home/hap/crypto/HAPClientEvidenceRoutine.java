package com.github.longkerdandy.viki.home.hap.crypto;

import static com.github.longkerdandy.viki.home.hap.crypto.Bytes.xor;
import static com.nimbusds.srp6.BigIntegerUtils.bigIntegerFromBytes;
import static com.nimbusds.srp6.BigIntegerUtils.bigIntegerToBytes;

import com.nimbusds.srp6.ClientEvidenceRoutine;
import com.nimbusds.srp6.SRP6ClientEvidenceContext;
import com.nimbusds.srp6.SRP6CryptoParams;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Custom routine for computing the client evidence message 'M1'.
 *
 * This version compatible with Homekit Accessory Protocol.
 *
 * Inspired by HAP-Java Project: https://github.com/beowulfe/HAP-Java/blob/master/src/main/java/com/beowulfe/hap/impl/pairing/ClientEvidenceRoutineImpl.java
 */
public class HAPClientEvidenceRoutine implements ClientEvidenceRoutine {

  /**
   * Computes a client evidence message 'M1' with following formula:
   *
   * M1 = H(H(N) xor H(g) || H(username) || s || A || B || H(S))
   */
  @Override
  public BigInteger computeClientEvidence(SRP6CryptoParams cryptoParams,
      SRP6ClientEvidenceContext ctx) {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance(cryptoParams.H);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalArgumentException("Could not locate requested algorithm", e);
    }
    digest.update(bigIntegerToBytes(cryptoParams.N));
    byte[] hN = digest.digest();

    digest.update(bigIntegerToBytes(cryptoParams.g));
    byte[] hg = digest.digest();

    byte[] hNhg = xor(hN, hg);

    digest.update(ctx.userID.getBytes(StandardCharsets.UTF_8));
    byte[] hu = digest.digest();

    digest.update(bigIntegerToBytes(ctx.S));
    byte[] hS = digest.digest();

    digest.update(hNhg);
    digest.update(hu);
    digest.update(bigIntegerToBytes(ctx.s));
    digest.update(bigIntegerToBytes(ctx.A));
    digest.update(bigIntegerToBytes(ctx.B));
    digest.update(hS);

    return bigIntegerFromBytes(digest.digest());
  }
}
