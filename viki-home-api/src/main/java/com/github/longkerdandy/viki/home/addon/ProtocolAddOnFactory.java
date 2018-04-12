package com.github.longkerdandy.viki.home.addon;

import com.github.longkerdandy.viki.home.storage.Storage;
import org.apache.commons.configuration2.AbstractConfiguration;

/**
 * Factory for Protocol Add-On
 */
public interface ProtocolAddOnFactory {

  /**
   * Create a new Protocol Add-On instance based on the given configuration
   *
   * @param config Configuration
   * @param storage Storage
   * @return Protocol Add-On
   */
  ProtocolAddOn create(AbstractConfiguration config, Storage storage);
}
