package com.github.longkerdandy.viki.home.hap;

import com.github.longkerdandy.viki.home.ext.ControllerExtFactory;
import com.github.longkerdandy.viki.home.storage.SQLiteStorage;
import java.util.Locale;

/**
 * Factory for HomeKit Accessory Protocol Extension
 */
public class HomeKitProtocolExtFactory implements ControllerExtFactory<HomeKitProtocolExt> {

  @Override
  public HomeKitProtocolExt create(Locale locale, SQLiteStorage storage) {
    return new HomeKitProtocolExt(locale, storage);
  }
}
