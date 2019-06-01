package com.github.longkerdandy.viki.home.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Write Behavior Result
 */
public enum WriteResult {

  SUCCESS("success"),
  NOT_EXIST("not_exist"),
  NOT_CONFIGURED("not_configured"),
  NOT_SUPPORTED("not_supported"),
  INTERNAL_ERROR("internal_error");

  private final String value;

  WriteResult(String value) {
    this.value = value;
  }

  public static WriteResult fromValue(String value) {
    for (WriteResult r : values()) {
      if (r.value.equals(value)) {
        return r;
      }
    }
    throw new IllegalArgumentException("invalid write result: " + value);
  }

  @JsonValue
  public String value() {
    return this.value;
  }
}
