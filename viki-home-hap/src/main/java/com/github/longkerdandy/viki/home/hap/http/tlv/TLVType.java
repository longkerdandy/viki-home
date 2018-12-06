package com.github.longkerdandy.viki.home.hap.http.tlv;

/**
 * TLV Types
 */
public enum TLVType {

  METHOD(0x00),
  IDENTIFIER(0x01),
  SALT(0x02),
  PUBLIC_KEY(0x03),
  PROOF(0x04),
  ENCRYPTED_DATA(0x05),
  STATE(0x06),
  ERROR(0x07),
  RETRY_DELAY(0x08),
  CERTIFICATE(0x09),
  SIGNATURE(0x0A),
  PERMISSIONS(0x0B),
  FRAGMENT_DATA(0x0C),
  FRAGMENT_LAST(0x0D),
  SEPARATOR(0xFF);

  private final int value;

  TLVType(int value) {
    this.value = value;
  }

  public static TLVType fromValue(int value) {
    for (TLVType t : values()) {
      if (t.value == value) {
        return t;
      }
    }
    return SEPARATOR; // ignore unrecognized type
  }

  public int value() {
    return this.value;
  }
}
