package com.github.longkerdandy.viki.home.hap.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

public class ChaCha20Test {

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols 2.3.2. Test Vector for the ChaCha20 Block
   * Function
   */
  @Test
  public void testVectorBlock0() throws DecoderException {
    byte[] key = Hex.decodeHex(""
        + "000102030405060708090a0b0c0d0e0f"
        + "101112131415161718191a1b1c1d1e1f");
    byte[] nonce = Hex.decodeHex("000000090000004a00000000");
    ChaCha20 chacha = new ChaCha20(key, 1);
    ByteBuffer out = chacha.getKeyStreamBlock(nonce, 1);
    assert Hex.encodeHexString(out).equalsIgnoreCase(""
        + "10f1e7e4d13b5915500fdd1fa32071c4"
        + "c7d1f4c733c068030422aa9ac3d46c4e"
        + "d2826446079faa0914c2d705d98b02a2"
        + "b5129cd1de164eb9cbd083e8a2503c4e");
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.1. The ChaCha20 Block Functions, Test
   * Vector #1
   */
  @Test
  public void testVectorBlock1() throws DecoderException {
    byte[] key = Hex.decodeHex(""
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000000");
    byte[] nonce = Hex.decodeHex("000000000000000000000000");
    ChaCha20 chacha = new ChaCha20(key, 1);
    ByteBuffer out = chacha.getKeyStreamBlock(nonce, 0);
    assert Hex.encodeHexString(out).equalsIgnoreCase(""
        + "76b8e0ada0f13d90405d6ae55386bd28"
        + "bdd219b8a08ded1aa836efcc8b770dc7"
        + "da41597c5157488d7724e03fb8d84a37"
        + "6a43b8f41518a11cc387b669b2ee6586");
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.1. The ChaCha20 Block Functions, Test
   * Vector #2
   */
  @Test
  public void testVectorBlock2() throws DecoderException {
    byte[] key = Hex.decodeHex(""
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000000");
    byte[] nonce = Hex.decodeHex("000000000000000000000000");
    ChaCha20 chacha = new ChaCha20(key, 1);
    ByteBuffer out = chacha.getKeyStreamBlock(nonce, 1);
    assert Hex.encodeHexString(out).equalsIgnoreCase(""
        + "9f07e7be5551387a98ba977c732d080d"
        + "cb0f29a048e3656912c6533e32ee7aed"
        + "29b721769ce64e43d57133b074d839d5"
        + "31ed1f28510afb45ace10a1f4b794d6f");
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.1. The ChaCha20 Block Functions, Test
   * Vector #3
   */
  @Test
  public void testVectorBlock3() throws DecoderException {
    byte[] key = Hex.decodeHex(""
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000001");
    byte[] nonce = Hex.decodeHex("000000000000000000000000");
    ChaCha20 chacha = new ChaCha20(key, 1);
    ByteBuffer out = chacha.getKeyStreamBlock(nonce, 1);
    assert Hex.encodeHexString(out).equalsIgnoreCase(""
        + "3aeb5224ecf849929b9d828db1ced4dd"
        + "832025e8018b8160b82284f3c949aa5a"
        + "8eca00bbb4a73bdad192b5c42f73f2fd"
        + "4e273644c8b36125a64addeb006c13a0");
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.1. The ChaCha20 Block Functions, Test
   * Vector #4
   */
  @Test
  public void testVectorBlock4() throws DecoderException {
    byte[] key = Hex.decodeHex(""
        + "00ff0000000000000000000000000000"
        + "00000000000000000000000000000000");
    byte[] nonce = Hex.decodeHex("000000000000000000000000");
    ChaCha20 chacha = new ChaCha20(key, 1);
    ByteBuffer out = chacha.getKeyStreamBlock(nonce, 2);
    assert Hex.encodeHexString(out).equalsIgnoreCase(""
        + "72d54dfbf12ec44b362692df94137f32"
        + "8fea8da73990265ec1bbbea1ae9af0ca"
        + "13b25aa26cb4a648cb9b9d1be65b2c09"
        + "24a66c54d545ec1b7374f4872e99f096");
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.1. The ChaCha20 Block Functions, Test
   * Vector #5
   */
  @Test
  public void testVectorBlock5() throws DecoderException {
    byte[] key = Hex.decodeHex(""
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000000");
    byte[] nonce = Hex.decodeHex("000000000000000000000002");
    ChaCha20 chacha = new ChaCha20(key, 1);
    ByteBuffer out = chacha.getKeyStreamBlock(nonce, 0);
    assert Hex.encodeHexString(out).equalsIgnoreCase(""
        + "c2c64d378cd536374ae204b9ef933fcd"
        + "1a8b2288b3dfa49672ab765b54ee27c7"
        + "8a970e0e955c14f3a88e741b97c286f7"
        + "5f8fc299e8148362fa198a39531bed6d");
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols 2.4.2. Example and Test Vector for the
   * ChaCha20 Cipher
   */
  @Test
  public void testVectorEncryption0() throws DecoderException {
    byte[] key = Hex.decodeHex(""
        + "000102030405060708090a0b0c0d0e0f"
        + "101112131415161718191a1b1c1d1e1f");
    byte[] nonce = Hex.decodeHex("000000000000004a00000000");
    byte[] plainText = ("Ladies and Gentlemen of the class of '99: "
        + "If I could offer you only one tip for the future, sunscreen would be it.")
        .getBytes(StandardCharsets.UTF_8);
    ChaCha20 chacha = new ChaCha20(key, 1);
    byte[] out = chacha.encrypt(plainText, nonce);
    assert Hex.encodeHexString(out).equalsIgnoreCase(""
        + "6e2e359a2568f98041ba0728dd0d6981"
        + "e97e7aec1d4360c20a27afccfd9fae0b"
        + "f91b65c5524733ab8f593dabcd62b357"
        + "1639d624e65152ab8f530c359f0861d8"
        + "07ca0dbf500d6a6156a38e088a22b65e"
        + "52bc514d16ccf806818ce91ab7793736"
        + "5af90bbf74a35be6b40b8eedf2785e42"
        + "874d");
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols 2.4.2. Example and Test Vector for the
   * ChaCha20 Cipher
   */
  @Test
  public void testVectorDecryption0() throws DecoderException {
    byte[] key = Hex.decodeHex(""
        + "000102030405060708090a0b0c0d0e0f"
        + "101112131415161718191a1b1c1d1e1f");
    byte[] nonce = Hex.decodeHex("000000000000004a00000000");
    byte[] cipherText = Hex.decodeHex(""
        + "6e2e359a2568f98041ba0728dd0d6981"
        + "e97e7aec1d4360c20a27afccfd9fae0b"
        + "f91b65c5524733ab8f593dabcd62b357"
        + "1639d624e65152ab8f530c359f0861d8"
        + "07ca0dbf500d6a6156a38e088a22b65e"
        + "52bc514d16ccf806818ce91ab7793736"
        + "5af90bbf74a35be6b40b8eedf2785e42"
        + "874d");
    ChaCha20 chacha = new ChaCha20(key, 1);
    byte[] out = chacha.decrypt(cipherText, nonce);
    assert new String(out, StandardCharsets.UTF_8)
        .equalsIgnoreCase("Ladies and Gentlemen of the class of '99: "
            + "If I could offer you only one tip for the future, sunscreen would be it.");
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.2. ChaCha20 Encryption, Test Vector #1
   */
  @Test
  public void testVectorEncryption1() throws DecoderException {
    byte[] key = Hex.decodeHex(""
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000000");
    byte[] nonce = Hex.decodeHex("000000000000000000000000");
    byte[] plainText = Hex.decodeHex(""
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000000");
    ChaCha20 chacha = new ChaCha20(key, 0);
    byte[] out = chacha.encrypt(plainText, nonce);
    assert Hex.encodeHexString(out).equalsIgnoreCase(""
        + "76b8e0ada0f13d90405d6ae55386bd28"
        + "bdd219b8a08ded1aa836efcc8b770dc7"
        + "da41597c5157488d7724e03fb8d84a37"
        + "6a43b8f41518a11cc387b669b2ee6586");
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.2. ChaCha20 Encryption, Test Vector #1
   */
  @Test
  public void testVectorDecryption1() throws DecoderException {
    byte[] key = Hex.decodeHex(""
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000000");
    byte[] nonce = Hex.decodeHex("000000000000000000000000");
    byte[] cipherText = Hex.decodeHex(""
        + "76b8e0ada0f13d90405d6ae55386bd28"
        + "bdd219b8a08ded1aa836efcc8b770dc7"
        + "da41597c5157488d7724e03fb8d84a37"
        + "6a43b8f41518a11cc387b669b2ee6586");
    ChaCha20 chacha = new ChaCha20(key, 0);
    byte[] out = chacha.decrypt(cipherText, nonce);
    assert Hex.encodeHexString(out).equalsIgnoreCase(""
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000000");
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.2. ChaCha20 Encryption, Test Vector #2
   */
  @Test
  public void testVectorEncryption2() throws DecoderException {
    byte[] key = Hex.decodeHex(""
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000001");
    byte[] nonce = Hex.decodeHex("000000000000000000000002");
    byte[] plainText = (""
        + "Any submission to the IETF intended by the Contributor for publication as all or"
        + " part of an IETF Internet-Draft or RFC and any statement made within the context"
        + " of an IETF activity is considered an \"IETF Contribution\". Such statements "
        + "include oral statements in IETF sessions, as well as written and electronic "
        + "communications made at any time or place, which are addressed to")
        .getBytes(StandardCharsets.UTF_8);
    ChaCha20 chacha = new ChaCha20(key, 1);
    byte[] out = chacha.encrypt(plainText, nonce);
    assert Hex.encodeHexString(out).equalsIgnoreCase(""
        + "a3fbf07df3fa2fde4f376ca23e827370"
        + "41605d9f4f4f57bd8cff2c1d4b7955ec"
        + "2a97948bd3722915c8f3d337f7d37005"
        + "0e9e96d647b7c39f56e031ca5eb6250d"
        + "4042e02785ececfa4b4bb5e8ead0440e"
        + "20b6e8db09d881a7c6132f420e527950"
        + "42bdfa7773d8a9051447b3291ce1411c"
        + "680465552aa6c405b7764d5e87bea85a"
        + "d00f8449ed8f72d0d662ab052691ca66"
        + "424bc86d2df80ea41f43abf937d3259d"
        + "c4b2d0dfb48a6c9139ddd7f76966e928"
        + "e635553ba76c5c879d7b35d49eb2e62b"
        + "0871cdac638939e25e8a1e0ef9d5280f"
        + "a8ca328b351c3c765989cbcf3daa8b6c"
        + "cc3aaf9f3979c92b3720fc88dc95ed84"
        + "a1be059c6499b9fda236e7e818b04b0b"
        + "c39c1e876b193bfe5569753f88128cc0"
        + "8aaa9b63d1a16f80ef2554d7189c411f"
        + "5869ca52c5b83fa36ff216b9c1d30062"
        + "bebcfd2dc5bce0911934fda79a86f6e6"
        + "98ced759c3ff9b6477338f3da4f9cd85"
        + "14ea9982ccafb341b2384dd902f3d1ab"
        + "7ac61dd29c6f21ba5b862f3730e37cfd"
        + "c4fd806c22f221");
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.2. ChaCha20 Encryption, Test Vector #2
   */
  @Test
  public void testVectorDecryption2() throws DecoderException {
    byte[] key = Hex.decodeHex(""
        + "00000000000000000000000000000000"
        + "00000000000000000000000000000001");
    byte[] nonce = Hex.decodeHex("000000000000000000000002");
    byte[] cipherText = Hex.decodeHex(""
        + "a3fbf07df3fa2fde4f376ca23e827370"
        + "41605d9f4f4f57bd8cff2c1d4b7955ec"
        + "2a97948bd3722915c8f3d337f7d37005"
        + "0e9e96d647b7c39f56e031ca5eb6250d"
        + "4042e02785ececfa4b4bb5e8ead0440e"
        + "20b6e8db09d881a7c6132f420e527950"
        + "42bdfa7773d8a9051447b3291ce1411c"
        + "680465552aa6c405b7764d5e87bea85a"
        + "d00f8449ed8f72d0d662ab052691ca66"
        + "424bc86d2df80ea41f43abf937d3259d"
        + "c4b2d0dfb48a6c9139ddd7f76966e928"
        + "e635553ba76c5c879d7b35d49eb2e62b"
        + "0871cdac638939e25e8a1e0ef9d5280f"
        + "a8ca328b351c3c765989cbcf3daa8b6c"
        + "cc3aaf9f3979c92b3720fc88dc95ed84"
        + "a1be059c6499b9fda236e7e818b04b0b"
        + "c39c1e876b193bfe5569753f88128cc0"
        + "8aaa9b63d1a16f80ef2554d7189c411f"
        + "5869ca52c5b83fa36ff216b9c1d30062"
        + "bebcfd2dc5bce0911934fda79a86f6e6"
        + "98ced759c3ff9b6477338f3da4f9cd85"
        + "14ea9982ccafb341b2384dd902f3d1ab"
        + "7ac61dd29c6f21ba5b862f3730e37cfd"
        + "c4fd806c22f221");
    ChaCha20 chacha = new ChaCha20(key, 1);
    byte[] out = chacha.decrypt(cipherText, nonce);
    assert new String(out, StandardCharsets.UTF_8).equalsIgnoreCase(""
        + "Any submission to the IETF intended by the Contributor for publication as all or"
        + " part of an IETF Internet-Draft or RFC and any statement made within the context"
        + " of an IETF activity is considered an \"IETF Contribution\". Such statements "
        + "include oral statements in IETF sessions, as well as written and electronic "
        + "communications made at any time or place, which are addressed to");
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.2. ChaCha20 Encryption, Test Vector #3
   */
  @Test
  public void testVectorEncryption3() throws DecoderException {
    byte[] key = Hex.decodeHex(""
        + "1c9240a5eb55d38af333888604f6b5f0"
        + "473917c1402b80099dca5cbc207075c0");
    byte[] nonce = Hex.decodeHex("000000000000000000000002");
    byte[] plainText = (""
        + "'Twas brillig, and the slithy toves\n"
        + "Did gyre and gimble in the wabe:\n"
        + "All mimsy were the borogoves,\n"
        + "And the mome raths outgrabe.")
        .getBytes(StandardCharsets.UTF_8);
    ChaCha20 chacha = new ChaCha20(key, 42);
    byte[] out = chacha.encrypt(plainText, nonce);
    assert Hex.encodeHexString(out).equalsIgnoreCase(""
        + "62e6347f95ed87a45ffae7426f27a1df"
        + "5fb69110044c0d73118effa95b01e5cf"
        + "166d3df2d721caf9b21e5fb14c616871"
        + "fd84c54f9d65b283196c7fe4f60553eb"
        + "f39c6402c42234e32a356b3e764312a6"
        + "1a5532055716ead6962568f87d3f3f77"
        + "04c6a8d1bcd1bf4d50d6154b6da731b1"
        + "87b58dfd728afa36757a797ac188d1");
  }

  /**
   * RFC 7539 - ChaCha20 and Poly1305 for IETF Protocols A.2. ChaCha20 Encryption, Test Vector #3
   */
  @Test
  public void testVectorDecryption3() throws DecoderException {
    byte[] key = Hex.decodeHex(""
        + "1c9240a5eb55d38af333888604f6b5f0"
        + "473917c1402b80099dca5cbc207075c0");
    byte[] nonce = Hex.decodeHex("000000000000000000000002");
    byte[] cipherText = Hex.decodeHex(""
        + "62e6347f95ed87a45ffae7426f27a1df"
        + "5fb69110044c0d73118effa95b01e5cf"
        + "166d3df2d721caf9b21e5fb14c616871"
        + "fd84c54f9d65b283196c7fe4f60553eb"
        + "f39c6402c42234e32a356b3e764312a6"
        + "1a5532055716ead6962568f87d3f3f77"
        + "04c6a8d1bcd1bf4d50d6154b6da731b1"
        + "87b58dfd728afa36757a797ac188d1");
    ChaCha20 chacha = new ChaCha20(key, 42);
    byte[] out = chacha.decrypt(cipherText, nonce);
    assert new String(out, StandardCharsets.UTF_8).equalsIgnoreCase(""
        + "'Twas brillig, and the slithy toves\n"
        + "Did gyre and gimble in the wabe:\n"
        + "All mimsy were the borogoves,\n"
        + "And the mome raths outgrabe.");
  }
}
