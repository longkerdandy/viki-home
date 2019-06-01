package com.github.longkerdandy.viki.home.core;

import static com.github.longkerdandy.viki.home.util.Configurations.getPropertiesConfiguration;

import com.github.longkerdandy.viki.home.ext.ControllerExt;
import com.github.longkerdandy.viki.home.ext.ControllerExtFactory;
import com.github.longkerdandy.viki.home.ext.SmartThingExt;
import com.github.longkerdandy.viki.home.ext.SmartThingExtFactory;
import com.github.longkerdandy.viki.home.storage.SQLiteStorage;
import java.util.Locale;
import java.util.ServiceLoader;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application Entrance
 */
public class Application {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {

    try {
      logger.info("Loading configurations ...");
      // load configurations
      PropertiesConfiguration config = getPropertiesConfiguration("config/viki-home.properties");
      Locale locale = Locale.forLanguageTag(config.getString("locale", "zh-CN"));

      logger.info("Initializing storage ...");
      // load and init storage
      SQLiteStorage storage = new SQLiteStorage(config);

      logger.info("Migrate storage to new version if necessary ...");
      // storage migration
      Flyway.configure()
          .dataSource(storage.getDataSource())
          .locations(config.getString("storage.migration.scripts"))
          .encoding(config.getString("storage.migration.encoding", "UTF-8"))
          .load()
          .migrate();

      logger.info("Loading smart thing extensions ...");
      // load and init extensions
      Iterable<SmartThingExtFactory> thingExtFactories = ServiceLoader
          .load(SmartThingExtFactory.class);
      for (SmartThingExtFactory thingExtFactory : thingExtFactories) {
        SmartThingExt thingExt = thingExtFactory.create(locale, storage);
        thingExt.init();
      }

      logger.info("Loading controller extensions ...");
      // load and init extensions
      Iterable<ControllerExtFactory> controllerExtFactories = ServiceLoader
          .load(ControllerExtFactory.class);
      for (ControllerExtFactory controllerExtFactory : controllerExtFactories) {
        ControllerExt controllerExt = controllerExtFactory.create(locale, storage);
        controllerExt.init();
      }

      logger.info("Initialization complete, V.I.K.I Home is up and running.");
    } catch (Exception e) {
      logger.error("Error happened when application running:", e);
    }
  }
}
