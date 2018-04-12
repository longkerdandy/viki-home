package com.github.longkerdandy.viki.home.storage.sqlite;

import com.github.longkerdandy.viki.home.storage.Storage;
import com.github.longkerdandy.viki.home.storage.StorageFactory;
import org.apache.commons.configuration2.AbstractConfiguration;

/**
 * Factory for SQLite Storage
 */
public class SQLiteStorageFactory implements StorageFactory {

  @Override
  public Storage createStorage(AbstractConfiguration config) {
    return new SQLiteStorage(config);
  }
}
