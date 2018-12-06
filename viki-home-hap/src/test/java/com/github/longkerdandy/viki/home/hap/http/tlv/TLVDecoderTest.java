package com.github.longkerdandy.viki.home.hap.http.tlv;

import static io.netty.buffer.Unpooled.buffer;

import io.netty.buffer.ByteBuf;
import java.util.Map;
import org.junit.Test;

public class TLVDecoderTest {

  @Test
  public void decodeTest1() {
    ByteBuf buffer = buffer(10);
    buffer.writeByte((byte) 0x06);
    buffer.writeByte((byte) 0x01);
    buffer.writeByte((byte) 0x03);
    buffer.writeByte((byte) 0x01);
    buffer.writeByte((byte) 0x05);
    buffer.writeByte((byte) 0x68);
    buffer.writeByte((byte) 0x65);
    buffer.writeByte((byte) 0x6C);
    buffer.writeByte((byte) 0x6C);
    buffer.writeByte((byte) 0x6F);

    Map<TLVType, ?> tlvs = TLVDecoder.decode(buffer);
    assert tlvs != null;
    assert tlvs.size() == 2;
    assert tlvs.get(TLVType.STATE).equals(3);
    assert tlvs.get(TLVType.IDENTIFIER).equals("hello");
  }

  @Test
  public void decodeTest2() {
    ByteBuf buffer = buffer(314);
    buffer.writeByte((byte) 0x06);
    buffer.writeByte((byte) 0x01);
    buffer.writeByte((byte) 0x03);
    buffer.writeByte((byte) 0x09);
    buffer.writeByte((byte) 0xFF);
    for (int i = 1; i <= 255; i++) {
      buffer.writeByte((byte) 0x61);
    }
    buffer.writeByte((byte) 0x09);
    buffer.writeByte((byte) 0x2D);
    for (int i = 1; i <= 45; i++) {
      buffer.writeByte((byte) 0x61);
    }
    buffer.writeByte((byte) 0x01);
    buffer.writeByte((byte) 0x05);
    buffer.writeByte((byte) 0x68);
    buffer.writeByte((byte) 0x65);
    buffer.writeByte((byte) 0x6C);
    buffer.writeByte((byte) 0x6C);
    buffer.writeByte((byte) 0x6F);

    Map<TLVType, ?> tlvs = TLVDecoder.decode(buffer);
    assert tlvs != null;
    assert tlvs.size() == 3;
    assert tlvs.get(TLVType.STATE).equals(3);
    byte[] cert = (byte[]) tlvs.get(TLVType.CERTIFICATE);
    assert cert != null;
    assert cert.length == 255 + 45;
    for (int i = 0; i < 255 + 45; i++) {
      assert cert[i] == 0x61;
    }
    assert tlvs.get(TLVType.IDENTIFIER).equals("hello");
  }
}
