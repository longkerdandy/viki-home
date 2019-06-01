package com.github.longkerdandy.viki.home.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.longkerdandy.viki.home.schema.EventSchema;
import com.github.longkerdandy.viki.home.schema.PropertySchema;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * Event
 */
public class Event {

  protected final String thing;                   // thing id
  protected final String name;                    // developer friendly name
  protected final Property[] outputs;             // outputs
  protected final LocalDateTime occurredAt;       // timestamp

  /**
   * Constructor
   *
   * @param thing thing id
   * @param name developer friendly name
   * @param outputs outputs
   * @param occurredAt timestamp
   */
  @JsonCreator
  public Event(@JsonProperty("thing") String thing, @JsonProperty("name") String name,
      @JsonProperty("outputs") Property[] outputs,
      @JsonProperty("occurredAt") LocalDateTime occurredAt) {
    this.thing = thing;
    this.name = name;
    this.outputs = outputs;
    this.occurredAt = occurredAt;
  }

  public String getThing() {
    return thing;
  }

  public String getName() {
    return name;
  }

  public Property[] getOutputs() {
    return outputs;
  }

  public LocalDateTime getOccurredAt() {
    return occurredAt;
  }

  @Override
  public String toString() {
    return "Event{" +
        "thing='" + thing + '\'' +
        ", name='" + name + '\'' +
        ", outputs=" + Arrays.toString(outputs) +
        ", occurredAt=" + occurredAt +
        '}';
  }

  /**
   * Validate the Event
   *
   * @param eventSchema {@link EventSchema}
   * @return True if Event is valid
   */
  public boolean validate(EventSchema eventSchema) {
    if (eventSchema == null) {
      return false;
    }

    if (StringUtils.isEmpty(thing)) {
      return false;
    }

    if (StringUtils.isEmpty(name)) {
      return false;
    }

    if (occurredAt == null) {
      return false;
    }

    if (outputs != null) {
      for (Property property : outputs) {
        Optional<PropertySchema> propertySchema = eventSchema.getOutputByName(property.getName());
        if (propertySchema.isEmpty() || !property.validate(propertySchema.get())) {
          return false;
        }
      }
    }

    return true;
  }
}
