package com.github.longkerdandy.viki.home.schema;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Property Schema
 */
public class PropertySchema<T> extends DataSchema<T> {

  protected final ResourceBundle resources;   // resources for localized label and description

  protected boolean readable = true;          // can be read
  protected boolean writable = false;         // can be write
  protected boolean hidden = false;           // can be displayed
  protected boolean observable = false;       // can be observed

  /**
   * Constructor
   *
   * @param name developer friendly name
   * @param type {@link DataType}
   * @param resources {@link ResourceBundle}
   */
  protected PropertySchema(String name, DataType type, ResourceBundle resources) {
    super(name, type);
    this.resources = resources;
  }

  /**
   * Create PropertySchema<Long> with {@link DataType#INTEGER}
   *
   * @param name developer friendly name
   * @param resources {@link ResourceBundle}
   * @param minimum <strong>Optional</strong> minimum value (include)
   * @param maximum <strong>Optional</strong> maximum value (include)
   * @param constant <strong>Optional</strong> constant value
   * @param enumeration <strong>Optional</strong> possible values
   * @return PropertySchema<Long>
   */
  public static PropertySchema<Long> createIntegerProperty(String name, ResourceBundle resources,
      Long minimum, Long maximum, Long constant, Long[] enumeration) {
    PropertySchema<Long> prop = new PropertySchema<>(name, DataType.INTEGER, resources);
    prop.minimum = minimum;
    prop.maximum = maximum;
    prop.constant = constant;
    prop.enumeration = enumeration;
    return prop;
  }

  /**
   * Create PropertySchema<Double> with {@link DataType#NUMBER}
   *
   * @param name developer friendly name
   * @param resources {@link ResourceBundle}
   * @param minimum <strong>Optional</strong> minimum value (include)
   * @param maximum <strong>Optional</strong> maximum value (include)
   * @param constant <strong>Optional</strong> constant value
   * @param enumeration <strong>Optional</strong> possible values
   * @return PropertySchema<Double>
   */
  public static PropertySchema<Double> createNumberProperty(String name, ResourceBundle resources,
      Double minimum, Double maximum, Double constant, Double[] enumeration) {
    PropertySchema<Double> prop = new PropertySchema<>(name, DataType.NUMBER, resources);
    prop.minimum = minimum;
    prop.maximum = maximum;
    prop.constant = constant;
    prop.enumeration = enumeration;
    return prop;
  }

  /**
   * Create PropertySchema<String> with {@link DataType#STRING}
   *
   * @param name developer friendly name
   * @param resources {@link ResourceBundle}
   * @param minLength <strong>Optional</strong> minimum length (include)
   * @param maxLength <strong>Optional</strong> maximum length (include)
   * @param pattern <strong>Optional</strong> regex pattern
   * @param constant <strong>Optional</strong> constant value
   * @param enumeration <strong>Optional</strong> possible values
   * @return PropertySchema<String>
   */
  public static PropertySchema<String> createStringProperty(String name, ResourceBundle resources,
      Integer minLength, Integer maxLength, Pattern pattern,
      String constant, String[] enumeration) {
    PropertySchema<String> prop = new PropertySchema<>(name, DataType.STRING, resources);
    prop.minLength = minLength;
    prop.maxLength = maxLength;
    prop.pattern = pattern;
    prop.constant = constant;
    prop.enumeration = enumeration;
    return prop;
  }

  /**
   * Create PropertySchema<Boolean> with {@link DataType#BOOLEAN}
   *
   * @param name developer friendly name
   * @param resources {@link ResourceBundle}
   * @param constant <strong>Optional</strong> constant value
   * @return PropertySchema<Boolean>
   */
  public static PropertySchema<Boolean> createBooleanProperty(String name, ResourceBundle resources,
      Boolean constant) {
    PropertySchema<Boolean> prop = new PropertySchema<>(name, DataType.BOOLEAN, resources);
    prop.constant = constant;
    return prop;
  }

