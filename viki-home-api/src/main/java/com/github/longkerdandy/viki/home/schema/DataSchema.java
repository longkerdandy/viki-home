package com.github.longkerdandy.viki.home.schema;

import com.github.longkerdandy.viki.home.model.DataType;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Data Schema
 *
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
 *      OBJECT
 * </pre>
 *
 * <pre> The relations between {@link DataType} and fields
 *      DataType  minimum  maximum  constant  enumeration  minLength  maxLength  pattern  format  items  properties
 *      INTEGER      *       *         *           *
 *      NUMBER       *       *         *           *
 *      STRING                         *           *           *          *         *
 *      BOOLEAN                        *
 *      DATETIME     *       *         *                                                    *
 *      ARRAY                          *                       *          *                         *
 *      BLOB                           *                       *          *
 *      OBJECT                                                                                              *
 * </pre>
 */
public abstract class DataSchema<T> {

  protected final String name;                    // developer friendly name
  protected final DataType type;                  // data type

  protected T minimum;                            // minimum value (include)
  protected T maximum;                            // maximum value (include)
  protected T constant;                           // constant value
  protected T[] enumeration;                      // possible values
  protected Integer minLength;                    // minimum length (include)
  protected Integer maxLength;                    // maximum length (include)
  protected Pattern pattern;                      // string regex pattern
  protected DateTimeFormatter format;             // date time format
  protected DataSchema item;                      // schema for sub array item
  protected Map<String, DataSchema> properties;   // schema for sub objects

  /**
   * Constructor
   *
   * @param name developer friendly name
   * @param type {@link DataType}
   */
  protected DataSchema(String name, DataType type) {
    this.name = name;
    this.type = type;
  }

  /**
   * Get localized label
   *
   * @return label
   */
  public abstract String getLabel();

  /**
   * Get localized description
   *
   * @return description
   */
  public abstract String getDescription();

  public String getName() {
    return name;
  }

  public DataType getType() {
    return type;
  }

  public T getMinimum() {
    return minimum;
  }

  public T getMaximum() {
    return maximum;
  }

  public T getConstant() {
    return constant;
  }

  public T[] getEnumeration() {
    return enumeration;
  }

  public Integer getMinLength() {
    return minLength;
  }

  public Integer getMaxLength() {
    return maxLength;
  }

  public Pattern getPattern() {
    return pattern;
  }

  public DateTimeFormatter getFormat() {
    return format;
  }

  public DataSchema getItem() {
    return item;
  }

  public Map<String, DataSchema> getProperties() {
    return properties;
  }
}
