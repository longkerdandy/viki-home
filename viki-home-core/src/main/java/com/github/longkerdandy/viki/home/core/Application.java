package com.github.longkerdandy.viki.home.core;

import com.github.longkerdandy.viki.home.addon.ProtocolAddOn;
import com.github.longkerdandy.viki.home.addon.ProtocolAddOnFactory;
import com.github.longkerdandy.viki.home.storage.Storage;
import com.github.longkerdandy.viki.home.storage.StorageFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import javax.sql.DataSource;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.flywaydb.core.Flyway;

/**
 * Application Entrance
 */
public class Application {

  public static void main(String[] args) throws ConfigurationException {

    // load configurations
    FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
        new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class);
    PropertiesConfiguration config =
        getPropertiesConfiguration(builder, "config/viki-home.properties");

    // load and init storage
    Optional<StorageFactory> storageFactory = ServiceLoader.load(StorageFactory.class).findFirst();
    if (!storageFactory.isPresent()) {
      throw new AssertionError("No storage module present");
    }
    Storage storage = storageFactory.get().createStorage(config);
    DataSource ds = storage.getDataSource();

    // storage migration
    Flyway flyway = new Flyway();
    flyway.setDataSource(ds);
    flyway.setLocations(config.getString("storage.migration.scripts"));
    flyway.setEncoding(config.getString("storage.migration.encoding", "UTF-8"));
    flyway.migrate();

    // load and init protocol add-ons
    Iterable<ProtocolAddOnFactory> addOnFactories = ServiceLoader.load(ProtocolAddOnFactory.class);
    List<ProtocolAddOn> addOns = new ArrayList<>();
    for (ProtocolAddOnFactory addOnFactory : addOnFactories) {
      ProtocolAddOn addOn = addOnFactory.create(config, storage);
      addOn.init();
      addOns.add(addOn);
    }
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
