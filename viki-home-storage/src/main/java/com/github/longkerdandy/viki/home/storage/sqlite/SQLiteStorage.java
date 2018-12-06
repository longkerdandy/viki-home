package com.github.longkerdandy.viki.home.storage.sqlite;

import com.github.longkerdandy.viki.home.storage.Storage;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.configuration2.AbstractConfiguration;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlite3.SQLitePlugin;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

/**
 * SQLite Storage
 */
public class SQLiteStorage implements Storage {

  // data source
  private final SQLiteDataSource ds;
  // Jdbi Instance
  private final Jdbi jdbi;

  /**
   * Constructor
   *
   * @param config Storage Configuration
   */
  public SQLiteStorage(AbstractConfiguration config) {
    SQLiteConfig sc = new SQLiteConfig(parseSQLitePragma(config));
    this.ds = new SQLiteDataSource(sc);
    this.ds.setUrl(config.getString("storage.jdbc.url"));
    this.jdbi = Jdbi.create(ds).installPlugin(new SQLitePlugin());
  }

  /**
   * Get the DataSource instance.
   *
   * @return DataSource Instance
   */
  @Override
  public DataSource getDataSource() {
    return this.ds;
  }

  /**
   * Get the Jdbi instance. Jdbi instances are thread-safe and do not own any database resources.
   * Typically applications create a single, shared Jdbi instance, and set up any common
   * configuration there.
   *
   * @return Jdbi Instance
   */
  @Override
  public Jdbi getJdbi() {
    return this.jdbi;
  }

  /**
   * Parse and return SQLite configuration options.
   *
   * @param config Configuration
   * @return SQLite Configuration
   * @see <a href="http://www.sqlite.org/pragma.html">http://www.sqlite.org/pragma.html</a>
   */
  protected Properties parseSQLitePragma(AbstractConfiguration config) {
    final String prefix = "storage.sqlite.pragma";
    Properties prop = new Properties();
    config.getKeys(prefix).forEachRemaining(
        key -> prop.put(key.substring(prefix.length() + 1), config.getString(key)));
    return prop;
  }
}
