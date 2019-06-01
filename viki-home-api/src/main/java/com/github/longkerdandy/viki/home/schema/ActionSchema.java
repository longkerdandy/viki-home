package com.github.longkerdandy.viki.home.schema;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;

/**
 * Action Schema
 */
public class ActionSchema {

  protected final String name;                    // action name
  protected String function = "default";          // function that belong to

  @JsonInclude(NON_EMPTY)
  protected List<PropertySchema> inputs;          // input arguments
  @JsonInclude(NON_EMPTY)
  protected List<PropertySchema> outputs;         // output arguments

  /**
   * Constructor
   *
   * @param name action name
   */
  @JsonCreator
  protected ActionSchema(@JsonProperty("name") String name) {
    this.name = name;
  }

  /**
   * Create ActionSchema
   *
   * @param name action name
   * @param inputs input arguments
   * @param outputs output arguments
   * @return ActionSchema
   */
  public static ActionSchema create(String name, List<PropertySchema> inputs,
      List<PropertySchema> outputs) {
    ActionSchema action = new ActionSchema(name);
    action.inputs = inputs;
    action.outputs = outputs;
    return action;
  }

  public String getName() {
    return name;
  }

  public String getFunction() {
    return function;
  }

  public List<PropertySchema> getInputs() {
    return inputs;
  }

  public List<PropertySchema> getOutputs() {
    return outputs;
  }

  public ActionSchema function(String function) {
    this.function = function;
    return this;
  }

  /**
   * Get input {@link PropertySchema} based on its name
   *
   * @param name schema name
   * @return Optional {@link PropertySchema}
   */
  public Optional<PropertySchema> getInputByName(String name) {
    return inputs.stream().filter(p -> p.getName().equals(name)).findFirst();
  }

  /**
   * Get output {@link PropertySchema} based on its name
   *
   * @param name schema name
   * @return Optional {@link PropertySchema}
   */
  public Optional<PropertySchema> getOutputByName(String name) {
    return outputs.stream().filter(p -> p.getName().equals(name)).findFirst();
  }

  @Override
  public String toString() {
    return "ActionSchema{" +
        "name='" + name + '\'' +
        ", function='" + function + '\'' +
        ", inputs=" + inputs +
        ", outputs=" + outputs +
        '}';
  }
}
