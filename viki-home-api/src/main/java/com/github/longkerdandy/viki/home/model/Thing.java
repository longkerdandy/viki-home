package com.github.longkerdandy.viki.home.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.longkerdandy.viki.home.schema.ThingSchema;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

/**
 * Thing
 */
public class Thing {

  protected final String id;                                // thing identifier
  protected final String schema;                            // schema identifier
  protected final String name;                              // developer friendly name
  protected final Property[] properties;                    // properties
  protected final Action[] actions;                         // actions
  protected final Event[] events;                           // events

  /**
   * Constructor
   *
   * @param id thing identifier
   * @param schema schema identifier
   * @param name developer friendly name
   * @param properties properties
   * @param actions actions
   * @param events events
   */
  @JsonCreator
  public Thing(@JsonProperty("id") String id,
      @JsonProperty("schema") String schema,
      @JsonProperty("name") String name,
      @JsonProperty("properties") Property[] properties,
      @JsonProperty("actions") Action[] actions,
      @JsonProperty("events") Event[] events) {
    this.id = id;
    this.schema = schema;
    this.name = name;
    this.properties = properties;
    this.actions = actions;
    this.events = events;
  }

  public String getId() {
    return id;
  }

  public String getSchema() {
    return schema;
  }

  public String getName() {
    return name;
  }

  public Property[] getProperties() {
    return properties;
  }

  public Action[] getActions() {
    return actions;
  }

  public Event[] getEvents() {
    return events;
  }

  @Override
  public String toString() {
    return "Thing{" +
        "id='" + id + '\'' +
        ", schema='" + schema + '\'' +
        ", name='" + name + '\'' +
        ", properties=" + Arrays.toString(properties) +
        ", actions=" + Arrays.toString(actions) +
        ", events=" + Arrays.toString(events) +
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

    if (StringUtils.isEmpty(this.schema) || !this.schema.equals(thingSchema.getId())) {
      return false;
    }

    if (StringUtils.isEmpty(name)) {
      return false;
    }

    if (properties != null) {
      for (Property property : properties) {
        if (!property.validate(thingSchema.getProperties().get(property.getName()))) {
          return false;
        }
      }
    }

    if (actions != null) {
      for (Action action : actions) {
        if (!action.validate(thingSchema.getActions().get(action.getName()))) {
          return false;
        }
      }
    }

    if (events != null) {
      for (Event event : events) {
        if (event.validate(thingSchema.getEvents().get(event.getName()))) {
          return false;
        }
      }
    }

    return true;
  }
}
