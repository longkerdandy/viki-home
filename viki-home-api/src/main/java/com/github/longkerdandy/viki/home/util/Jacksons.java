package com.github.longkerdandy.viki.home.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;

/**
 * Jackson (JSON) Util
 */
public class Jacksons {

  private Jacksons() {
  }

  /**
   * Get the singleton of pre-configured ObjectMapper
   */
  @SuppressWarnings("unused")
  public static ObjectMapper getMapper() {
    return SingletonHelper.MAPPER;
  }

  /**
   * Get the singleton of pre-configured ObjectWriter
   */
  public static ObjectWriter getWriter() {
    return SingletonHelper.MAPPER.writer();
  }

  /**
   * Get the singleton of pre-configured ObjectReader
   */
  public static ObjectReader getReader(JavaType type) {
    return SingletonHelper.MAPPER.readerFor(type);
  }

  /**
   * Get the singleton of pre-configured ObjectReader
   */
  public static ObjectReader getReader(Class<?> type) {
    return SingletonHelper.MAPPER.readerFor(type);
  }

  /**
   * Get the singleton of pre-configured ObjectReader
   */
  public static ObjectReader getReader(TypeReference<?> type) {
    return SingletonHelper.MAPPER.readerFor(type);
  }

  /**
   * Check the String field from {@link JsonNode}
   *
   * @param node {@link JsonNode}
   * @param fieldName Field Name
   * @return String value of the field
   * @throws IOException if field is missing or null
   */
  public static String checkString(JsonNode node, String fieldName) throws IOException {
    JsonNode subNode = node.path(fieldName);
    if (subNode.isNull() || subNode.isMissingNode()) {
      throw new IOException("JsonNode missing " + fieldName + " field");
    }
    return subNode.asText();
  }

  /**
   * Check the Integer field from {@link JsonNode}
   *
   * @param node {@link JsonNode}
   * @param fieldName Field Name
   * @return Integer value of the field
   * @throws IOException if field is missing or null
   */
  public static int checkInteger(JsonNode node, String fieldName) throws IOException {
    JsonNode subNode = node.path(fieldName);
    if (subNode.isNull() || subNode.isMissingNode()) {
      throw new IOException("JsonNode missing " + fieldName + " field");
    }
    return subNode.asInt();
  }

  /**
   * Get the Boolean field from {@link JsonNode}
   *
   * @param node {@link JsonNode}
   * @param fieldName Field Name
   * @return Boolean value of the field or null
   */
  public static Boolean getBoolean(JsonNode node, String fieldName) {
    JsonNode subNode = node.path(fieldName);
    if (subNode.isNull() || subNode.isMissingNode()) {
      return null;
    }
    return subNode.asBoolean();
  }

  /**
   * Get the String field from {@link JsonNode}
   *
   * @param node {@link JsonNode}
   * @param fieldName Field Name
   * @return String value of the field or null
   */
  public static String getString(JsonNode node, String fieldName) {
    JsonNode subNode = node.path(fieldName);
    if (subNode.isNull() || subNode.isMissingNode()) {
      return null;
    }
    return subNode.asText();
  }

  /**
   * Get the Integer field from {@link JsonNode}
   *
   * @param node {@link JsonNode}
   * @param fieldName Field Name
   * @return Integer value of the field or null
   */
  public static Integer getInteger(JsonNode node, String fieldName) {
    JsonNode subNode = node.path(fieldName);
    if (subNode.isNull() || subNode.isMissingNode()) {
      return null;
    }
    return subNode.asInt();
  }

  /**
   * Get the Long field from {@link JsonNode}
   *
   * @param node {@link JsonNode}
   * @param fieldName Field Name
   * @return Long value of the field or null
   */
  public static Long getLong(JsonNode node, String fieldName) {
    JsonNode subNode = node.path(fieldName);
    if (subNode.isNull() || subNode.isMissingNode()) {
      return null;
    }
    return subNode.asLong();
  }

  /**
   * Get the Double field from {@link JsonNode}
   *
   * @param node {@link JsonNode}
   * @param fieldName Field Name
   * @return Double value of the field or null
   */
  public static Double getDouble(JsonNode node, String fieldName) {
    JsonNode subNode = node.path(fieldName);
    if (subNode.isNull() || subNode.isMissingNode()) {
      return null;
    }
    return subNode.asDouble();
  }

  /**
   * Prior to Java 5, java memory model had a lot of issues and other approaches used to fail in
   * certain scenarios where too many threads try to get the instance of the Singleton class
   * simultaneously. So Bill Pugh came up with a different approach to create the Singleton class
   * using a inner static helper class.
   *
   * Notice the private inner static class that contains the instance of the singleton class. When
   * the singleton class is loaded, SingletonHelper class is not loaded into memory and only when
   * someone calls the getInstance method, this class gets loaded and creates the Singleton class
   * instance.
   */
  private static class SingletonHelper {

    private static final ObjectMapper MAPPER;

    static {
      // mapper
      MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
      // serialization
      MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
      MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
      MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
      // deserialization
      MAPPER.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
      MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
  }
}
