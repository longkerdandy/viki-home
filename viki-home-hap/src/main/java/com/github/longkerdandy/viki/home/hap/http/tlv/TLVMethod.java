package com.github.longkerdandy.viki.home.hap.http.tlv;

/**
 * TLV Methods for {@link TLVType#METHOD}
 */
public enum TLVMethod {

  RESERVED(0),
  PAIR_SETUP(1),
  PAIR_VERIFY(2),
  ADD_PAIRING(3),
  REMOVE_PAIRING(4),
  LIST_PAIRINGS(5);

  private final int value;

  TLVMethod(int value) {
    this.value = value;
  }

  public static TLVMethod fromValue(int value) {
    for (TLVMethod m : values()) {
      if (m.value == value) {
        return m;
      }
    }
    return RESERVED; // ignore unrecognized type
  }

  public int value() {
    return this.value;
  }
}
