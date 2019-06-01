package com.github.longkerdandy.viki.home.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.longkerdandy.viki.home.model.DataType;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Property Schema
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
 *      DataType  minimum  maximum  constant  enumeration  minLength  maxLength  pattern  items
 *      INTEGER      *       *         *           *
 *      NUMBER       *       *         *           *
 *      STRING                         *           *           *          *         *
 *      BOOLEAN                        *
 *      DATETIME     *       *         *
 *      ARRAY                          *                       *          *                 *
 *      BLOB                           *                       *          *
 *      OBJECT
 * </pre>
 */
@JsonDeserialize(using = PropertySchemaDeserializer.class)
public class PropertySchema<T> {

  protected final String name;                    // property name
  protected final DataType type;                  // data type

  protected T minimum;                            // minimum value (include)
  protected T maximum;                            // maximum value (include)
  protected T constant;                           // constant value
  protected T[] enumeration;                      // possible values
  protected Integer minLength;                    // minimum length (include)
  protected Integer maxLength;                    // maximum length (include)
  protected Pattern pattern;                      // string regex pattern
  protected PropertySchema item;                  // schema for sub array item
  protected String function = "default";          // function that belong to
  protected boolean writable = false;             // can be write
  protected boolean observable = false;           // can be observed

  /**
   * Constructor
   *
   * @param name property name
   * @param type {@link DataType}
   */
  @JsonCreator
  protected PropertySchema(@JsonProperty("name") String name, @JsonProperty("type") DataType type) {
    this.name = name;
    this.type = type;
  }

  /**
   * Create {@link PropertySchema<Long>} with {@link DataType#INTEGER}
   *
   * @param name property name
   * @param minimum <strong>optional</strong> minimum value (include)
   * @param maximum <strong>optional</strong> maximum value (include)
   * @param constant <strong>optional</strong> constant value
   * @param enumeration <strong>optional</strong> possible values
   * @return {@link PropertySchema<Long>}
   */
  public static PropertySchema<Long> createIntegerProperty(String name, Long minimum, Long maximum,
      Long constant, Long[] enumeration) {
    PropertySchema<Long> prop = new PropertySchema<>(name, DataType.INTEGER);
    prop.minimum = minimum;
    prop.maximum = maximum;
    prop.constant = constant;
    prop.enumeration = enumeration;
    return prop;
  }

  /**
   * Create {@link PropertySchema<Double>} with {@link DataType#NUMBER}
   *
   * @param name property name
   * @param minimum <strong>optional</strong> minimum value (include)
   * @param maximum <strong>optional</strong> maximum value (include)
   * @param constant <strong>optional</strong> constant value
   * @param enumeration <strong>optional</strong> possible values
   * @return {@link PropertySchema<Double>}
   */
  public static PropertySchema<Double> createNumberProperty(String name, Double minimum,
      Double maximum, Double constant, Double[] enumeration) {
    PropertySchema<Double> prop = new PropertySchema<>(name, DataType.NUMBER);
    prop.minimum = minimum;
    prop.maximum = maximum;
    prop.constant = constant;
    prop.enumeration = enumeration;
    return prop;
  }

  /**
   * Create {@link PropertySchema<String>} with {@link DataType#STRING}
   *
   * @param name property name
   * @param minLength <strong>optional</strong> minimum length (include)
   * @param maxLength <strong>optional</strong> maximum length (include)
   * @param pattern <strong>optional</strong> regex pattern
   * @param constant <strong>optional</strong> constant value
   * @param enumeration <strong>optional</strong> possible values
   * @return {@link PropertySchema<String>}
   */
  public static PropertySchema<String> createStringProperty(String name, Integer minLength,
      Integer maxLength, Pattern pattern, String constant, String[] enumeration) {
    PropertySchema<String> prop = new PropertySchema<>(name, DataType.STRING);
    prop.minLength = minLength;
    prop.maxLength = maxLength;
    prop.pattern = pattern;
    prop.constant = constant;
    prop.enumeration = enumeration;
    return prop;
  }

  /**
   * Create {@link PropertySchema<Boolean>} with {@link DataType#BOOLEAN}
   *
   * @param name property name
   * @param constant <strong>optional</strong> constant value
   * @return {@link PropertySchema<Boolean>}
   */
  public static PropertySchema<Boolean> createBooleanProperty(String name, Boolean constant) {
    PropertySchema<Boolean> prop = new PropertySchema<>(name, DataType.BOOLEAN);
    prop.constant = constant;
    return prop;
  }

