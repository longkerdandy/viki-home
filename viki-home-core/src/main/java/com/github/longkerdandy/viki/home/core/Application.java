package com.github.longkerdandy.viki.home.core;

import com.github.longkerdandy.viki.home.addon.ProtocolAddOn;
import com.github.longkerdandy.viki.home.addon.ProtocolAddOnFactory;
import com.github.longkerdandy.viki.home.storage.Storage;
import com.github.longkerdandy.viki.home.storage.StorageFactory;
import java.util.Optional;
import java.util.ServiceLoader;
import javax.sql.DataSource;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application Entrance
 */
public class Application {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) throws Exception {

    logger.info("Loading configurations ...");
    // load configurations
    FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
        new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class);
    PropertiesConfiguration config =
        getPropertiesConfiguration(builder, "config/viki-home.properties");

    logger.info("Initializing storage ...");
    // load and init storage
    Optional<StorageFactory> storageFactory = ServiceLoader.load(StorageFactory.class).findFirst();
    if (storageFactory.isEmpty()) {
      throw new AssertionError("No storage module present");
    }
    Storage storage = storageFactory.get().createStorage(config);
    DataSource ds = storage.getDataSource();

    logger.info("Migrate storage to new version if necessary ...");
    // storage migration
    Flyway flyway = Flyway
        .configure()
        .dataSource(ds)
        .locations(config.getString("storage.migration.scripts"))
        .encoding(config.getString("storage.migration.encoding", "UTF-8"))
        .load();
    flyway.migrate();

    logger.info("Loading protocol add-ons ...");
    // load and init protocol add-ons
    Iterable<ProtocolAddOnFactory> addOnFactories = ServiceLoader.load(ProtocolAddOnFactory.class);
    //List<ProtocolAddOn> addOns = new ArrayList<>();
    for (ProtocolAddOnFactory addOnFactory : addOnFactories) {
      ProtocolAddOn addOn = addOnFactory.create(config, storage);
      addOn.init();
      //addOns.add(addOn);
    }

    logger.info("Initialization complete, V.I.K.I Home is up and running.");
  }

  /**
   * Create a new PropertiesConfiguration based on the given file name
   *
   * @param builder FileBasedConfigurationBuilder
   * @param fileName Properties File Name
   * @return PropertiesConfiguration
   * @throws ConfigurationException Error loading properties file
   */
  public static PropertiesConfiguration getPropertiesConfiguration(
      FileBasedConfigurationBuilder<PropertiesConfiguration> builder, String fileName)
      throws ConfigurationException {
    Parameters params = new Parameters();
    return builder.configure(params.properties()
        .setFileName(fileName)
        .setListDelimiterHandler(new DefaultListDelimiterHandler(',')))
        .getConfiguration();
  }
}
