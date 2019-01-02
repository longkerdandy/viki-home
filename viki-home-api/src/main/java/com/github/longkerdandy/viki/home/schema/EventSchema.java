package com.github.longkerdandy.viki.home.schema;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Event Schema
 */
public class EventSchema {

  protected final String name;                    // developer friendly name
  protected final ResourceBundle resources;       // localized label and description

  @JsonInclude(NON_EMPTY)
  protected Map<String, PropertySchema> outputs;  // output arguments

  /**
   * Constructor
   *
   * @param name developer friendly name
   * @param resources {@link ResourceBundle}
   */
  protected EventSchema(String name, ResourceBundle resources) {
    this.name = name;
    this.resources = resources;
  }

  /**
   * Create EventSchema
   *
   * @param name developer friendly name
   * @param resources {@link ResourceBundle}
   * @param outputs output arguments
   * @return EventSchema
   */
  public static EventSchema create(String name, ResourceBundle resources,
      Map<String, PropertySchema> outputs) {
    EventSchema action = new EventSchema(name, resources);
    action.outputs = outputs;
    return action;
  }

  /**
   * Get localized label
   *
   * @return label
   */
  public String getLabel() {
    return resources.getString("event." + name + ".label");
  }

  /**
   * Get localized description
   *
   * @return description
   */
  public String getDescription() {
    return resources.getString("event." + name + ".description");
  }

  public String getName() {
    return name;
  }

  public Map<String, PropertySchema> getOutputs() {
    return outputs;
  }

  @Override
  public String toString() {
    return "ActionSchema{" +
        "name='" + name + '\'' +
        ", label='" + getLabel() + '\'' +
        ", description='" + getDescription() + '\'' +
        ", outputs=" + outputs +
        '}';
  }
}
