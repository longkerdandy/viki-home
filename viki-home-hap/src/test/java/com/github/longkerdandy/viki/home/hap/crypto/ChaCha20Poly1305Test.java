package com.github.longkerdandy.viki.home.hap.crypto;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

public class ChaCha20Poly1305Test {

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols 2.8.2. Example and Test Vector for
   * AEAD_CHACHA20_POLY1305
   */
  @Test
  public void testVectorEncryption0() throws DecoderException {
    byte[] key = Hex.decodeHex(""
        + "808182838485868788898a8b8c8d8e8f"
        + "909192939495969798999a9b9c9d9e9f");
    byte[] nonce = Hex.decodeHex("070000004041424344454647");
    byte[] aad = Hex.decodeHex("50515253c0c1c2c3c4c5c6c7");
    byte[] plainText = ("Ladies and Gentlemen of the class of '99: "
        + "If I could offer you only one tip for the future, sunscreen would be it.")
        .getBytes(StandardCharsets.UTF_8);
    ChaCha20Poly1305 cp = new ChaCha20Poly1305(key);
    byte[] out = cp.encrypt(plainText, nonce, aad);
    assert out.length == plainText.length + 16;
    assert Hex.encodeHexString(out).equalsIgnoreCase(""
        + "d31a8d34648e60db7b86afbc53ef7ec2"    // actual ciphertext
        + "a4aded51296e08fea9e2b5a736ee62d6"
        + "3dbea45e8ca9671282fafb69da92728b"
        + "1a71de0a9e060b2905d6a5b67ecd3b36"
        + "92ddbd7f2d778b8c9803aee328091b58"
        + "fab324e4fad675945585808b4831d7bc"
        + "3ff4def08e4b7a9de576d26586cec64b"
        + "6116"
        + "1ae10b594f09e26a7e902ecbd0600691");  // tag
    assert Hex.encodeHexString(key).equalsIgnoreCase(""
        + "808182838485868788898a8b8c8d8e8f"
        + "909192939495969798999a9b9c9d9e9f");
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols 2.8.2. Example and Test Vector for
   * AEAD_CHACHA20_POLY1305
   */
  @Test
  public void testVectorDecryption0() throws DecoderException, GeneralSecurityException {
    byte[] key = Hex.decodeHex(""
        + "808182838485868788898a8b8c8d8e8f"
        + "909192939495969798999a9b9c9d9e9f");
    byte[] nonce = Hex.decodeHex("070000004041424344454647");
    byte[] aad = Hex.decodeHex("50515253c0c1c2c3c4c5c6c7");
    byte[] cipherText = Hex.decodeHex(""
        + "d31a8d34648e60db7b86afbc53ef7ec2"    // actual ciphertext
        + "a4aded51296e08fea9e2b5a736ee62d6"
        + "3dbea45e8ca9671282fafb69da92728b"
        + "1a71de0a9e060b2905d6a5b67ecd3b36"
        + "92ddbd7f2d778b8c9803aee328091b58"
        + "fab324e4fad675945585808b4831d7bc"
        + "3ff4def08e4b7a9de576d26586cec64b"
        + "6116"
        + "1ae10b594f09e26a7e902ecbd0600691");  // tag
    ChaCha20Poly1305 cp = new ChaCha20Poly1305(key);
    byte[] out = cp.decrypt(cipherText, nonce, aad);
    assert out.length == cipherText.length - 16;
    assert new String(out, StandardCharsets.UTF_8).equalsIgnoreCase(""
        + "Ladies and Gentlemen of the class of '99: "
        + "If I could offer you only one tip for the future, sunscreen would be it.");
    assert Hex.encodeHexString(key).equalsIgnoreCase(""
        + "808182838485868788898a8b8c8d8e8f"
        + "909192939495969798999a9b9c9d9e9f");
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.5. ChaCha20-Poly1305 AEAD Decryption
   */
  @Test
  public void testVectorEncryption1() throws DecoderException {
    byte[] key = Hex.decodeHex(""
        + "1c9240a5eb55d38af333888604f6b5f0"
        + "473917c1402b80099dca5cbc207075c0");
    byte[] nonce = Hex.decodeHex("000000000102030405060708");
    byte[] aad = Hex.decodeHex("f33388860000000000004e91");
    byte[] plainText = (""
        + "Internet-Drafts are draft documents valid for a maximum of six months and may be"
        + " updated, replaced, or obsoleted by other documents at any time. It is inappropriate "
        + "to use Internet-Drafts as reference material or to cite them other than as /“work in progress./”")
        .getBytes(StandardCharsets.UTF_8);
    ChaCha20Poly1305 cp = new ChaCha20Poly1305(key);
    byte[] out = cp.encrypt(plainText, nonce, aad);
    assert out.length == plainText.length + 16;
    assert Hex.encodeHexString(out).equalsIgnoreCase(""
        + "64a0861575861af460f062c79be643bd"    // actual ciphertext
        + "5e805cfd345cf389f108670ac76c8cb2"
        + "4c6cfc18755d43eea09ee94e382d26b0"
        + "bdb7b73c321b0100d4f03b7f355894cf"
        + "332f830e710b97ce98c8a84abd0b9481"
        + "14ad176e008d33bd60f982b1ff37c855"
        + "9797a06ef4f0ef61c186324e2b350638"
        + "3606907b6a7c02b0f9f6157b53c867e4"
        + "b9166c767b804d46a59b5216cde7a4e9"
        + "9040c5a40433225ee282a1b0a06c523e"
        + "af4534d7f83fa1155b0047718cbc546a"
        + "0d072b04b3564eea1b422273f548271a"
        + "0bb2316053fa76991955ebd63159434e"
        + "cebb4e466dae5a1073a6727627097a10"
        + "49e617d91d361094fa68f0ff77987130"
        + "305beaba2eda04df997b714d6c6f2c29"
        + "a6ad5cb4022b02709b"
        + "eead9d67890cbb22392336fea1851f38");  // tag
    assert Hex.encodeHexString(key).equalsIgnoreCase(""
        + "1c9240a5eb55d38af333888604f6b5f0"
        + "473917c1402b80099dca5cbc207075c0");
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.5. ChaCha20-Poly1305 AEAD Decryption
   */
  @Test
  public void testVectorDecryption1() throws DecoderException, GeneralSecurityException {
    byte[] key = Hex.decodeHex(""
        + "1c9240a5eb55d38af333888604f6b5f0"
        + "473917c1402b80099dca5cbc207075c0");
    byte[] nonce = Hex.decodeHex("000000000102030405060708");
    byte[] aad = Hex.decodeHex("f33388860000000000004e91");
    byte[] cipherText = Hex.decodeHex(""
        + "64a0861575861af460f062c79be643bd"    // actual ciphertext
        + "5e805cfd345cf389f108670ac76c8cb2"
        + "4c6cfc18755d43eea09ee94e382d26b0"
        + "bdb7b73c321b0100d4f03b7f355894cf"
        + "332f830e710b97ce98c8a84abd0b9481"
        + "14ad176e008d33bd60f982b1ff37c855"
        + "9797a06ef4f0ef61c186324e2b350638"
        + "3606907b6a7c02b0f9f6157b53c867e4"
        + "b9166c767b804d46a59b5216cde7a4e9"
        + "9040c5a40433225ee282a1b0a06c523e"
        + "af4534d7f83fa1155b0047718cbc546a"
        + "0d072b04b3564eea1b422273f548271a"
        + "0bb2316053fa76991955ebd63159434e"
        + "cebb4e466dae5a1073a6727627097a10"
        + "49e617d91d361094fa68f0ff77987130"
        + "305beaba2eda04df997b714d6c6f2c29"
        + "a6ad5cb4022b02709b"
        + "eead9d67890cbb22392336fea1851f38");  // tag
    ChaCha20Poly1305 cp = new ChaCha20Poly1305(key);
    byte[] out = cp.decrypt(cipherText, nonce, aad);
    assert out.length == cipherText.length - 16;
    assert new String(out, StandardCharsets.UTF_8).equalsIgnoreCase(""
        + "Internet-Drafts are draft documents valid for a maximum of six months and may be"
        + " updated, replaced, or obsoleted by other documents at any time. It is inappropriate "
        + "to use Internet-Drafts as reference material or to cite them other than as /“work in progress./”");
    assert Hex.encodeHexString(key).equalsIgnoreCase(""
        + "1c9240a5eb55d38af333888604f6b5f0"
        + "473917c1402b80099dca5cbc207075c0");
  }
}
