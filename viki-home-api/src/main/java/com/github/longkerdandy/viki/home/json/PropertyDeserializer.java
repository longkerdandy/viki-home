package com.github.longkerdandy.viki.home.json;

import static com.fasterxml.jackson.databind.node.JsonNodeType.ARRAY;
import static com.fasterxml.jackson.databind.node.JsonNodeType.MISSING;
import static com.fasterxml.jackson.databind.node.JsonNodeType.NULL;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.github.longkerdandy.viki.home.model.DataType;
import com.github.longkerdandy.viki.home.model.Property;
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

    // parse DataType field
    JsonNode nameNode = node.path("name");
    if (nameNode.getNodeType() == NULL || nameNode.getNodeType() == MISSING) {
      throw new IOException("Property json missing name field");
    }
    String name = nameNode.asText();

    // parse DataType field
    JsonNode typeNode = node.path("type");
    if (typeNode.getNodeType() == NULL || typeNode.getNodeType() == MISSING) {
      throw new IOException("Property json missing type field");
    }
    DataType type = DataType.fromValue(typeNode.asText());

    // parse value based on DataType
    JsonNode valueNode = node.path("value");
    switch (type) {
      case INTEGER:
        return valueNode.getNodeType() == NULL || valueNode.getNodeType() == MISSING
            ? new Property<Long>(name, type, null)
            : new Property<>(name, type, valueNode.asLong());
      case NUMBER:
        return valueNode.getNodeType() == NULL || valueNode.getNodeType() == MISSING
            ? new Property<Double>(name, type, null)
            : new Property<>(name, type, valueNode.asDouble());
      case STRING:
        return valueNode.getNodeType() == NULL || valueNode.getNodeType() == MISSING
            ? new Property<String>(name, type, null)
            : new Property<>(name, type, valueNode.asText());
      case BOOLEAN:
        return valueNode.getNodeType() == NULL || valueNode.getNodeType() == MISSING
            ? new Property<Boolean>(name, type, null)
            : new Property<>(name, type, valueNode.asBoolean());
      case DATETIME:
        if (valueNode.getNodeType() == NULL || valueNode.getNodeType() == MISSING) {
          return new Property<LocalDateTime>(name, type, null);
        } else {
          return new Property<>(name, type,
              valueNode.traverse(p.getCodec()).readValueAs(LocalDateTime.class));
        }
      case ARRAY_INTEGER:
        if (valueNode.getNodeType() == NULL || valueNode.getNodeType() == MISSING) {
          return new Property<byte[]>(name, type, null);
        } else {
          return new Property<>(name, type,
              valueNode.traverse(p.getCodec()).readValueAs(long[].class));
        }
      case ARRAY_NUMBER:
        if (valueNode.getNodeType() == NULL || valueNode.getNodeType() == MISSING) {
          return new Property<byte[]>(name, type, null);
        } else {
          return new Property<>(name, type,
              valueNode.traverse(p.getCodec()).readValueAs(double[].class));
        }
      case ARRAY_STRING:
        if (valueNode.getNodeType() == NULL || valueNode.getNodeType() == MISSING) {
          return new Property<byte[]>(name, type, null);
        } else {
          return new Property<>(name, type,
              valueNode.traverse(p.getCodec()).readValueAs(String[].class));
        }
      case BLOB:
        if (valueNode.getNodeType() == NULL || valueNode.getNodeType() == MISSING) {
          return new Property<byte[]>(name, type, null);
        } else {
          return new Property<>(name, type,
              valueNode.traverse(p.getCodec()).readValueAs(byte[].class));
        }
      case OBJECT:
        // recursive deserialize object type
        if (valueNode.getNodeType() == NULL || valueNode.getNodeType() == MISSING) {
          return new Property<Property[]>(name, type, null);
        } else if (valueNode.getNodeType() == ARRAY) {
          Property[] value = new Property[valueNode.size()];
          for (int i = 0; i < valueNode.size(); i++) {
            value[i] = deserialize(valueNode.get(i).traverse(p.getCodec()), context);
          }
          return new Property<>(name, type, value);
        } else {
          throw new IOException("Property json's value field is not array.");
        }
      default:
        throw new IOException("Property json's type field is unknown.");
    }
  }
}
