package com.github.longkerdandy.viki.home.hap.util;

import static java.nio.charset.StandardCharsets.UTF_8;

import at.favre.lib.crypto.HKDF;
import com.github.longkerdandy.viki.home.hap.crypto.ChaCha20Poly1305;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.KeyPairGenerator;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RegExUtils;
import org.whispersystems.curve25519.Curve25519;
import org.whispersystems.curve25519.Curve25519KeyPair;

/**
 * Cipher Util
 */
public final class Ciphers {

  // SRP 3072-bit Group
  // This prime is: 2^3072 - 2^3008 - 1 + 2^64 * { [2^2942 pi] + 1690314 }
  public static final BigInteger N_3072 = new BigInteger(RegExUtils.removeAll(
      "FFFFFFFF FFFFFFFF C90FDAA2 2168C234 C4C6628B 80DC1CD1 29024E08"
          + "8A67CC74 020BBEA6 3B139B22 514A0879 8E3404DD EF9519B3 CD3A431B"
          + "302B0A6D F25F1437 4FE1356D 6D51C245 E485B576 625E7EC6 F44C42E9"
          + "A637ED6B 0BFF5CB6 F406B7ED EE386BFB 5A899FA5 AE9F2411 7C4B1FE6"
          + "49286651 ECE45B3D C2007CB8 A163BF05 98DA4836 1C55D39A 69163FA8"
          + "FD24CF5F 83655D23 DCA3AD96 1C62F356 208552BB 9ED52907 7096966D"
          + "670C354E 4ABC9804 F1746C08 CA18217C 32905E46 2E36CE3B E39E772C"
          + "180E8603 9B2783A2 EC07A28F B5C55DF0 6F4C52C9 DE2BCBF6 95581718"
          + "3995497C EA956AE5 15D22618 98FA0510 15728E5A 8AAAC42D AD33170D"
          + "04507A33 A85521AB DF1CBA64 ECFB8504 58DBEF0A 8AEA7157 5D060C7D"
          + "B3970F85 A6E1E4C7 ABF5AE8C DB0933D7 1E8C94E0 4A25619D CEE3D226"
          + "1AD2EE6B F12FFA06 D98A0864 D8760273 3EC86A64 521F2B18 177B200C"
          + "BBE11757 7A615D6C 770988C0 BAD946E2 08E24FA0 74E5AB31 43DB5BFC"
          + "E0FD108E 4B82D120 A93AD2CA FFFFFFFF FFFFFFFF", "\\s"), 16
  );

  // SRP 3072-bit Group
  // The generator is: 5
  public static final BigInteger G_3072 = BigInteger.valueOf(5L);

  // SRP SHA-512 is used as the hash function, replacing SHA-1.
  public static final String H = "SHA-512";

  private Ciphers() {
  }

  /**
   * Generate Ed25519 Private Key and Public Key
   */
  public static KeyPair ed25519KeyGen() {
    KeyPairGenerator keygen = new KeyPairGenerator();
    keygen.initialize(256, new SecureRandom());
    return keygen.generateKeyPair();
  }

  /**
   * Sign the signature with Ed25519
   *
   * @param privateKey Private Key
   * @param message that was signed
   * @return The signature of the {@code message}
   * @throws GeneralSecurityException Due to invalid {@code privateKey} or algorithm
   */
  public static byte[] ed25519Sign(byte[] privateKey, byte[] message)
      throws GeneralSecurityException {
    EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
    EdDSAEngine ed25519 = new EdDSAEngine(MessageDigest.getInstance(spec.getHashAlgorithm()));
    PrivateKey key = new EdDSAPrivateKey(new EdDSAPrivateKeySpec(privateKey, spec));
    ed25519.initSign(key);
    return ed25519.signOneShot(message);
  }

