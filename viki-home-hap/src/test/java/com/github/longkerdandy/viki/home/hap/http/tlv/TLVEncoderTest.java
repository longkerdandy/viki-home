package com.github.longkerdandy.viki.home.hap.http.tlv;

import io.netty.buffer.ByteBuf;
import java.util.LinkedHashMap;
import org.junit.Test;

public class TLVEncoderTest {

  @Test
  public void encodeTest1() {
    LinkedHashMap<TLVType, Object> tlvs = new LinkedHashMap<>();
    tlvs.put(TLVType.STATE, 3);
    tlvs.put(TLVType.IDENTIFIER, "hello");

    ByteBuf buffer = TLVEncoder.encode(tlvs);
    assert buffer.readByte() == 0x06;
    assert buffer.readByte() == 0x01;
    assert buffer.readByte() == 0x03;
    assert buffer.readByte() == 0x01;
    assert buffer.readByte() == 0x05;
    assert buffer.readByte() == 0x68;
    assert buffer.readByte() == 0x65;
    assert buffer.readByte() == 0x6C;
    assert buffer.readByte() == 0x6C;
    assert buffer.readByte() == 0x6F;
  }

  @Test
  public void encodeTest2() {
    LinkedHashMap<TLVType, Object> tlvs = new LinkedHashMap<>();
    byte[] bytes = new byte[300];
    for (int i = 0; i < 255 + 45; i++) {
      bytes[i] = 0x61;
    }
    tlvs.put(TLVType.STATE, 3);
    tlvs.put(TLVType.CERTIFICATE, bytes);
    tlvs.put(TLVType.IDENTIFIER, "hello");

    ByteBuf buffer = TLVEncoder.encode(tlvs);
    assert buffer.readByte() == 0x06;
    assert buffer.readByte() == 0x01;
    assert buffer.readByte() == 0x03;
    assert buffer.readByte() == 0x09;
    assert Byte.toUnsignedInt(buffer.readByte()) == 0xFF;
    for (int i = 1; i <= 255; i++) {
      assert buffer.readByte() == 0x61;
    }
    assert buffer.readByte() == 0x09;
    assert buffer.readByte() == 0x2D;
    for (int i = 1; i <= 45; i++) {
      assert buffer.readByte() == 0x61;
    }
    assert buffer.readByte() == 0x01;
    assert buffer.readByte() == 0x05;
    assert buffer.readByte() == 0x68;
    assert buffer.readByte() == 0x65;
    assert buffer.readByte() == 0x6C;
    assert buffer.readByte() == 0x6C;
    assert buffer.readByte() == 0x6F;
  }
}
