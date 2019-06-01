package com.github.longkerdandy.viki.home.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Data Type
 */
public enum DataType {

  INTEGER("integer"),                 // Long
  NUMBER("number"),                   // Double
  STRING("string"),                   // String
  BOOLEAN("boolean"),                 // Boolean
  DATETIME("datetime"),               // LocalDateTime
  ARRAY_INTEGER("integer_array"),     // long[]
  ARRAY_NUMBER("number_array"),       // double[]
  ARRAY_STRING("string_array"),       // String[]
  BLOB("blob");                       // byte[]

  private final String value;

  DataType(String value) {
    this.value = value;
  }

  public static DataType fromValue(String value) {
    for (DataType t : values()) {
      if (t.value.equals(value)) {
        return t;
      }
    }
    throw new IllegalArgumentException("invalid data type: " + value);
  }

  @JsonValue
  public String value() {
    return this.value;
  }
}
