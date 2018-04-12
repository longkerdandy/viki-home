package com.github.longkerdandy.viki.home.storage;

import javax.sql.DataSource;
import org.jdbi.v3.core.Jdbi;

/**
 * Storage
 */
public interface Storage {

  /**
   * Get the Jdbi instance
   *
   * @return Jdbi
   */
  Jdbi getJdbi();

  /**
   * Get the DataSource instance
   *
   * @return DataSource
   */
  DataSource getDataSource();
}
