package com.github.longkerdandy.viki.home.mi.model;

import static com.github.longkerdandy.viki.home.mi.schema.SchemaMapping.paramToProp;
import static com.github.longkerdandy.viki.home.model.DataType.BLOB;
import static com.github.longkerdandy.viki.home.model.DataType.BOOLEAN;
import static com.github.longkerdandy.viki.home.model.DataType.DATETIME;
import static com.github.longkerdandy.viki.home.model.DataType.INTEGER;
import static com.github.longkerdandy.viki.home.model.DataType.NUMBER;
import static com.github.longkerdandy.viki.home.model.DataType.STRING;

import com.github.longkerdandy.viki.home.model.Property;
import com.github.longkerdandy.viki.home.model.Thing;
import com.github.longkerdandy.viki.home.schema.PropertySchema;
import com.github.longkerdandy.viki.home.schema.ThingSchema;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Zigbee Thing
 *
 * Zigbee protocol based thing which connected to {@link Gateway}. Please note the {@link Gateway}
 * itself is also considered to be a {@link ZigbeeThing}.
 */
public class ZigbeeThing {

  private final String sid;                   // unique id
  private final String gid;                   // gateway id
  private final String tid;                   // thing id
  private final String model;                 // model
  private final Integer shortId;              // zigbee id

  /**
   * Constructor
   */
  public ZigbeeThing(String sid, String gid, String tid, String model, Integer shortId) {
    this.sid = sid;
    this.gid = gid;
    this.tid = tid;
    this.model = model;
    this.shortId = shortId;
  }

  public String getSid() {
    return sid;
  }

  public String getGid() {
    return gid;
  }

  public String getTid() {
    return tid;
  }

  public String getModel() {
    return model;
  }

  public Integer getShortId() {
    return shortId;
  }

  /**
   * Convert to {@link Thing} based on {@link ThingSchema}
   *
   * @param schema {@link ThingSchema}
   * @param params will be convert to {@link Property}s
   * @return {@link Thing}
   */
  public Thing toThing(ThingSchema schema, Map<String, Object> params) {
    List<Property> properties = new ArrayList<>();
    properties.add(new Property<>("model", STRING, this.model, LocalDateTime.now()));
    for (Entry<String, Object> entry : paramToProp(this.model, params).entrySet()) {
      String propName = entry.getKey();
      Optional<PropertySchema> propertySchema = schema.getPropertyByName(propName);
      if (propertySchema.isPresent()) {
        Object value = entry.getValue();
        switch (propertySchema.get().getType()) {
          case INTEGER:
            properties.add(new Property<>(propName, INTEGER, value instanceof String ?
                Long.parseLong((String) value) : (Long) value, LocalDateTime.now()));
            break;
          case NUMBER:
            properties.add(new Property<>(propName, NUMBER, value instanceof String ?
                Double.parseDouble((String) value) : (Double) value, LocalDateTime.now()));
            break;
          case STRING:
            properties.add(new Property<>(propName, STRING, (String) value, LocalDateTime.now()));
            break;
          case BOOLEAN:
            properties.add(new Property<>(propName, BOOLEAN, value instanceof String ?
                Boolean.parseBoolean((String) value) : (Boolean) value, LocalDateTime.now()));
            break;
          case DATETIME:
            properties.add(new Property<>(propName, DATETIME, value instanceof String ?
                LocalDateTime.parse((String) value) : (LocalDateTime) value, LocalDateTime.now()));
            break;
          case ARRAY_INTEGER:
            properties.add(new Property<>(propName, BLOB, (long[]) value, LocalDateTime.now()));
            break;
          case ARRAY_NUMBER:
            properties.add(new Property<>(propName, BLOB, (double[]) value, LocalDateTime.now()));
            break;
          case ARRAY_STRING:
            properties.add(new Property<>(propName, BLOB, (String[]) value, LocalDateTime.now()));
            break;
          case BLOB:
            properties.add(new Property<>(propName, BLOB, (byte[]) value, LocalDateTime.now()));
            break;
        }
      }
    }
    Thing thing = new Thing(this.tid, schema.getName(), properties, LocalDateTime.now());
    if (!thing.validate(schema)) {
      throw new IllegalStateException("Thing " + thing + " validation failed");
    }
    return thing;
  }

  @Override
  public String toString() {
    return "ZigbeeThing{" +
        "sid='" + sid + '\'' +
        ", gid='" + gid + '\'' +
        ", tid='" + tid + '\'' +
        ", model='" + model + '\'' +
        ", shortId='" + shortId + '\'' +
        '}';
  }
}
