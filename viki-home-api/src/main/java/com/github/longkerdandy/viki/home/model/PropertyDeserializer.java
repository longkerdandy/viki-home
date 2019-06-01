package com.github.longkerdandy.viki.home.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Jackson Deserializer for {@link Property}
 */
public class PropertyDeserializer extends StdDeserializer<Property> {

  /**
   * Constructor
   */
  protected PropertyDeserializer() {
    super(Property.class);
  }

  @Override
  public Property deserialize(JsonParser p, DeserializationContext context) throws IOException {
    // read into JsonNode
    JsonNode node = p.readValueAsTree();
    if (node.isNull() || node.isMissingNode()) {
      return null;
    }

    // parse name field
    JsonNode nameNode = node.path("name");
    if (nameNode.isNull() || nameNode.isMissingNode()) {
      throw new IOException("Property json missing name field");
    }
    String name = nameNode.asText();

    // parse DataType field
    JsonNode typeNode = node.path("type");
    if (typeNode.isNull() || typeNode.isMissingNode()) {
      throw new IOException("Property json missing type field");
    }
    DataType type = DataType.fromValue(typeNode.asText());

    // parse updatedAt field
    JsonNode updatedAtNode = node.path("updatedAt");
    if (updatedAtNode.isNull() || updatedAtNode.isMissingNode()) {
      throw new IOException("Property json missing updatedAt field");
    }
    LocalDateTime updatedAt = LocalDateTime.parse(updatedAtNode.asText());

    // parse value based on DataType
    JsonNode valueNode = node.path("value");
    switch (type) {
      case INTEGER:
        return valueNode.isNull() || valueNode.isMissingNode()
            ? new Property<Long>(name, type, null, updatedAt)
            : new Property<>(name, type, valueNode.asLong(), updatedAt);
      case NUMBER:
        return valueNode.isNull() || valueNode.isMissingNode()
            ? new Property<Double>(name, type, null, updatedAt)
            : new Property<>(name, type, valueNode.asDouble(), updatedAt);
      case STRING:
        return valueNode.isNull() || valueNode.isMissingNode()
            ? new Property<String>(name, type, null, updatedAt)
            : new Property<>(name, type, valueNode.asText(), updatedAt);
      case BOOLEAN:
        return valueNode.isNull() || valueNode.isMissingNode()
            ? new Property<Boolean>(name, type, null, updatedAt)
            : new Property<>(name, type, valueNode.asBoolean(), updatedAt);
      case DATETIME:
        if (valueNode.isNull() || valueNode.isMissingNode()) {
          return new Property<LocalDateTime>(name, type, null, updatedAt);
        } else {
          return new Property<>(name, type,
              valueNode.traverse(p.getCodec()).readValueAs(LocalDateTime.class), updatedAt);
        }
      case ARRAY_INTEGER:
        if (valueNode.isNull() || valueNode.isMissingNode()) {
          return new Property<byte[]>(name, type, null, updatedAt);
        } else {
          return new Property<>(name, type,
              valueNode.traverse(p.getCodec()).readValueAs(long[].class), updatedAt);
        }
      case ARRAY_NUMBER:
        if (valueNode.isNull() || valueNode.isMissingNode()) {
          return new Property<byte[]>(name, type, null, updatedAt);
        } else {
          return new Property<>(name, type,
              valueNode.traverse(p.getCodec()).readValueAs(double[].class), updatedAt);
        }
      case ARRAY_STRING:
        if (valueNode.isNull() || valueNode.isMissingNode()) {
          return new Property<byte[]>(name, type, null, updatedAt);
        } else {
          return new Property<>(name, type,
              valueNode.traverse(p.getCodec()).readValueAs(String[].class), updatedAt);
        }
      case BLOB:
        if (valueNode.isNull() || valueNode.isMissingNode()) {
          return new Property<byte[]>(name, type, null, updatedAt);
        } else {
          return new Property<>(name, type,
              valueNode.traverse(p.getCodec()).readValueAs(byte[].class), updatedAt);
        }
      default:
        throw new IOException("Property json's type field is unknown.");
    }
  }
}
