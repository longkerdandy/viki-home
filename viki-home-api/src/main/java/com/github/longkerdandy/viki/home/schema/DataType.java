package com.github.longkerdandy.viki.home.schema;

/**
 * Data Type
 */
public enum DataType {

  INTEGER,  // Long
  NUMBER,   // Double
  STRING,   // String
  BOOLEAN,  // Boolean
  DATETIME, // LocalDateTime
  ARRAY,    // DataSchema<T>[]
  BLOB,     // byte[]
  Object    // DataSchema[]
}
