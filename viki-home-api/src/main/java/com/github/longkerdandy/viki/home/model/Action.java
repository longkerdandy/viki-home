package com.github.longkerdandy.viki.home.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.longkerdandy.viki.home.schema.ActionSchema;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

/**
 * Action
 */
public class Action {

  protected final String name;                    // developer friendly name
  protected final Property[] inputs;              // inputs
  protected final Property[] outputs;             // outputs
  protected final LocalDateTime createdAt;        // timestamp

  /**
   * Constructor
   *
   * @param name developer friendly name
   * @param inputs inputs
   * @param outputs outputs
   * @param createdAt timestamp
   */
  @JsonCreator
  public Action(@JsonProperty("name") String name,
      @JsonProperty("inputs") Property[] inputs,
      @JsonProperty("outputs") Property[] outputs,
      @JsonProperty("createdAt") LocalDateTime createdAt) {
    this.name = name;
    this.inputs = inputs;
    this.outputs = outputs;
    this.createdAt = createdAt;
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

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  @Override
  public String toString() {
    return "Action{" +
        "name='" + name + '\'' +
        ", inputs=" + Arrays.toString(inputs) +
        ", outputs=" + Arrays.toString(outputs) +
        ", createdAt=" + createdAt +
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

    if (StringUtils.isEmpty(name)) {
      return false;
    }

    if (createdAt == null) {
      return false;
    }

    if (inputs != null) {
      for (Property property : inputs) {
        if (!property.validate(actionSchema.getInputs().get(property.getName()))) {
          return false;
        }
      }
    }

    if (outputs != null) {
      for (Property property : outputs) {
        if (!property.validate(actionSchema.getOutputs().get(property.getName()))) {
          return false;
        }
      }
    }

    return true;
  }
}
