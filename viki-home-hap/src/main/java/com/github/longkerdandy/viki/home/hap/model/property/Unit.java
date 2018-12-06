package com.github.longkerdandy.viki.home.hap.model.property;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Unit of the Characteristic's Value
 */
public enum Unit {

  CELSIUS("celsius"),
  PERCENTAGE("percentage"),
  ARCDEGREES("arcdegrees"),
  LUX("lux"),
  SECONDS("seconds");

  private final String value;

  Unit(String value) {
    this.value = value;
  }

  public static Unit fromValue(String value) {
    for (Unit u : values()) {
      if (u.value.equals(value)) {
        return u;
      }
    }
    throw new IllegalArgumentException("invalid unit: " + value);
  }

  @JsonValue
  public String value() {
    return this.value;
  }
}
