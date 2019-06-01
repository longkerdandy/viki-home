package com.github.longkerdandy.viki.home.util;

import java.util.Properties;
import org.apache.commons.configuration2.AbstractConfiguration;

/**
 * SQLite Utils
 */
public class SQLites {

  private static final String PREFIX = "storage.sqlite.pragma";

  private SQLites() {
  }

  /**
   * Parse and return SQLite configuration options.
   *
   * @param config Configuration
   * @return SQLite Configuration
   * @see <a href="http://www.sqlite.org/pragma.html">http://www.sqlite.org/pragma.html</a>
   */
  public static Properties parseSQLitePragma(AbstractConfiguration config) {
    return parseSQLitePragma(config, PREFIX);
  }

  /**
   * Parse and return SQLite configuration options.
   *
   * @param config Configuration
   * @param prefix SQLite Pragma configuration prefix
   * @return SQLite Pragma properties
   * @see <a href="http://www.sqlite.org/pragma.html">http://www.sqlite.org/pragma.html</a>
   */
  public static Properties parseSQLitePragma(AbstractConfiguration config, String prefix) {
    Properties prop = new Properties();
    config.getKeys(prefix).forEachRemaining(
        key -> prop.put(key.substring(prefix.length() + 1), config.getString(key)));
    return prop;
  }
}
