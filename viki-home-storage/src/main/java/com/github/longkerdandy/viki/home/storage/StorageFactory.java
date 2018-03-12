package com.github.longkerdandy.viki.home.storage;

import java.util.Properties;
import org.apache.commons.configuration2.AbstractConfiguration;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlite3.SQLitePlugin;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

/**
 * Storage Factory
 */
public class StorageFactory {

  // Jdbi Instance
  private Jdbi jdbi;

  /**
   * Constructor
   *
   * @param config Storage Configuration
   */
  public StorageFactory(AbstractConfiguration config) {
    SQLiteConfig sc = new SQLiteConfig(parseSQLitePragma(config));
    SQLiteDataSource ds = new SQLiteDataSource(sc);
    ds.setUrl(config.getString("storage.jdbc.url"));
    this.jdbi = Jdbi.create(ds).installPlugin(new SQLitePlugin());
  }

  /**
   * Get Jdbi instance
   * Jdbi instances are thread-safe and do not own any database resources.
   * Typically applications create a single, shared Jdbi instance, and set up any common configuration there.
   *
   * @return Jdbi Instance
   */
  public Jdbi getJdbi() {
    return this.jdbi;
  }

  /**
   * Parse and return SQLite configuration options
   * See http://www.sqlite.org/pragma.html for more information
   *
   * @param config Configuration
   * @return SQLite Configuration
   */
  protected Properties parseSQLitePragma(AbstractConfiguration config) {
    final String prefix = "storage.sqlite.pragma";
    Properties prop = new Properties();
    config.getKeys(prefix).forEachRemaining(
        key -> prop.put(key.substring(prefix.length() + 1), config.getString(key)));
    return prop;
  }
}
