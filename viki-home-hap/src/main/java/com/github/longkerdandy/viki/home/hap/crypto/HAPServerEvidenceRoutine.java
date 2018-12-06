package com.github.longkerdandy.viki.home.hap.crypto;

import static com.nimbusds.srp6.BigIntegerUtils.bigIntegerFromBytes;
import static com.nimbusds.srp6.BigIntegerUtils.bigIntegerToBytes;

import com.nimbusds.srp6.SRP6CryptoParams;
import com.nimbusds.srp6.SRP6ServerEvidenceContext;
import com.nimbusds.srp6.ServerEvidenceRoutine;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Custom routine interface for computing the server evidence message 'M2'.
 *
 * This version compatible with Homekit Accessory Protocol.
 *
 * Inspired by HAP-Java Project: https://github.com/beowulfe/HAP-Java/blob/master/src/main/java/com/beowulfe/hap/impl/pairing/ServerEvidenceRoutineImpl.java
 */
public class HAPServerEvidenceRoutine implements ServerEvidenceRoutine {

  /**
   * Computes a server evidence message 'M2' with following formula:
   *
   * M2 = H(A || M1 || H(S))
   */
  @Override
  public BigInteger computeServerEvidence(SRP6CryptoParams cryptoParams,
      SRP6ServerEvidenceContext ctx) {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance(cryptoParams.H);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalArgumentException("Could not locate requested algorithm", e);
    }

    byte[] hS = digest.digest(bigIntegerToBytes(ctx.S));

    digest.update(bigIntegerToBytes(ctx.A));
    digest.update(bigIntegerToBytes(ctx.M1));
    digest.update(hS);

    return bigIntegerFromBytes(digest.digest());
  }
}
