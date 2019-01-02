package com.github.longkerdandy.viki.home.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.longkerdandy.viki.home.schema.EventSchema;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

/**
 * Event
 */
public class Event {

  protected final String name;                    // developer friendly name
  protected final Property[] outputs;             // outputs
  protected final LocalDateTime createdAt;        // timestamp

  /**
   * Constructor
   *
   * @param name developer friendly name
   * @param outputs outputs
   * @param createdAt timestamp
   */
  @JsonCreator
  public Event(@JsonProperty("name") String name,
      @JsonProperty("outputs") Property[] outputs,
      @JsonProperty("createdAt") LocalDateTime createdAt) {
    this.name = name;
    this.outputs = outputs;
    this.createdAt = createdAt;
  }

  public String getName() {
    return name;
  }

  public Property[] getOutputs() {
    return outputs;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  @Override
  public String toString() {
    return "Event{" +
        "name='" + name + '\'' +
        ", outputs=" + Arrays.toString(outputs) +
        ", createdAt=" + createdAt +
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

    if (StringUtils.isEmpty(name)) {
      return false;
    }

    if (createdAt == null) {
      return false;
    }

    if (outputs != null) {
      for (Property property : outputs) {
        if (!property.validate(eventSchema.getOutputs().get(property.getName()))) {
          return false;
        }
      }
    }

    return true;
  }
}
