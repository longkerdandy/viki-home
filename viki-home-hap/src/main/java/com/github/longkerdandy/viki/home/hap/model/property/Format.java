package com.github.longkerdandy.viki.home.hap.model.property;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Format of the Characteristic's Value
 */
public enum Format {

  BOOL("bool"),
  UINT8("uint8"),
  UINT16("uint16"),
  UINT32("uint32"),
  UINT64("uint64"),
  INT("int"),
  FLOAT("float"),
  STRING("string"),     // encoded in UTF-8
  TLV8("tlv8"),         // Base64 encoded set of one or more TLV8's
  DATA("data");         // Base64 encoded data blob

  private final String value;

  Format(String value) {
    this.value = value;
  }

  public static Format fromValue(String value) {
    for (Format f : values()) {
      if (f.value.equals(value)) {
        return f;
      }
    }
    throw new IllegalArgumentException("invalid format: " + value);
  }

  @JsonValue
  public String value() {
    return this.value;
  }
}
