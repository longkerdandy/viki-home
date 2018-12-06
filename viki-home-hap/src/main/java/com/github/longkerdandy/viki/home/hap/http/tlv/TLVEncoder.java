package com.github.longkerdandy.viki.home.hap.http.tlv;

import static io.netty.buffer.Unpooled.buffer;

import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encoder for HomeKit Protocol's TLV Format
 */
public class TLVEncoder {

  /**
   * Encode TLV data format, from {@link Map} to {@link ByteBuf}
   *
   * @param tlvs {@link LinkedHashMap} which K: {@link TLVType}, V: Value (byte[] int or String)
   * @return {@link ByteBuf}
   */
  public static ByteBuf encode(LinkedHashMap<TLVType, ?> tlvs) {
    // Calculate TLV length
    int length = 0;
    for (Map.Entry<TLVType, ?> entry : tlvs.entrySet()) {
      if (entry.getValue() instanceof Integer) {
        // Integer type which value is actually byte
        length = length + 3;
      } else if (entry.getValue() instanceof String) {
        // String type
        length = length + 2 + ((String) entry.getValue()).getBytes(StandardCharsets.UTF_8).length;
      } else if (entry.getValue() instanceof byte[]) {
        // Bytes type
        byte[] bytes = (byte[]) entry.getValue();
        length = length + ((Double) Math.ceil((double) bytes.length / 255)).intValue() * 2
            + bytes.length;
      } else if (entry.getValue() instanceof TLVMethod || entry.getValue() instanceof TLVError) {
        // TLVMethod or TLVError
        length = length + 3;
      } else {
        // Null value
        length = length + 2;
      }
    }

    // Allocate buffer
    ByteBuf buffer = buffer(length);

    // Write TLVs to the buffer
    for (Map.Entry<TLVType, ?> entry : tlvs.entrySet()) {
      TLVType type = entry.getKey();
      switch (type) {
        // Integer types which value is actually byte
        case METHOD:
        case STATE:
        case ERROR:
        case RETRY_DELAY:
        case PERMISSIONS:
          buffer.writeByte((byte) type.value());
          buffer.writeByte((byte) 1);
          if (type == TLVType.METHOD) {
            buffer.writeByte(Integer.valueOf(((TLVMethod) entry.getValue()).value()).byteValue());
          } else if (type == TLVType.ERROR) {
            buffer.writeByte(Integer.valueOf(((TLVError) entry.getValue()).value()).byteValue());
          } else {
            buffer.writeByte(((Integer) entry.getValue()).byteValue());
          }
          break;
        // String type
        case IDENTIFIER:
          buffer.writeByte((byte) type.value());
          if (entry.getValue() != null) {
            byte[] bytes = ((String) entry.getValue()).getBytes(StandardCharsets.UTF_8);
            buffer.writeByte((byte) bytes.length);
            buffer.writeBytes(bytes);
          } else {
            buffer.writeByte((byte) 0);
          }
          break;
        // Bytes types, slice if needed
        case SALT:
        case PUBLIC_KEY:
        case PROOF:
        case ENCRYPTED_DATA:
        case CERTIFICATE:
        case SIGNATURE:
        case FRAGMENT_DATA:
        case FRAGMENT_LAST:
          if (entry.getValue() != null) {
            byte[] bytes = ((byte[]) entry.getValue());
            for (int idx = 0; bytes.length - idx > 0; idx = idx + 255) {
              int l = bytes.length - idx > 255 ? 255 : bytes.length - idx;
              buffer.writeByte((byte) type.value());
              buffer.writeByte((byte) l);
              buffer.writeBytes(bytes, idx, l);
            }
          } else {
            buffer.writeByte((byte) type.value());
            buffer.writeByte((byte) 0);
          }
          break;
        // Separator type has no value
        case SEPARATOR:
          buffer.writeByte((byte) type.value());
          buffer.writeByte((byte) 0);
          break;
      }
    }

    // Finished writing and return
    return buffer;
  }
}
