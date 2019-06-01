package com.github.longkerdandy.viki.home.mi.schema;

import com.github.longkerdandy.viki.home.model.Action;
import com.github.longkerdandy.viki.home.model.Property;
import com.github.longkerdandy.viki.home.schema.ThingSchema;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

/**
 * Mapping between {@link ThingSchema} definition and MiJia Gateway Protocol
 */
public class SchemaMapping {

  private static BidiMap<String, String> MIJIA_GATEWAY = new DualHashBidiMap<>(Map.of(
      "channel", "mid"
  ));

  private static BidiMap<String, String> AQARA_WALLSWITCH1 = new DualHashBidiMap<>(Map.of(
      "status", "channel_0"
  ));

  private static BidiMap<String, String> AQARA_WALLSWITCH2 = new DualHashBidiMap<>(Map.of(
      "status_left", "channel_0",
      "status_right", "channel_1"
  ));

  private static Map<String, BidiMap<String, String>> MODEL = Map.of(
      "gateway", MIJIA_GATEWAY,
      "ctrl_neutral1", AQARA_WALLSWITCH1,
      "ctrl_neutral2", AQARA_WALLSWITCH2,
      "ctrl_ln1", AQARA_WALLSWITCH1,
      "ctrl_ln1.aq1", AQARA_WALLSWITCH1,
      "ctrl_ln2", AQARA_WALLSWITCH2,
      "ctrl_ln2.aq1", AQARA_WALLSWITCH2
  );

  /**
   * Decode MIJIA_GATEWAY
   */
  protected static Map<String, Object> decodeMiJiaGateway(String model,
      Map<String, Object> params) {
    Map<String, Object> properties = new HashMap<>();
    for (String paramName : params.keySet()) {
      String propName = MODEL.get(model).getKey(paramName);
      propName = propName != null ? propName : paramName;
      if (paramName.equals("rgb")) {
        long value = (long) params.get(paramName);
        long R = (0x00FF0000 & value) >> 16;
        long G = (0x0000FF00 & value) >> 8;
        long B = 0x000000FF & value;
        properties.put(propName, new long[]{R, G, B});
      }
    }
    return properties;
  }

  /**
   * Encode MIJIA_GATEWAY
   */
  protected static Map<String, Object> encodeMiJiaGateway(String model,
      Map<String, Object> properties) {
    Map<String, Object> params = new HashMap<>();
    for (String propName : properties.keySet()) {
      String paramName = MODEL.get(model).getOrDefault(propName, propName);
      if (propName.equals("rgb")) {
        long[] RGB = (long[]) properties.get(propName);
        long value = 0xFF000000L | (RGB[0] << 16) | (RGB[1] << 8) | RGB[2];
        params.put(paramName, value == 0xFF000000L ? 0L : value);
      } else if (propName.equals("channel")) {
        params.put(paramName, properties.get(propName));
      }
    }
    return params;
  }

  /**
   * Decode AQARA_WALLSWITCH1 & AQARA_WALLSWITCH2
   */
  protected static Map<String, Object> decodeAqaraWallSwitch(String model,
      Map<String, Object> params) {
    Map<String, Object> properties = new HashMap<>();
    for (String paramName : params.keySet()) {
      String propName = MODEL.get(model).getKey(paramName);
      propName = propName != null ? propName : paramName;
      if (List.of("channel_0", "channel_1").contains(paramName)) {
        properties.put(propName, params.get(paramName));
      }
    }
    return properties;
  }

  /**
   * Encode AQARA_WALLSWITCH1 & AQARA_WALLSWITCH2
   */
  protected static Map<String, Object> encodeAqaraWallSwitch(String model,
      Map<String, Object> properties) {
    Map<String, Object> params = new HashMap<>();
    for (String propName : properties.keySet()) {
      String paramName = MODEL.get(model).getOrDefault(propName, propName);
      if (List.of("status", "status_left", "status_right").contains(propName)) {
        params.put(paramName, properties.get(propName));
      }
    }
    return params;
  }

  /**
   * Convert parameters to properties
   *
   * @param model name
   * @param params name value pairs
   * @return properties
   */
  public static Map<String, Object> paramToProp(String model, Map<String, Object> params) {
    switch (model) {
      case "gateway":
        return decodeMiJiaGateway(model, params);
      case "ctrl_neutral1":
      case "ctrl_neutral2":
      case "ctrl_ln1":
      case "ctrl_ln1.aq1":
      case "ctrl_ln2":
      case "ctrl_ln2.aq1":
        return decodeAqaraWallSwitch(model, params);
      default:
        throw new IllegalArgumentException("unknown model " + model);
    }
  }

  /**
   * Convert properties to parameters
   *
   * @param model name
   * @param properties name value pairs
   * @return parameters
   */
  public static Map<String, Object> propToParam(String model, Map<String, Object> properties) {
    switch (model) {
      case "gateway":
        return encodeMiJiaGateway(model, properties);
      case "ctrl_neutral1":
      case "ctrl_neutral2":
      case "ctrl_ln1":
      case "ctrl_ln1.aq1":
      case "ctrl_ln2":
      case "ctrl_ln2.aq1":
        return encodeAqaraWallSwitch(model, properties);
      default:
        throw new IllegalArgumentException("unknown model " + model);
    }
  }

  /**
   * Convert action to parameters
   *
   * @param model name
   * @param action {@link Action}
   * @return parameters
   */
  public static Map<String, Object> actionToParam(String model, Action action) {
    Map<String, Object> properties = new HashMap<>();
    for (Property property : action.getInputs()) {
      properties.put(property.getName(), property.getValue());
    }
    return propToParam(model, properties);
  }

  /**
   * Convert property to parameter
   *
   * @param model name
   * @param property {@link Property}
   * @return parameters
   */
  public static Map<String, Object> propToParam(String model, Property property) {
    Map<String, Object> properties = new HashMap<>();
    properties.put(property.getName(), property.getValue());
    return propToParam(model, properties);
  }
}
