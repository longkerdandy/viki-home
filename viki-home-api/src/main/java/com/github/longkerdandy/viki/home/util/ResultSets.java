package com.github.longkerdandy.viki.home.util;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ResultSet Util
 */
public class ResultSets {

  private ResultSets() {
  }

  /**
   * Get Byte from ResultSet
   */
  public static Byte getByte(ResultSet rs, String columnLabel) throws SQLException {
    byte b = rs.getByte(columnLabel);
    if (rs.wasNull()) {
      return null;
    } else {
      return b;
    }
  }

  /**
   * Get Short from ResultSet
   */
  public static Short getShort(ResultSet rs, String columnLabel) throws SQLException {
    short s = rs.getShort(columnLabel);
    if (rs.wasNull()) {
      return null;
    } else {
      return s;
    }
  }

  /**
   * Get Integer from ResultSet
   */
  public static Integer getInt(ResultSet rs, String columnLabel) throws SQLException {
    int i = rs.getInt(columnLabel);
    if (rs.wasNull()) {
      return null;
    } else {
      return i;
    }
  }

  /**
   * Get Long from ResultSet
   */
  public static Long getLong(ResultSet rs, String columnLabel) throws SQLException {
    long l = rs.getLong(columnLabel);
    if (rs.wasNull()) {
      return null;
    } else {
      return l;
    }
  }

  /**
   * Get Float from ResultSet
   */
  public static Float getFloat(ResultSet rs, String columnLabel) throws SQLException {
    float f = rs.getFloat(columnLabel);
    if (rs.wasNull()) {
      return null;
    } else {
      return f;
    }
  }

  /**
   * Get Double from ResultSet
   */
  public static Double getDouble(ResultSet rs, String columnLabel) throws SQLException {
    double d = rs.getDouble(columnLabel);
    if (rs.wasNull()) {
      return null;
    } else {
      return d;
    }
  }

  /**
   * Get String from ResultSet
   */
  public static String getString(ResultSet rs, String columnLabel) throws SQLException {
    return rs.getString(columnLabel);
  }

  /**
   * Get Blob from ResultSet
   */
  public static Blob getBlob(ResultSet rs, String columnLabel) throws SQLException {
    return rs.getBlob(columnLabel);
  }
}
