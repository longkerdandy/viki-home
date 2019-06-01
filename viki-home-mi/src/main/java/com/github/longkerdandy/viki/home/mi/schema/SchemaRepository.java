package com.github.longkerdandy.viki.home.mi.schema;

import com.github.longkerdandy.viki.home.schema.ThingSchema;
import com.github.longkerdandy.viki.home.util.Jacksons;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link ThingSchema} definitions repository
 */
public class SchemaRepository {

  // List of resource name
  private static final List<String> NAMES = List.of(
      "MiJiaGateway",
      "AqaraWallSwitch1",
      "AqaraWallSwitch2"
  );

  private static Map<String, String> MODEL = Map.of(
      "gateway", "mi:gateway:multifunction_gateway",
      "ctrl_neutral1", "aqara:zigbee:1_button_wall_switch",
      "ctrl_neutral2", "aqara:zigbee:2_button_wall_switch",
      "ctrl_ln1", "aqara:zigbee:1_button_wall_switch",
      "ctrl_ln1.aq1", "aqara:zigbee:1_button_wall_switch",
      "ctrl_ln2", "aqara:zigbee:2_button_wall_switch",
      "ctrl_ln2.aq1", "aqara:zigbee:2_button_wall_switch"
  );

  private final Locale locale;
  private final String schemaPath;
  private final String i18nPath;
  private final Map<String, ThingSchema> repository;

  /**
   * Constructor
   *
   * @param locale {@link Locale}
   */
  public SchemaRepository(Locale locale, String schemaPath, String i18nPath) {
    this.locale = locale;
    this.schemaPath = schemaPath.endsWith("/") ? schemaPath : schemaPath + "/";
    this.i18nPath = i18nPath.endsWith("/") ? i18nPath : i18nPath + "/";
    this.repository = new ConcurrentHashMap<>();
  }

  /**
   * Load {@link ThingSchema} from resources
   *
   * @throws IOException when failed to parse definition files
   */
  public void load() throws IOException {
    File i18n = new File(this.i18nPath);
    URL[] urls = {i18n.toURI().toURL()};
    ClassLoader classLoader = new URLClassLoader(urls);
    for (String name : NAMES) {
      try (InputStream stream = new FileInputStream(this.schemaPath + name + ".json")) {
        ThingSchema schema = Jacksons.getReader(ThingSchema.class).readValue(stream);
        ResourceBundle bundle = ResourceBundle.getBundle(name, this.locale, classLoader);
        this.repository.put(schema.getName(), schema.resourceBundle(bundle));
      }
    }
  }

  /**
   * Get the Map of name & {@link ThingSchema}
   *
   * @return Map of name & {@link ThingSchema}
   */
  public Map<String, ThingSchema> getSchemas() {
    return repository;
  }

  /**
   * Get {@link ThingSchema} by its name
   *
   * @param name Name
   * @return {@link ThingSchema}
   */
  public ThingSchema getSchemaByName(String name) {
    return this.repository.get(name);
  }

  /**
   * Get {@link ThingSchema} by its model
   *
   * @param model Model
   * @return {@link ThingSchema}
   */
  public ThingSchema getSchemaByModel(String model) {
    return this.repository.get(MODEL.get(model));
  }
}
