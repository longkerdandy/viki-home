package com.github.longkerdandy.viki.home.hap;

import com.github.longkerdandy.viki.home.addon.ProtocolAddOnFactory;
import com.github.longkerdandy.viki.home.storage.Storage;
import org.apache.commons.configuration2.AbstractConfiguration;

/**
 * Factory for HomeKit Accessory Protocol Add-On
 */
public class HomeKitProtocolAddOnFactory implements ProtocolAddOnFactory {

  @Override
  public HomeKitProtocolAddOn create(AbstractConfiguration config, Storage storage) {
    return new HomeKitProtocolAddOn(config, storage);
  }
}
