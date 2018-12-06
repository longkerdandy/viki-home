package com.github.longkerdandy.viki.home.hap.http.tlv;

/**
 * TLV Errors for {@link TLVType#ERROR}
 */
public enum TLVError {

  RESERVED(0),
  UNKNOWN(1),
  AUTHENTICATION(2),
  BACK_OFF(3),
  MAX_PEERS(4),
  MAX_TRIES(5),
  UNAVAILABLE(6),
  BUSY(7);

  private final int value;

  TLVError(int value) {
    this.value = value;
  }

  public static TLVError fromValue(int value) {
    for (TLVError e : values()) {
      if (e.value == value) {
        return e;
      }
    }
    return RESERVED; // ignore unrecognized type
  }

  public int value() {
    return this.value;
  }
}
