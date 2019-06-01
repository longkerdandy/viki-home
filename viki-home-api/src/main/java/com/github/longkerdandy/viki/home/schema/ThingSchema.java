package com.github.longkerdandy.viki.home.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Thing Schema
 */
public class ThingSchema {

  protected final String name;                              // schema name
  protected final Map<String, String> functions;            // functions
  protected final List<PropertySchema> properties;          // property schemas
  protected final List<ActionSchema> actions;               // action schemas
  protected final List<EventSchema> events;                 // event schemas

  protected ResourceBundle resources;                       // i18n resources

  /**
   * Constructor
   *
   * @param name schema name
   * @param properties property schemas
   * @param actions action schemas
   * @param events event schemas
   */
  @JsonCreator
  public ThingSchema(@JsonProperty("name") String name,
      @JsonProperty("functions") Map<String, String> functions,
      @JsonProperty("properties") List<PropertySchema> properties,
      @JsonProperty("actions") List<ActionSchema> actions,
      @JsonProperty("events") List<EventSchema> events) {
    this.name = name;
    this.functions = functions;
    this.properties = properties;
    this.actions = actions;
    this.events = events;
  }

  public String getName() {
    return name;
  }

  public Map<String, String> getFunctions() {
    return functions;
  }

  public List<PropertySchema> getProperties() {
    return properties;
  }

  public List<ActionSchema> getActions() {
    return actions;
  }

  public List<EventSchema> getEvents() {
    return events;
  }

  public ResourceBundle getResources() {
    return resources;
  }

  public ThingSchema resourceBundle(ResourceBundle resources) {
    this.resources = resources;
    return this;
  }

  /**
   * Get {@link PropertySchema} based on its name
   *
   * @param name schema name
   * @return Optional {@link PropertySchema}
   */
  public Optional<PropertySchema> getPropertyByName(String name) {
    return this.properties.stream().filter(p -> p.getName().equals(name)).findFirst();
  }

  /**
   * Get {@link ActionSchema} based on its name
   *
   * @param name schema name
   * @return Optional {@link ActionSchema}
   */
  public Optional<ActionSchema> getActionByName(String name) {
    return this.actions.stream().filter(a -> a.getName().equals(name)).findFirst();
  }

  /**
   * Get {@link EventSchema} based on its name
   *
   * @param name schema name
   * @return Optional {@link EventSchema}
   */
  public Optional<EventSchema> getEventByName(String name) {
    return this.events.stream().filter(e -> e.getName().equals(name)).findFirst();
  }

  @Override
  public String toString() {
    return "ThingSchema{" +
        "name='" + name + '\'' +
        ", functions=" + functions +
        ", properties=" + properties +
        ", actions=" + actions +
        ", events=" + events +
        ", resources=" + resources +
        '}';
  }
}
