package com.github.longkerdandy.viki.home.mi;

import com.github.longkerdandy.viki.home.ext.SmartThingExtFactory;
import com.github.longkerdandy.viki.home.storage.SQLiteStorage;
import java.util.Locale;

/**
 * Factory for the XiaoMi smart home protocol extension
 */
public class MiProtocolExtFactory implements SmartThingExtFactory<MiProtocolExt> {

  @Override
  public MiProtocolExt create(Locale locale, SQLiteStorage storage) {
    return new MiProtocolExt(locale, storage);
  }
}
