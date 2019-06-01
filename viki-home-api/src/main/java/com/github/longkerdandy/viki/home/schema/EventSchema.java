package com.github.longkerdandy.viki.home.schema;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;

/**
 * Event Schema
 */
public class EventSchema {

  protected final String name;                    // event name
  protected String function = "default";          // function that belong to

  @JsonInclude(NON_EMPTY)
  protected List<PropertySchema> outputs;         // output arguments

  /**
   * Constructor
   *
   * @param name event name
   */
  @JsonCreator
  protected EventSchema(@JsonProperty("name") String name) {
    this.name = name;
  }

  /**
   * Create EventSchema
   *
   * @param name event name
   * @param outputs output arguments
   * @return EventSchema
   */
  public static EventSchema create(String name, List<PropertySchema> outputs) {
    EventSchema action = new EventSchema(name);
    action.outputs = outputs;
    return action;
  }

  public String getName() {
    return name;
  }

  public String getFunction() {
    return function;
  }

  public List<PropertySchema> getOutputs() {
    return outputs;
  }

  public EventSchema function(String function) {
    this.function = function;
    return this;
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
    return "EventSchema{" +
        "name='" + name + '\'' +
        ", function='" + function + '\'' +
        ", outputs=" + outputs +
        '}';
  }
}
