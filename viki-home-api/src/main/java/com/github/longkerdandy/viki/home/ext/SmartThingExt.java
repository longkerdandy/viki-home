package com.github.longkerdandy.viki.home.ext;

import com.github.longkerdandy.viki.home.model.Action;
import com.github.longkerdandy.viki.home.model.Property;
import com.github.longkerdandy.viki.home.model.WriteResult;
import com.github.longkerdandy.viki.home.schema.ThingSchema;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Extension for smart things
 */
public interface SmartThingExt {

  /**
   * Get the extension name
   *
   * @return Extension Name
   */
  String getExtName();

  /**
   * Initialize
   */
  void init() throws Exception;

  /**
   * Destroy
   */
  void destroy() throws Exception;

  /**
   * Get the Map of name & {@link ThingSchema}
   *
   * @return Map of name & {@link ThingSchema}
   */
  Map<String, ThingSchema> getSchemas();

  /**
   * Perform {@link Action} on target Thing
   *
   * {@link Action} will be validated before calling this method. {@link Action} 's outputs will be
   * set when method returned.
   *
   * @param thing Thing id
   * @param schema ThingSchema name
   * @param action {@link Action} with inputs
   * @return {@link WriteResult} and {@link Action} with outputs
   */
  CompletableFuture<WriteResult> performAction(String thing, String schema, Action action);

  /**
   * Write {@link Property} on target Thing
   *
   * {@link Property} will be validated before calling this method.
   *
   * @param thing Thing id
   * @param schema ThingSchema name
   * @param property {@link Property}
   * @return {@link WriteResult}
   */
  CompletableFuture<WriteResult> writeProperty(String thing, String schema, Property property);
}
