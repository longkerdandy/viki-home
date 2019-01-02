package com.github.longkerdandy.viki.home.model;

import static com.github.longkerdandy.viki.home.model.DataType.ARRAY_INTEGER;
import static com.github.longkerdandy.viki.home.model.DataType.ARRAY_NUMBER;
import static com.github.longkerdandy.viki.home.model.DataType.ARRAY_STRING;
import static com.github.longkerdandy.viki.home.model.DataType.BOOLEAN;
import static com.github.longkerdandy.viki.home.model.DataType.DATETIME;
import static com.github.longkerdandy.viki.home.model.DataType.INTEGER;
import static com.github.longkerdandy.viki.home.model.DataType.NUMBER;
import static com.github.longkerdandy.viki.home.model.DataType.STRING;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.longkerdandy.viki.home.json.PropertyDeserializer;
import com.github.longkerdandy.viki.home.schema.PropertySchema;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.regex.Matcher;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Property
 *
 * @param <T> value type base on the data type
 * <pre> The relations between {@link DataType} and generic type
 *      DataType            T
 *      INTEGER             Long
 *      NUMBER              Double
 *      STRING              String
 *      BOOLEAN             Boolean
 *      DATETIME            LocalDateTime
 *      ARRAY_INTEGER       long[]
 *      ARRAY_NUMBER        double[]
 *      ARRAY_STRING        String[]
 *      BLOB                byte[]
 *      OBJECT              Property[]
 * </pre>
 */
@JsonDeserialize(using = PropertyDeserializer.class)
public class Property<T> {

  protected final String name;                    // developer friendly name
  protected final DataType type;                  // data type
  protected final T value;                        // value

  /**
   * Constructor
   *
   * @param name developer friendly name
   * @param type {@link DataType}
   * @param value value
   */
  @JsonCreator
  public Property(@JsonProperty("name") String name,
      @JsonProperty("type") DataType type,
      @JsonProperty("value") T value) {
    this.name = name;
    this.type = type;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public DataType getType() {
    return type;
  }

  public T getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "Property{" +
        "name='" + name + '\'' +
        ", type=" + type +
        ", value=" + value +
        '}';
  }

