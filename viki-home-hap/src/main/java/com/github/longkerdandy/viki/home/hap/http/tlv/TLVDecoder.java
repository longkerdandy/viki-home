package com.github.longkerdandy.viki.home.hap.http.tlv;

import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Decoder for HomeKit Protocol's TLV Format
 */
public class TLVDecoder {

  /**
   * Decode TLV data format, from {@link ByteBuf} to {@link Map}
   *
   * There may be multiple TLV items of the same type, in this case the last one will be returned.
   *
   * @param buffer {@link ByteBuf}
   * @return {@link Map} which K: {@link TLVType}, V: Value (byte[] int or String)
   */
  public static Map<TLVType, ?> decode(ByteBuf buffer) {
    // Result as map, which value is either byte[] int or String
    Map<TLVType, Object> result = new HashMap<>();

    // Concat value larger than 255 bytes
    TLVType lastType = null;
    int lastLength = 0;
    // Loop and parse each fragment
    while (buffer.readableBytes() >= 2) {
      TLVType type = TLVType.fromValue(Byte.toUnsignedInt(buffer.readByte())); // type
      int length = Byte.toUnsignedInt(buffer.readByte());                           // length
      if (buffer.readableBytes() >= length) {
        switch (type) {
          // Integer types which value is actually byte
          case METHOD:
          case STATE:
          case ERROR:
          case RETRY_DELAY:
          case PERMISSIONS:
            if (length == 1) {
              if (type == TLVType.METHOD) {
                result.put(type, TLVMethod.fromValue(Byte.toUnsignedInt(buffer.readByte())));
              } else if (type == TLVType.ERROR) {
                result.put(type, TLVError.fromValue(Byte.toUnsignedInt(buffer.readByte())));
              } else {
                result.put(type, Byte.toUnsignedInt(buffer.readByte()));
              }
            } else {
              buffer.skipBytes(length);
            }
            break;
          // String type
          case IDENTIFIER:
            result.put(type, buffer.readCharSequence(length, StandardCharsets.UTF_8).toString());
            break;
          // Bytes types, concat if needed
          case SALT:
          case PUBLIC_KEY:
          case PROOF:
          case ENCRYPTED_DATA:
          case CERTIFICATE:
          case SIGNATURE:
          case FRAGMENT_DATA:
          case FRAGMENT_LAST:
            byte[] value = new byte[length];
            buffer.readBytes(value, 0, length);
            if (lastType == type && lastLength == 255) {
              byte[] lastValue = (byte[]) result.get(lastType);
              result.put(type, ArrayUtils.addAll(lastValue, value));
            } else {
              result.put(type, value);
            }
            break;
          // Separator type has no value
          case SEPARATOR:
            result.put(type, null);
            break;
        }

        // Update last type and length
        lastType = type;
        lastLength = length;
      } else {
        buffer.skipBytes(buffer.readableBytes());
      }
    }

    return result;
  }
}
