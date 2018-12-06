package com.github.longkerdandy.viki.home.hap.model;

import java.util.Arrays;

/**
 * HomeKit Accessory Protocol Pairing
 */
public class Pairing {

  private String paringId;  // UTF-8 identifier
  private byte[] publicKey; // signed Ed25519 key
  private int permissions;  // (0x00) : Regular user; (0x01) : Admin

  private Pairing() {
  }

  public Pairing(String paringId, byte[] publicKey, int permissions) {
    this.paringId = paringId;
    this.publicKey = publicKey;
    this.permissions = permissions;
  }

  public String getParingId() {
    return paringId;
  }

  public void setParingId(String paringId) {
    this.paringId = paringId;
  }

  public byte[] getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(byte[] publicKey) {
    this.publicKey = publicKey;
  }

  public int getPermissions() {
    return permissions;
  }

  public void setPermissions(int permissions) {
    this.permissions = permissions;
  }

  @Override
  public String toString() {
    return "Pairing{" +
        "paringId='" + paringId + '\'' +
        ", publicKey=" + Arrays.toString(publicKey) +
        ", permissions=" + permissions +
        '}';
  }
}
