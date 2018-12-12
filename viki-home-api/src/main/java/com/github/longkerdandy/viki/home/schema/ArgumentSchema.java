package com.github.longkerdandy.viki.home.schema;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Action Argument Schema
 */
public class ArgumentSchema<T> extends DataSchema<T> {

  protected final ResourceBundle resources;   // resources for localized label and description

  protected boolean required = true;          // can be read

  /**
   * Constructor
   *
   * @param name developer friendly name
   * @param type {@link DataType}
   * @param resources {@link ResourceBundle}
   */
  protected ArgumentSchema(String name, DataType type, ResourceBundle resources) {
    super(name, type);
    this.resources = resources;
  }

  /**
   * Create ArgumentSchema<Long> with {@link DataType#INTEGER}
   *
   * @param name developer friendly name
   * @param resources {@link ResourceBundle}
   * @param minimum <strong>Optional</strong> minimum value (include)
   * @param maximum <strong>Optional</strong> maximum value (include)
   * @param constant <strong>Optional</strong> constant value
   * @param enumeration <strong>Optional</strong> possible values
   * @return ArgumentSchema<Long>
   */
  public static ArgumentSchema<Long> createIntegerArgument(String name, ResourceBundle resources,
      Long minimum, Long maximum, Long constant, Long[] enumeration) {
    ArgumentSchema<Long> args = new ArgumentSchema<>(name, DataType.INTEGER, resources);
    args.minimum = minimum;
    args.maximum = maximum;
    args.constant = constant;
    args.enumeration = enumeration;
    return args;
  }

  /**
   * Create ArgumentSchema<Double> with {@link DataType#NUMBER}
   *
   * @param name developer friendly name
   * @param resources {@link ResourceBundle}
   * @param minimum <strong>Optional</strong> minimum value (include)
   * @param maximum <strong>Optional</strong> maximum value (include)
   * @param constant <strong>Optional</strong> constant value
   * @param enumeration <strong>Optional</strong> possible values
   * @return ArgumentSchema<Double>
   */
  public static ArgumentSchema<Double> createNumberArgument(String name, ResourceBundle resources,
      Double minimum, Double maximum, Double constant, Double[] enumeration) {
    ArgumentSchema<Double> args = new ArgumentSchema<>(name, DataType.NUMBER, resources);
    args.minimum = minimum;
    args.maximum = maximum;
    args.constant = constant;
    args.enumeration = enumeration;
    return args;
  }

  /**
   * Create ArgumentSchema<String> with {@link DataType#STRING}
   *
   * @param name developer friendly name
   * @param resources {@link ResourceBundle}
   * @param minLength <strong>Optional</strong> minimum length (include)
   * @param maxLength <strong>Optional</strong> maximum length (include)
   * @param pattern <strong>Optional</strong> regex pattern
   * @param constant <strong>Optional</strong> constant value
   * @param enumeration <strong>Optional</strong> possible values
   * @return ArgumentSchema<String>
   */
  public static ArgumentSchema<String> createStringArgument(String name, ResourceBundle resources,
      Integer minLength, Integer maxLength, Pattern pattern,
      String constant, String[] enumeration) {
    ArgumentSchema<String> args = new ArgumentSchema<>(name, DataType.STRING, resources);
    args.minLength = minLength;
    args.maxLength = maxLength;
    args.pattern = pattern;
    args.constant = constant;
    args.enumeration = enumeration;
    return args;
  }

  /**
   * Create ArgumentSchema<Boolean> with {@link DataType#BOOLEAN}
   *
   * @param name developer friendly name
   * @param resources {@link ResourceBundle}
   * @param constant <strong>Optional</strong> constant value
   * @return ArgumentSchema<Boolean>
   */
  public static ArgumentSchema<Boolean> createBooleanArgument(String name, ResourceBundle resources,
      Boolean constant) {
    ArgumentSchema<Boolean> args = new ArgumentSchema<>(name, DataType.BOOLEAN, resources);
    args.constant = constant;
    return args;
  }

  /**
   * Create ArgumentSchema<LocalDateTime> with {@link DataType#DATETIME}
   *
   * @param name developer friendly name
   * @param resources {@link ResourceBundle}
   * @param minimum <strong>Optional</strong> minimum value (include)
   * @param maximum <strong>Optional</strong> maximum value (include)
   * @param constant <strong>Optional</strong> constant value
   * @return ArgumentSchema<LocalDateTime>
   */
  public static ArgumentSchema<LocalDateTime> createDateTimeArgument(
      String name, ResourceBundle resources,
      LocalDateTime minimum, LocalDateTime maximum, LocalDateTime constant) {
    ArgumentSchema<LocalDateTime> args = new ArgumentSchema<>(name, DataType.DATETIME, resources);
    args.minimum = minimum;
    args.maximum = maximum;
    args.constant = constant;
    return args;
  }

  /**
   * Create ArgumentSchema<Object> with {@link DataType#ARRAY}
   *
   * @param name developer friendly name
   * @param resources {@link ResourceBundle}
   * @param minLength <strong>Optional</strong> minimum length (include)
   * @param maxLength <strong>Optional</strong> maximum length (include)
   * @param items <strong>Optional</strong> array item with unknown length
   * @param constant <strong>Optional</strong> constant value
   * @param enumeration <strong>Optional</strong> possible values
   * @return ArgumentSchema<Object>
   */
  public static ArgumentSchema<Object> createArrayArgument(String name, ResourceBundle resources,
      Integer minLength, Integer maxLength, ArgumentSchema items,
      String constant, String[] enumeration) {
    ArgumentSchema<Object> args = new ArgumentSchema<>(name, DataType.ARRAY, resources);
    args.minLength = minLength;
    args.maxLength = maxLength;
    args.items = items;
    args.constant = constant;
    args.enumeration = enumeration;
    return args;
  }

  /**
   * Create ArgumentSchema<byte[]> with {@link DataType#BLOB}
   *
   * @param name developer friendly name
   * @param resources {@link ResourceBundle}
   * @param minLength <strong>Optional</strong> minimum length (include)
   * @param maxLength <strong>Optional</strong> maximum length (include)
   * @param constant <strong>Optional</strong> constant value
   * @return ArgumentSchema
   */
  public static ArgumentSchema<byte[]> createBlobArgument(String name, ResourceBundle resources,
      Integer minLength, Integer maxLength, byte[] constant) {
    ArgumentSchema<byte[]> args = new ArgumentSchema<>(name, DataType.BLOB, resources);
    args.minLength = minLength;
    args.maxLength = maxLength;
    args.constant = constant;
    return args;
  }

  /**
   * Create ArgumentSchema<Object> with {@link DataType#BLOB}
   *
   * @param name developer friendly name
   * @param resources {@link ResourceBundle}
   * @param properties <strong>Optional</strong> array items with fixed length and order
   * @return ArgumentSchema<Object>
   */
  public static ArgumentSchema<Object> createObjectArgument(String name, ResourceBundle resources,
      Map<String, DataSchema> properties) {
    ArgumentSchema<Object> args = new ArgumentSchema<>(name, DataType.Object, resources);
    args.properties = properties;
    return args;
  }

  @Override
  public String getLabel() {
    return resources.getString("argument." + name + ".label");
  }

  @Override
  public String getDescription() {
    return resources.getString("argument." + name + ".description");
  }

  public ArgumentSchema<T> required(boolean required) {
    this.required = required;
    return this;
  }

  public boolean isRequired() {
    return required;
  }

  @Override
  public String toString() {
    return "ArgumentSchema{" +
        "required=" + required +
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
