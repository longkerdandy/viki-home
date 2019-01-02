package com.github.longkerdandy.viki.home.schema;

import java.util.Map;
import java.util.ResourceBundle;

/**
 * Thing Schema
 */
public class ThingSchema {

  protected final String id;                                // schema identifier
  protected final String name;                              // developer friendly name
  protected final ResourceBundle resources;                 // localized label and description
  protected final Map<String, PropertySchema> properties;   // properties
  protected final Map<String, ActionSchema> actions;        // actions
  protected final Map<String, EventSchema> events;          // events

  /**
   * Constructor
   *
   * @param id schema identifier
   * @param name developer friendly name
   * @param resources {@link ResourceBundle}
   * @param properties properties
   * @param actions actions
   * @param events events
   */
  public ThingSchema(String id, String name, ResourceBundle resources,
      Map<String, PropertySchema> properties,
      Map<String, ActionSchema> actions,
      Map<String, EventSchema> events) {
    this.id = id;
    this.name = name;
    this.resources = resources;
    this.properties = properties;
    this.actions = actions;
    this.events = events;
  }

  /**
   * Get localized label
   *
   * @return label
   */
  public String getLabel() {
    return resources.getString(name + ".label");
  }

  /**
   * Get localized description
   *
   * @return description
   */
  public String getDescription() {
    return resources.getString(name + ".description");
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Map<String, PropertySchema> getProperties() {
    return properties;
  }

  public Map<String, ActionSchema> getActions() {
    return actions;
  }

  public Map<String, EventSchema> getEvents() {
    return events;
  }

  @Override
  public String toString() {
    return "ThingSchema{" +
        "id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", label='" + getLabel() + '\'' +
        ", description='" + getDescription() + '\'' +
        ", properties=" + properties +
        ", actions=" + actions +
        ", events=" + events +
        '}';
  }
}