  /**
   * Create PropertySchema<LocalDateTime> with {@link DataType#DATETIME}
   *
   * @param name developer friendly name
   * @param resources {@link ResourceBundle}
   * @param minimum <strong>Optional</strong> minimum value (include)
   * @param maximum <strong>Optional</strong> maximum value (include)
   * @param constant <strong>Optional</strong> constant value
   * @return PropertySchema<LocalDateTime>
   */
  public static PropertySchema<LocalDateTime> createDateTimeProperty(
      String name, ResourceBundle resources,
      LocalDateTime minimum, LocalDateTime maximum, LocalDateTime constant) {
    PropertySchema<LocalDateTime> prop = new PropertySchema<>(name, DataType.DATETIME, resources);
    prop.minimum = minimum;
    prop.maximum = maximum;
    prop.constant = constant;
    return prop;
  }

  /**
   * Create PropertySchema<Object> with {@link DataType#ARRAY}
   *
   * @param name developer friendly name
   * @param resources {@link ResourceBundle}
   * @param minLength <strong>Optional</strong> minimum length (include)
   * @param maxLength <strong>Optional</strong> maximum length (include)
   * @param items <strong>Optional</strong> array item with unknown length
   * @param constant <strong>Optional</strong> constant value
   * @param enumeration <strong>Optional</strong> possible values
   * @return PropertySchema<Object>
   */
  public static PropertySchema<Object> createArrayProperty(String name, ResourceBundle resources,
      Integer minLength, Integer maxLength, PropertySchema items,
      String constant, String[] enumeration) {
    PropertySchema<Object> prop = new PropertySchema<>(name, DataType.ARRAY, resources);
    prop.minLength = minLength;
    prop.maxLength = maxLength;
    prop.items = items;
    prop.constant = constant;
    prop.enumeration = enumeration;
    return prop;
  }

  /**
   * Create PropertySchema<byte[]> with {@link DataType#BLOB}
   *
   * @param name developer friendly name
   * @param resources {@link ResourceBundle}
   * @param minLength <strong>Optional</strong> minimum length (include)
   * @param maxLength <strong>Optional</strong> maximum length (include)
   * @param constant <strong>Optional</strong> constant value
   * @return PropertySchema
   */
  public static PropertySchema<byte[]> createBlobProperty(String name, ResourceBundle resources,
      Integer minLength, Integer maxLength, byte[] constant) {
    PropertySchema<byte[]> prop = new PropertySchema<>(name, DataType.BLOB, resources);
    prop.minLength = minLength;
    prop.maxLength = maxLength;
    prop.constant = constant;
    return prop;
  }

  /**
   * Create PropertySchema<Object> with {@link DataType#BLOB}
   *
   * @param name developer friendly name
   * @param resources {@link ResourceBundle}
   * @param properties <strong>Optional</strong> array items with fixed length and order
   * @return PropertySchema<Object>
   */
  public static PropertySchema<Object> createObjectProperty(String name, ResourceBundle resources,
      Map<String, DataSchema> properties) {
    PropertySchema<Object> prop = new PropertySchema<>(name, DataType.Object, resources);
    prop.properties = properties;
    return prop;
  }

  @Override
  public String getLabel() {
    return resources.getString("property." + name + ".label");
  }

  @Override
  public String getDescription() {
    return resources.getString("property." + name + ".description");
  }

  public PropertySchema<T> readable(boolean readable) {
    this.readable = readable;
    return this;
  }

  public PropertySchema<T> writable(boolean writable) {
    this.writable = writable;
    return this;
  }

  public PropertySchema<T> hidden(boolean hidden) {
    this.hidden = hidden;
    return this;
  }

  public PropertySchema<T> observable(boolean observable) {
    this.observable = observable;
    return this;
  }

  public boolean isReadable() {
    return readable;
  }

  public boolean isWritable() {
    return writable;
  }

  public boolean isHidden() {
    return hidden;
  }

  public boolean isObservable() {
    return observable;
  }

  @Override
  public String toString() {
    return "PropertySchema{" +
        "readable=" + readable +
        ", writable=" + writable +
        ", hidden=" + hidden +
        ", observable=" + observable +
        ", name='" + name + '\'' +
        ", type=" + type +
        ", label='" + getLabel() + '\'' +
        ", description='" + getDescription() + '\'' +
        ", minimum=" + minimum +
        ", maximum=" + maximum +
        ", constant=" + constant +
        ", enumeration=" + Arrays.toString(enumeration) +
        ", minLength=" + minLength +
        ", maxLength=" + maxLength +
        ", pattern=" + pattern +
        ", format=" + format +
        ", items=" + items +
        ", properties=" + properties +
        '}';
  }
}