  /**
   * Verify the signature with Ed25519
   *
   * @param publicKey Public Key
   * @param message that was signed
   * @param signature of the {@code message}
   * @return True if the {@code signature} is valid, False otherwise
   * @throws GeneralSecurityException Due to invalid {@code publicKey} or algorithm
   */
  public static boolean ed25519Verify(byte[] publicKey, byte[] message, byte[] signature)
      throws GeneralSecurityException {
    EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519);
    EdDSAEngine ed25519 = new EdDSAEngine(MessageDigest.getInstance(spec.getHashAlgorithm()));
    PublicKey key = new EdDSAPublicKey(new EdDSAPublicKeySpec(publicKey, spec));
    ed25519.initVerify(key);
    return ed25519.verifyOneShot(message, signature);
  }

  /**
   * Generate Curve25519 Private Key and Public Key
   */
  public static Curve25519KeyPair curve25519KeyGen() {
    Curve25519 cipher = Curve25519.getInstance(Curve25519.BEST);
    return cipher.generateKeyPair();
  }

  /**
   * Generate Curve25519 shared secret
   *
   * @param publicKey The Curve25519 (typically remote party's) Public Key
   * @param privateKey The Curve25519 (typically yours) Private Key
   * @return A 32-byte shared secret
   */
  public static byte[] curve25519SharedSecret(byte[] publicKey, byte[] privateKey) {
    Curve25519 cipher = Curve25519.getInstance(Curve25519.BEST);
    return cipher.calculateAgreement(publicKey, privateKey);
  }

  /**
   * HKDF extract and expand
   *
   * @param inputKey Input Keying Material(IKM)
   * @param salt Salt Value
   * @param info Context and Application Specific Information
   * @param outputSize Length of Output Keying Material in bytes
   * @return byte array of Output Keying Material (OKM)
   */
  public static byte[] hkdf(byte[] inputKey, String salt, String info, int outputSize) {
    HKDF hkdf = HKDF.fromHmacSha512();
    return hkdf.extractAndExpand(salt.getBytes(UTF_8), inputKey, info.getBytes(UTF_8), outputSize);
  }

  /**
   * The HomeKit Protocol uses the 64-bit nonce option where the first 32 bits of 96-bit nonce are
   * 0
   */
  public static byte[] chaCha20Poly1305Nonce(String nonce) {
    return ArrayUtils.addAll(new byte[]{0, 0, 0, 0}, nonce.getBytes(UTF_8));
  }

  /**
   * The HomeKit Protocol uses the 64-bit nonce option where the first 32 bits of 96-bit nonce are
   * 0
   */
  public static byte[] chaCha20Poly1305Nonce(byte[] nonce) {
    if (nonce.length == 8) {
      return ArrayUtils.addAll(new byte[]{0, 0, 0, 0}, nonce);
    } else if (nonce.length == 12) {
      return nonce;
    } else {
      throw new IllegalArgumentException("nonce is either 64-bit nor 96-bit");
    }
  }

  /**
   * Encrypt with ChaCha20Poly1305
   *
   * @param key A 256-bit key
   * @param plaintext to be encrypted
   * @param nonce A 96-bit nonce
   * @return cipherText with format {actual_ciphertext || tag}
   */
  public static byte[] chaCha20Poly1305Encrypt(byte[] key, byte[] plaintext, byte[] nonce) {
    ChaCha20Poly1305 cp = new ChaCha20Poly1305(key);
    return cp.encrypt(plaintext, nonce, null);
  }

  /**
   * Encrypt with ChaCha20Poly1305
   *
   * @param key A 256-bit key
   * @param plaintext to be encrypted
   * @param nonce A 96-bit nonce
   * @param additionalData Additional Data
   * @return cipherText with format {actual_ciphertext || tag}
   */
  public static byte[] chaCha20Poly1305Encrypt(byte[] key, byte[] plaintext, byte[] nonce,
      byte[] additionalData) {
    ChaCha20Poly1305 cp = new ChaCha20Poly1305(key);
    return cp.encrypt(plaintext, nonce, additionalData);
  }

  /**
   * Decrypt with ChaCha20Poly1305
   *
   * @param key A 256-bit key
   * @param cipherText with format {actual_ciphertext || tag}
   * @param nonce A 96-bit nonce
   * @return plaintext if authentication is successful.
   * @throws GeneralSecurityException when verification or decryption failed
   */
  public static byte[] chaCha20Poly1305Decrypt(byte[] key, byte[] cipherText, byte[] nonce)
      throws GeneralSecurityException {
    ChaCha20Poly1305 cp = new ChaCha20Poly1305(key);
    return cp.decrypt(cipherText, nonce, null);
  }

  /**
   * Decrypt with ChaCha20Poly1305
   *
   * @param key A 256-bit key
   * @param cipherText with format {actual_ciphertext || tag}
   * @param nonce A 96-bit nonce
   * @param additionalData Additional Data
   * @return plaintext if authentication is successful.
   * @throws GeneralSecurityException when verification or decryption failed
   */
  public static byte[] chaCha20Poly1305Decrypt(byte[] key, byte[] cipherText, byte[] nonce,
      byte[] additionalData) throws GeneralSecurityException {
    ChaCha20Poly1305 cp = new ChaCha20Poly1305(key);
    return cp.decrypt(cipherText, nonce, additionalData);
  }

  /* ------ Codes below from org.bouncycastle.util.Pack ------ */

  public static byte[] intToLittleEndian(int n) {
    byte[] bs = new byte[4];
    intToLittleEndian(n, bs, 0);
    return bs;
  }

  public static void intToLittleEndian(int n, byte[] bs, int off) {
    bs[off] = (byte) (n);
    bs[++off] = (byte) (n >>> 8);
    bs[++off] = (byte) (n >>> 16);
    bs[++off] = (byte) (n >>> 24);
  }

  public static byte[] longToLittleEndian(long n) {
    byte[] bs = new byte[8];
    longToLittleEndian(n, bs, 0);
    return bs;
  }

  public static void longToLittleEndian(long n, byte[] bs, int off) {
    intToLittleEndian((int) (n & 0xffffffffL), bs, off);
    intToLittleEndian((int) (n >>> 32), bs, off + 4);
  }

  /* ------ Codes above from org.bouncycastle.util.Pack ------ */
}
