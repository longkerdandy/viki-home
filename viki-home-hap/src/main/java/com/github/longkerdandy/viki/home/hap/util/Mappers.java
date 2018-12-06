package com.github.longkerdandy.viki.home.hap.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Jdbi Mapper Util
 */
public final class Mappers {

  private Mappers() {
  }

  public static Boolean toBoolean(Integer value) {
    if (value != null) {
      return value != 0;
    } else {
      return null;
    }
  }

  public static Boolean toBoolean(String value) {
    if (value != null) {
      return Boolean.parseBoolean(value);
    } else {
      return null;
    }
  }

  public static Integer toInteger(String value) {
    if (value != null) {
      return Integer.parseInt(value);
    } else {
      return null;
    }
  }

  public static Long toLong(String value) {
    if (value != null) {
      return Long.parseLong(value);
    } else {
      return null;
    }
  }

  public static Double toDouble(String value) {
    if (value != null) {
      return Double.parseDouble(value);
    } else {
      return null;
    }
  }

  public static String[] toArray(String value) {
    if (value != null) {
      return value.split(",");
    } else {
      return null;
    }
  }

  public static List<Integer> toIntegerList(String value) {
    if (value != null) {
      return Arrays.stream(toArray(value)).map(Integer::parseInt).collect(Collectors.toList());
    } else {
      return null;
    }
  }

  public static List<Long> toLongList(String value) {
    if (value != null) {
      return Arrays.stream(toArray(value)).map(Long::parseLong).collect(Collectors.toList());
    } else {
      return null;
    }
  }

  public static List<Double> toDoubleList(String value) {
    if (value != null) {
      return Arrays.stream(toArray(value)).map(Double::parseDouble).collect(Collectors.toList());
    } else {
      return null;
    }
  }
}
