package com.github.longkerdandy.viki.home.hap.model.property;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Permission string describes the capability of the Characteristic
 */
public enum Permission {

  PAIRED_READ("pr"),                // this characteristic can only be read by paired controllers
  PAIRED_WRITE("pw"),               // this characteristic can only be written by paired controllers
  NOTIFY("ev"),                     // this characteristic support events
  ADDITIONAL_AUTHORIZATION("aa"),   // this characteristic support additional authorization data
  TIMED_WRITE("tw"),                // this characteristic support timed write procedure
  HIDDEN("hd");                     // this characteristic is hidden from user


  private final String value;

  Permission(String value) {
    this.value = value;
  }

  public static Permission fromValue(String value) {
    for (Permission p : values()) {
      if (p.value.equals(value)) {
        return p;
      }
    }
    throw new IllegalArgumentException("invalid permission: " + value);
  }

  @JsonValue
  public String value() {
    return this.value;
  }
}
