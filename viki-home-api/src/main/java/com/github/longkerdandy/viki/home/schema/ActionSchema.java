package com.github.longkerdandy.viki.home.schema;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Action Schema
 */
public class ActionSchema {

  protected final String name;                    // developer friendly name
  protected final ResourceBundle resources;       // localized label and description

  @JsonInclude(NON_EMPTY)
  protected Map<String, PropertySchema> inputs;   // input arguments
  @JsonInclude(NON_EMPTY)
  protected Map<String, PropertySchema> outputs;  // output arguments

  /**
   * Constructor
   *
   * @param name developer friendly name
   * @param resources {@link ResourceBundle}
   */
  protected ActionSchema(String name, ResourceBundle resources) {
    this.name = name;
    this.resources = resources;
  }

  /**
   * Create ActionSchema
   *
   * @param name developer friendly name
   * @param resources {@link ResourceBundle}
   * @param inputs input arguments
   * @param outputs output arguments
   * @return ActionSchema
   */
  public static ActionSchema create(String name, ResourceBundle resources,
      Map<String, PropertySchema> inputs, Map<String, PropertySchema> outputs) {
    ActionSchema action = new ActionSchema(name, resources);
    action.inputs = inputs;
    action.outputs = outputs;
    return action;
  }

  /**
   * Get localized label
   *
   * @return label
   */
  public String getLabel() {
    return resources.getString("action." + name + ".label");
  }

  /**
   * Get localized description
   *
   * @return description
   */
  public String getDescription() {
    return resources.getString("action." + name + ".description");
  }

  public String getName() {
    return name;
  }

  public Map<String, PropertySchema> getInputs() {
    return inputs;
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
        ", inputs=" + inputs +
        ", outputs=" + outputs +
        '}';
  }
}
