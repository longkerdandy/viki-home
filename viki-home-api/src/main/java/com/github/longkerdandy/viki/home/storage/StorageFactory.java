package com.github.longkerdandy.viki.home.storage;

import org.apache.commons.configuration2.AbstractConfiguration;

/**
 * Factory for Storage
 */
public interface StorageFactory {

  /**
   * Create a new Storage instance based on the given configuration
   *
   * @param config Configuration
   * @return Storage
   */
  Storage createStorage(AbstractConfiguration config);
}