  /**
   * Validate the Property
   *
   * @param propertySchema {@link PropertySchema}
   * @return True if Property is valid
   */
  @SuppressWarnings("unchecked")
  public boolean validate(PropertySchema propertySchema) {
    if (propertySchema == null) {
      return false;
    }

    if (StringUtils.isEmpty(name)) {
      return false;
    }

    if (type != propertySchema.getType()) {
      return false;
    }

    if (value == null) {
      return !propertySchema.isRequired();
    }

    if (propertySchema.getConstant() != null && (type == INTEGER || type == NUMBER || type == STRING
        || type == BOOLEAN || type == DATETIME) && !propertySchema.getConstant().equals(value)) {
      return false;
    }

    if (propertySchema.getEnumeration() != null && (type == INTEGER || type == NUMBER
        || type == STRING) && !ArrayUtils.contains(propertySchema.getEnumeration(), value)) {
      return false;
    }

    if (propertySchema.getItem() == null && (type == ARRAY_INTEGER || type == ARRAY_NUMBER
        || type == ARRAY_STRING)) {
      return false;
    }

    switch (type) {
      case INTEGER:
        if (propertySchema.getMinimum() != null
            && (Long) value < (Long) propertySchema.getMinimum()) {
          return false;
        }
        if (propertySchema.getMaximum() != null
            && (Long) value > (Long) propertySchema.getMaximum()) {
          return false;
        }
        break;
      case NUMBER:
        if (propertySchema.getMinimum() != null
            && (Double) value < (Double) propertySchema.getMinimum()) {
          return false;
        }
        if (propertySchema.getMaximum() != null
            && (Double) value > (Double) propertySchema.getMaximum()) {
          return false;
        }
        break;
      case STRING:
        if (propertySchema.getMinLength() != null
            && ((String) value).length() < propertySchema.getMinLength()) {
          return false;
        }
        if (propertySchema.getMaxLength() != null
            && ((String) value).length() > propertySchema.getMaxLength()) {
          return false;
        }
        if (propertySchema.getPattern() != null) {
          Matcher matcher = propertySchema.getPattern().matcher((String) value);
          if (!matcher.matches()) {
            return false;
          }
        }
        break;
      case BOOLEAN:
        break;
      case DATETIME:
        if (propertySchema.getMinimum() != null
            && ((LocalDateTime) propertySchema.getMinimum()).isAfter((LocalDateTime) value)) {
          return false;
        }
        if (propertySchema.getMaximum() != null
            && ((LocalDateTime) propertySchema.getMaximum()).isBefore((LocalDateTime) value)) {
          return false;
        }
        break;
      case ARRAY_INTEGER:
        if (propertySchema.getConstant() != null
            && !Arrays.equals((long[]) value, (long[]) propertySchema.getConstant())) {
          return false;
        }
        if (propertySchema.getMinLength() != null
            && ((long[]) value).length < propertySchema.getMinLength()) {
          return false;
        }
        if (propertySchema.getMaxLength() != null
            && ((long[]) value).length > propertySchema.getMaxLength()) {
          return false;
        }
        for (long l : (long[]) value) {
          PropertySchema<Long> item = (PropertySchema<Long>) propertySchema.getItem();
          if (item.getMinimum() != null && l < item.getMinimum()) {
            return false;
          }
          if (item.getMaximum() != null && l > item.getMaximum()) {
            return false;
          }
          if (item.getConstant() != null && l != item.getConstant()) {
            return false;
          }
          if (item.getEnumeration() != null && !ArrayUtils.contains(item.getEnumeration(), l)) {
            return false;
          }
        }
        break;
      case ARRAY_NUMBER:
        if (propertySchema.getConstant() != null
            && !Arrays.equals((double[]) value, (double[]) propertySchema.getConstant())) {
          return false;
        }
        if (propertySchema.getMinLength() != null
            && ((double[]) value).length < propertySchema.getMinLength()) {
          return false;
        }
        if (propertySchema.getMaxLength() != null
            && ((double[]) value).length > propertySchema.getMaxLength()) {
          return false;
        }
        for (double d : (double[]) value) {
          PropertySchema<Double> item = (PropertySchema<Double>) propertySchema.getItem();
          if (item.getMinimum() != null && d < item.getMinimum()) {
            return false;
          }
          if (item.getMaximum() != null && d > item.getMaximum()) {
            return false;
          }
          if (item.getConstant() != null && d != item.getConstant()) {
            return false;
          }
          if (item.getEnumeration() != null && !ArrayUtils.contains(item.getEnumeration(), d)) {
            return false;
          }
        }
        break;
      case ARRAY_STRING:
        if (propertySchema.getConstant() != null
            && !Arrays.equals((String[]) value, (String[]) propertySchema.getConstant())) {
          return false;
        }
        if (propertySchema.getMinLength() != null
            && ((String[]) value).length < propertySchema.getMinLength()) {
          return false;
        }
        if (propertySchema.getMaxLength() != null
            && ((String[]) value).length > propertySchema.getMaxLength()) {
          return false;
        }
        for (String s : (String[]) value) {
          PropertySchema<String> item = (PropertySchema<String>) propertySchema.getItem();
          if (item.getMinLength() != null && s.length() < item.getMinLength()) {
            return false;
          }
          if (item.getMaxLength() != null && s.length() > item.getMaxLength()) {
            return false;
          }
          if (item.getConstant() != null && !item.getConstant().equals(s)) {
            return false;
          }
          if (item.getEnumeration() != null && !ArrayUtils.contains(item.getEnumeration(), s)) {
            return false;
          }
          if (item.getPattern() != null) {
            Matcher matcher = item.getPattern().matcher(s);
            if (!matcher.matches()) {
              return false;
            }
          }
        }
        break;
      case BLOB:
        if (propertySchema.getConstant() != null
            && !Arrays.equals((byte[]) value, (byte[]) propertySchema.getConstant())) {
          return false;
        }
        if (propertySchema.getMinLength() != null
            && ((byte[]) value).length < propertySchema.getMinLength()) {
          return false;
        }
        if (propertySchema.getMaxLength() != null
            && ((byte[]) value).length > propertySchema.getMaxLength()) {
          return false;
        }
        break;
      case OBJECT:
        if (propertySchema.getProperties() == null) {
          return false;
        }
        for (Property p : (Property[]) value) {
          if (!p.validate((PropertySchema) propertySchema.getProperties().get(p.name))) {
            return false;
          }
        }
        break;
    }

    return true;
  }
}