  /**
   * Create {@link PropertySchema<LocalDateTime>} with {@link DataType#DATETIME}
   *
   * @param name property name
   * @param minimum <strong>optional</strong> minimum value (include)
   * @param maximum <strong>optional</strong> maximum value (include)
   * @param constant <strong>optional</strong> constant value
   * @return {@link PropertySchema<LocalDateTime>}
   */
  public static PropertySchema<LocalDateTime> createDateTimeProperty(String name,
      LocalDateTime minimum, LocalDateTime maximum, LocalDateTime constant) {
    PropertySchema<LocalDateTime> prop = new PropertySchema<>(name, DataType.DATETIME);
    prop.minimum = minimum;
    prop.maximum = maximum;
    prop.constant = constant;
    return prop;
  }

  /**
   * Create PropertySchema<long[]> with {@link DataType#ARRAY_INTEGER}
   *
   * @param name property name
   * @param minLength <strong>optional</strong> minimum length (include)
   * @param maxLength <strong>optional</strong> maximum length (include)
   * @param items <strong>optional</strong> array item with unknown length
   * @param constant <strong>optional</strong> constant value
   * @return PropertySchema<long [ ]>
   */
  public static PropertySchema<long[]> createIntegerArrayProperty(String name, Integer minLength,
      Integer maxLength, PropertySchema items, long[] constant) {
    PropertySchema<long[]> prop = new PropertySchema<>(name, DataType.ARRAY_INTEGER);
    prop.minLength = minLength;
    prop.maxLength = maxLength;
    prop.item = items;
    prop.constant = constant;
    return prop;
  }

  /**
   * Create PropertySchema<double[]> with {@link DataType#ARRAY_NUMBER}
   *
   * @param name property name
   * @param minLength <strong>optional</strong> minimum length (include)
   * @param maxLength <strong>optional</strong> maximum length (include)
   * @param items <strong>optional</strong> array item with unknown length
   * @param constant <strong>optional</strong> constant value
   * @return PropertySchema<double [ ]>
   */
  public static PropertySchema<double[]> createNumberArrayProperty(String name, Integer minLength,
      Integer maxLength, PropertySchema items, double[] constant) {
    PropertySchema<double[]> prop = new PropertySchema<>(name, DataType.ARRAY_NUMBER);
    prop.minLength = minLength;
    prop.maxLength = maxLength;
    prop.item = items;
    prop.constant = constant;
    return prop;
  }

  /**
   * Create PropertySchema<String[]> with {@link DataType#ARRAY_STRING}
   *
   * @param name property name
   * @param minLength <strong>optional</strong> minimum length (include)
   * @param maxLength <strong>optional</strong> maximum length (include)
   * @param items <strong>optional</strong> array item with unknown length
   * @param constant <strong>optional</strong> constant value
   * @return PropertySchema<String [ ]>
   */
  public static PropertySchema<String[]> createStringArrayProperty(String name, Integer minLength,
      Integer maxLength, PropertySchema items, String[] constant) {
    PropertySchema<String[]> prop = new PropertySchema<>(name, DataType.ARRAY_STRING);
    prop.minLength = minLength;
    prop.maxLength = maxLength;
    prop.item = items;
    prop.constant = constant;
    return prop;
  }

  /**
   * Create PropertySchema<byte[]> with {@link DataType#BLOB}
   *
   * @param name property name
   * @param minLength <strong>optional</strong> minimum length (include)
   * @param maxLength <strong>optional</strong> maximum length (include)
   * @param constant <strong>optional</strong> constant value
   * @return PropertySchema<byte [ ]>
   */
  public static PropertySchema<byte[]> createBlobProperty(String name, Integer minLength,
      Integer maxLength, byte[] constant) {
    PropertySchema<byte[]> prop = new PropertySchema<>(name, DataType.BLOB);
    prop.minLength = minLength;
    prop.maxLength = maxLength;
    prop.constant = constant;
    return prop;
  }

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

  public PropertySchema getItem() {
    return item;
  }

  public String getFunction() {
    return function;
  }

  public boolean isWritable() {
    return writable;
  }

  public boolean isObservable() {
    return observable;
  }

  public PropertySchema<T> function(String function) {
    this.function = function;
    return this;
  }

  public PropertySchema<T> writable(boolean writable) {
    this.writable = writable;
    return this;
  }

  public PropertySchema<T> observable(boolean observable) {
    this.observable = observable;
    return this;
  }

  @Override
  public String toString() {
    return "PropertySchema{" +
        "name='" + name + '\'' +
        ", type=" + type +
        ", minimum=" + minimum +
        ", maximum=" + maximum +
        ", constant=" + constant +
        ", enumeration=" + Arrays.toString(enumeration) +
        ", minLength=" + minLength +
        ", maxLength=" + maxLength +
        ", pattern=" + pattern +
        ", item=" + item +
        ", function='" + function + '\'' +
        ", writable=" + writable +
        ", observable=" + observable +
        '}';
  }
}
