package com.github.longkerdandy.viki.home.hap.crypto;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

public class Poly1305Test {

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols 2.5.2. Poly1305 Example and Test Vector
   */
  @Test
  public void testVector0() throws DecoderException, GeneralSecurityException {
    byte[] key = Hex.decodeHex(""
        + "85d6be7857556d337f4452fe42d506a8"
        + "0103808afb0db2fd4abff6af4149f51b");
    byte[] data = "Cryptographic Forum Research Group".getBytes(StandardCharsets.UTF_8);
    byte[] tag = Poly1305.computeMac(key, data);
    assert Hex.encodeHexString(tag).equalsIgnoreCase("a8061dc1305136c6c22b8baf0c0127a9");
    Poly1305.verifyMac(key, data, tag);
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols 2.5.2. Poly1305 Example and Test Vector
   */
  @Test(expected = GeneralSecurityException.class)
  public void testVectorFail() throws DecoderException, GeneralSecurityException {
    byte[] key = Hex.decodeHex(""
        + "85d6be7857556d337f4452fe42d506a8"
        + "0103808afb0db2fd4abff6af4149f51b");
    byte[] data = "Cryptographic Forum Research Group".getBytes(StandardCharsets.UTF_8);
    byte[] tag = Hex.decodeHex("00000000000000000000000000000000");
    Poly1305.verifyMac(key, data, tag);
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.3. Poly1305 Message Authentication Code,
   * Test Vector #1
   */
  @Test
  public void testVector1() throws DecoderException, GeneralSecurityException {
    byte[] key = Hex.decodeHex(""
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000000");
    byte[] data = Hex.decodeHex(""
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000000");
    byte[] tag = Poly1305.computeMac(key, data);
    assert Hex.encodeHexString(tag).equalsIgnoreCase("00000000000000000000000000000000");
    Poly1305.verifyMac(key, data, tag);
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.3. Poly1305 Message Authentication Code,
   * Test Vector #2
   */
  @Test
  public void testVector2() throws DecoderException, GeneralSecurityException {
    byte[] key = Hex.decodeHex(""
        + "00000000000000000000000000000000"
        + "36e5f6b5c5e06070f0efca96227a863e");
    byte[] data = (""
        + "Any submission to the IETF intended by the Contributor for publication as all or"
        + " part of an IETF Internet-Draft or RFC and any statement made within the context"
        + " of an IETF activity is considered an \"IETF Contribution\". Such statements "
        + "include oral statements in IETF sessions, as well as written and electronic "
        + "communications made at any time or place, which are addressed to")
        .getBytes(StandardCharsets.UTF_8);
    byte[] tag = Poly1305.computeMac(key, data);
    assert Hex.encodeHexString(tag).equalsIgnoreCase("36e5f6b5c5e06070f0efca96227a863e");
    Poly1305.verifyMac(key, data, tag);
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.3. Poly1305 Message Authentication Code,
   * Test Vector #3
   */
  @Test
  public void testVector3() throws DecoderException, GeneralSecurityException {
    byte[] key = Hex.decodeHex(""
        + "36e5f6b5c5e06070f0efca96227a863e"
        + "00000000000000000000000000000000");
    byte[] data = (""
        + "Any submission to the IETF intended by the Contributor for publication as all or"
        + " part of an IETF Internet-Draft or RFC and any statement made within the context"
        + " of an IETF activity is considered an \"IETF Contribution\". Such statements "
        + "include oral statements in IETF sessions, as well as written and electronic "
        + "communications made at any time or place, which are addressed to")
        .getBytes(StandardCharsets.UTF_8);
    byte[] tag = Poly1305.computeMac(key, data);
    assert Hex.encodeHexString(tag).equalsIgnoreCase("f3477e7cd95417af89a6b8794c310cf0");
    Poly1305.verifyMac(key, data, tag);
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.3. Poly1305 Message Authentication Code,
   * Test Vector #4
   */
  @Test
  public void testVector4() throws DecoderException, GeneralSecurityException {
    byte[] key = Hex.decodeHex(""
        + "1c9240a5eb55d38af333888604f6b5f0"
        + "473917c1402b80099dca5cbc207075c0");
    byte[] data = (""
        + "'Twas brillig, and the slithy toves\n"
        + "Did gyre and gimble in the wabe:\n"
        + "All mimsy were the borogoves,\n"
        + "And the mome raths outgrabe.")
        .getBytes(StandardCharsets.UTF_8);
    byte[] tag = Poly1305.computeMac(key, data);
    assert Hex.encodeHexString(tag).equalsIgnoreCase("4541669a7eaaee61e708dc7cbcc5eb62");
    Poly1305.verifyMac(key, data, tag);
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.3. Poly1305 Message Authentication Code,
   * Test Vector #5
   */
  @Test
  public void testVector5() throws DecoderException, GeneralSecurityException {
    byte[] key = Hex.decodeHex(""
        + "02000000000000000000000000000000"
        + "00000000000000000000000000000000");
    byte[] data = Hex.decodeHex(""
        + "ffffffffffffffffffffffffffffffff");
    byte[] tag = Poly1305.computeMac(key, data);
    assert Hex.encodeHexString(tag).equalsIgnoreCase("03000000000000000000000000000000");
    Poly1305.verifyMac(key, data, tag);
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.3. Poly1305 Message Authentication Code,
   * Test Vector #6
   */
  @Test
  public void testVector6() throws DecoderException, GeneralSecurityException {
    byte[] key = Hex.decodeHex(""
        + "02000000000000000000000000000000"
        + "ffffffffffffffffffffffffffffffff");
    byte[] data = Hex.decodeHex(""
        + "02000000000000000000000000000000");
    byte[] tag = Poly1305.computeMac(key, data);
    assert Hex.encodeHexString(tag).equalsIgnoreCase("03000000000000000000000000000000");
    Poly1305.verifyMac(key, data, tag);
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.3. Poly1305 Message Authentication Code,
   * Test Vector #7
   */
  @Test
  public void testVector7() throws DecoderException, GeneralSecurityException {
    byte[] key = Hex.decodeHex(""
        + "01000000000000000000000000000000"
        + "00000000000000000000000000000000");
    byte[] data = Hex.decodeHex(""
        + "ffffffffffffffffffffffffffffffff"
        + "f0ffffffffffffffffffffffffffffff"
        + "11000000000000000000000000000000");
    byte[] tag = Poly1305.computeMac(key, data);
    assert Hex.encodeHexString(tag).equalsIgnoreCase("05000000000000000000000000000000");
    Poly1305.verifyMac(key, data, tag);
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.3. Poly1305 Message Authentication Code,
   * Test Vector #8
   */
  @Test
  public void testVector8() throws DecoderException, GeneralSecurityException {
    byte[] key = Hex.decodeHex(""
        + "01000000000000000000000000000000"
        + "00000000000000000000000000000000");
    byte[] data = Hex.decodeHex(""
        + "ffffffffffffffffffffffffffffffff"
        + "fbfefefefefefefefefefefefefefefe"
        + "01010101010101010101010101010101");
    byte[] tag = Poly1305.computeMac(key, data);
    assert Hex.encodeHexString(tag).equalsIgnoreCase("00000000000000000000000000000000");
    Poly1305.verifyMac(key, data, tag);
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.3. Poly1305 Message Authentication Code,
   * Test Vector #9
   */
  @Test
  public void testVector9() throws DecoderException, GeneralSecurityException {
    byte[] key = Hex.decodeHex(""
        + "02000000000000000000000000000000"
        + "00000000000000000000000000000000");
    byte[] data = Hex.decodeHex(""
        + "fdffffffffffffffffffffffffffffff");
    byte[] tag = Poly1305.computeMac(key, data);
    assert Hex.encodeHexString(tag).equalsIgnoreCase("faffffffffffffffffffffffffffffff");
    Poly1305.verifyMac(key, data, tag);
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.3. Poly1305 Message Authentication Code,
   * Test Vector #10
   */
  @Test
  public void testVector10() throws DecoderException, GeneralSecurityException {
    byte[] key = Hex.decodeHex(""
        + "01000000000000000400000000000000"
        + "00000000000000000000000000000000");
    byte[] data = Hex.decodeHex(""
        + "E33594D7505E43B90000000000000000"
        + "3394D7505E4379CD0100000000000000"
        + "00000000000000000000000000000000"
        + "01000000000000000000000000000000");
    byte[] tag = Poly1305.computeMac(key, data);
    assert Hex.encodeHexString(tag).equalsIgnoreCase("14000000000000005500000000000000");
    Poly1305.verifyMac(key, data, tag);
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.3. Poly1305 Message Authentication Code,
   * Test Vector #11
   */
  @Test
  public void testVector11() throws DecoderException, GeneralSecurityException {
    byte[] key = Hex.decodeHex(""
        + "01000000000000000400000000000000"
        + "00000000000000000000000000000000");
    byte[] data = Hex.decodeHex(""
        + "E33594D7505E43B90000000000000000"
        + "3394D7505E4379CD0100000000000000"
        + "00000000000000000000000000000000");
    byte[] tag = Poly1305.computeMac(key, data);
    assert Hex.encodeHexString(tag).equalsIgnoreCase("13000000000000000000000000000000");
    Poly1305.verifyMac(key, data, tag);
  }
}
