package com.github.longkerdandy.viki.home.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.longkerdandy.viki.home.schema.ActionSchema;
import com.github.longkerdandy.viki.home.schema.PropertySchema;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * Action
 */
public class Action {

  protected final String thing;                   // thing id
  protected final String name;                    // developer friendly name
  protected final Property[] inputs;              // inputs
  protected final Property[] outputs;             // outputs
  protected final LocalDateTime occurredAt;       // timestamp

  /**
   * Constructor
   *
   * @param thing thing id
   * @param name developer friendly name
   * @param inputs inputs
   * @param outputs outputs
   * @param occurredAt timestamp
   */
  @JsonCreator
  public Action(@JsonProperty("thing") String thing, @JsonProperty("name") String name,
      @JsonProperty("inputs") Property[] inputs, @JsonProperty("outputs") Property[] outputs,
      @JsonProperty("occurredAt") LocalDateTime occurredAt) {
    this.thing = thing;
    this.name = name;
    this.inputs = inputs;
    this.outputs = outputs;
    this.occurredAt = occurredAt;
  }

  public String getThing() {
    return thing;
  }

  public String getName() {
    return name;
  }

  public Property[] getInputs() {
    return inputs;
  }

  public Property[] getOutputs() {
    return outputs;
  }

  public LocalDateTime getOccurredAt() {
    return occurredAt;
  }

  @Override
  public String toString() {
    return "Action{" +
        "thing='" + thing + '\'' +
        ", name='" + name + '\'' +
        ", inputs=" + Arrays.toString(inputs) +
        ", outputs=" + Arrays.toString(outputs) +
        ", occurredAt=" + occurredAt +
        '}';
  }

  /**
   * Validate the Action
   *
   * @param actionSchema {@link ActionSchema}
   * @return True if Action is valid
   */
  public boolean validate(ActionSchema actionSchema) {
    if (actionSchema == null) {
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

    if (inputs != null) {
      for (Property property : inputs) {
        Optional<PropertySchema> propertySchema = actionSchema.getInputByName(property.getName());
        if (propertySchema.isEmpty() || !property.validate(propertySchema.get())) {
          return false;
        }
      }
    }

    if (outputs != null) {
      for (Property property : outputs) {
        Optional<PropertySchema> propertySchema = actionSchema.getOutputByName(property.getName());
        if (propertySchema.isEmpty() || !property.validate(propertySchema.get())) {
          return false;
        }
      }
    }

    return true;
  }
}
