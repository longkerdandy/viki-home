package com.github.longkerdandy.viki.home.hap.http.response;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * HAP Status Code
 */
public enum Status {

  SUCCESS(0),
  INSUFFICIENT_PRIVILEGES(-70401),
  COMMUNICATION_FAIL(-70402),
  RESOURCE_BUSY(-70403),
  CHARACTERISTIC_READ_ONLY(-70404),
  CHARACTERISTIC_WRITE_ONLY(-70405),
  NOTIFICATION_NOT_SUPPORTED(-70406),
  OUT_OF_RESOURCES(-70407),
  OPERATION_TIMED_OUT(-70408),
  RESOURCE_NOT_EXIST(-70409),
  INVALID_VALUE(-70410),
  INSUFFICIENT_AUTHORIZATION(-70411);

  private final int value;

  Status(int value) {
    this.value = value;
  }

  public static Status fromValue(int value) {
    for (Status s : values()) {
      if (s.value == value) {
        return s;
      }
    }
    throw new IllegalArgumentException("invalid status code: " + value);
  }

  @JsonValue
  public int value() {
    return this.value;
  }
}
