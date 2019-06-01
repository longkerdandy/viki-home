package com.github.longkerdandy.viki.home.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.longkerdandy.viki.home.schema.PropertySchema;
import com.github.longkerdandy.viki.home.schema.ThingSchema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * Thing
 */
public class Thing {

  protected final String id;                                // thing identifier
  protected final String schema;                            // schema name
  protected final List<Property> properties;                // properties
  protected final LocalDateTime heartbeat;                  // timestamp

  /**
   * Constructor
   *
   * @param id thing identifier
   * @param schema schema name
   * @param properties properties
   * @param heartbeat timestamp
   */
  @JsonCreator
  public Thing(@JsonProperty("id") String id, @JsonProperty("schema") String schema,
      @JsonProperty("properties") List<Property> properties,
      @JsonProperty("heartbeat") LocalDateTime heartbeat) {
    this.id = id;
    this.schema = schema;
    this.properties = properties;
    this.heartbeat = heartbeat;
  }

  public String getId() {
    return id;
  }

  public String getSchema() {
    return schema;
  }

  public List<Property> getProperties() {
    return properties;
  }

  public LocalDateTime getHeartbeat() {
    return heartbeat;
  }

  @Override
  public String toString() {
    return "Thing{" +
        "id='" + id + '\'' +
        ", schema='" + schema + '\'' +
        ", properties=" + properties +
        '}';
  }

  /**
   * Validate the Thing
   *
   * @param thingSchema {@link ThingSchema}
   * @return True if Thing is valid
   */
  public boolean validate(ThingSchema thingSchema) {
    if (thingSchema == null) {
      return false;
    }

    if (StringUtils.isEmpty(id)) {
      return false;
    }

    if (StringUtils.isEmpty(this.schema) || !this.schema.equals(thingSchema.getName())) {
      return false;
    }

    if (heartbeat == null) {
      return false;
    }

    if (properties != null) {
      for (Property property : properties) {
        Optional<PropertySchema> propertySchema = thingSchema.getPropertyByName(property.getName());
        if (propertySchema.isEmpty() || !property.validate(propertySchema.get())) {
          return false;
        }
      }
    }

    return true;
  }
}
