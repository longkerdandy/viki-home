package com.github.longkerdandy.viki.home.util;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * Configuration Util
 */
public class Configurations {

  private Configurations() {
  }

  /**
   * Create a new PropertiesConfiguration based on the given file name
   *
   * @param fileName Properties File Name
   * @return PropertiesConfiguration
   * @throws ConfigurationException Error loading properties file
   */
  public static PropertiesConfiguration getPropertiesConfiguration(String fileName)
      throws ConfigurationException {
    FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
        new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class);
    Parameters params = new Parameters();
    return builder.configure(params.properties()
        .setFileName(fileName)
        .setListDelimiterHandler(new DefaultListDelimiterHandler(',')))
        .getConfiguration();
  }
}
