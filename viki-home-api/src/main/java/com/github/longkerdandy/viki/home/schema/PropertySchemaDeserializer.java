package com.github.longkerdandy.viki.home.schema;

import static com.github.longkerdandy.viki.home.util.Jacksons.checkString;
import static com.github.longkerdandy.viki.home.util.Jacksons.getBoolean;
import static com.github.longkerdandy.viki.home.util.Jacksons.getDouble;
import static com.github.longkerdandy.viki.home.util.Jacksons.getInteger;
import static com.github.longkerdandy.viki.home.util.Jacksons.getLong;
import static com.github.longkerdandy.viki.home.util.Jacksons.getString;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.github.longkerdandy.viki.home.model.DataType;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Jackson Deserializer for {@link PropertySchema}
 */
public class PropertySchemaDeserializer extends StdDeserializer<PropertySchema> {

  /**
   * Constructor
   */
  protected PropertySchemaDeserializer() {
    super(PropertySchema.class);
  }

  @Override
  public PropertySchema deserialize(JsonParser p, DeserializationContext context)
      throws IOException {
    // Read into JsonNode tree
    JsonNode node = p.readValueAsTree();
    if (node.isNull() || node.isMissingNode()) {
      return null;
    }

    PropertySchema result = null;

    // Parse name and DataType fields
    String name = checkString(node, "name");
    DataType type = DataType.fromValue(checkString(node, "type"));

    // Parse other fields based on type
    JsonNode minimumNode = node.path("minimum");
    JsonNode maximumNode = node.path("maximum");
    JsonNode constantNode = node.path("constant");
    JsonNode enumerationNode = node.path("enumeration");
    JsonNode itemNode = node.path("item");
    switch (type) {
      case INTEGER:
        result = PropertySchema.createIntegerProperty(
            name,
            getLong(node, "minimum"),
            getLong(node, "maximum"),
            getLong(node, "constant"),
            enumerationNode.isArray() ?
                enumerationNode.traverse(p.getCodec()).readValueAs(Long[].class) : null);
        break;
      case NUMBER:
        result = PropertySchema.createNumberProperty(
            name,
            getDouble(node, "minimum"),
            getDouble(node, "maximum"),
            getDouble(node, "constant"),
            enumerationNode.isArray() ?
                enumerationNode.traverse(p.getCodec()).readValueAs(Double[].class) : null);
        break;
      case STRING:
        String pattern = getString(node, "pattern");
        result = PropertySchema.createStringProperty(
            name,
            getInteger(node, "minLength"),
            getInteger(node, "maxLength"),
            pattern != null ? Pattern.compile(pattern) : null,
            getString(node, "constant"),
            enumerationNode.isArray() ?
                enumerationNode.traverse(p.getCodec()).readValueAs(String[].class) : null);
        break;
      case BOOLEAN:
        result = PropertySchema.createBooleanProperty(name, getBoolean(node, "constant"));
        break;
      case DATETIME:
        result = PropertySchema.createDateTimeProperty(
            name,
            minimumNode.isTextual() || minimumNode.isIntegralNumber() ?
                minimumNode.traverse(p.getCodec()).readValueAs(LocalDateTime.class) : null,
            maximumNode.isTextual() || maximumNode.isIntegralNumber() ?
                maximumNode.traverse(p.getCodec()).readValueAs(LocalDateTime.class) : null,
            constantNode.isTextual() || constantNode.isIntegralNumber() ?
                constantNode.traverse(p.getCodec()).readValueAs(LocalDateTime.class) : null);
        break;
      case ARRAY_INTEGER:
        result = PropertySchema.createIntegerArrayProperty(
            name,
            getInteger(node, "minLength"),
            getInteger(node, "maxLength"),
            itemNode.isObject() ? deserialize(itemNode.traverse(p.getCodec()), context) : null,
            constantNode.isArray() ?
                constantNode.traverse(p.getCodec()).readValueAs(long[].class) : null);
        break;
      case ARRAY_NUMBER:
        result = PropertySchema.createNumberArrayProperty(
            name,
            getInteger(node, "minLength"),
            getInteger(node, "maxLength"),
            itemNode.isObject() ? deserialize(itemNode.traverse(p.getCodec()), context) : null,
            constantNode.isArray() ?
                constantNode.traverse(p.getCodec()).readValueAs(double[].class) : null);
        break;
      case ARRAY_STRING:
        result = PropertySchema.createStringArrayProperty(
            name,
            getInteger(node, "minLength"),
            getInteger(node, "maxLength"),
            itemNode.isObject() ? deserialize(itemNode.traverse(p.getCodec()), context) : null,
            constantNode.isArray() ?
                constantNode.traverse(p.getCodec()).readValueAs(String[].class) : null);
        break;
      case BLOB:
        result = PropertySchema.createBlobProperty(
            name,
            getInteger(node, "minLength"),
            getInteger(node, "maxLength"),
            constantNode.isArray() ?
                constantNode.traverse(p.getCodec()).readValueAs(byte[].class) : null);
        break;
    }

    // Parse writable and observable fields, then return
    return result
        .writable(node.path("writable").asBoolean(result.writable))
        .observable(node.path("observable").asBoolean(result.observable));
  }
}
