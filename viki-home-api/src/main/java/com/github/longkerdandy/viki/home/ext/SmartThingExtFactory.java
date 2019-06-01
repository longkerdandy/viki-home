package com.github.longkerdandy.viki.home.ext;

import com.github.longkerdandy.viki.home.storage.SQLiteStorage;
import java.util.Locale;

/**
 * Extension Factory for {@link SmartThingExt}
 *
 * @param <T> {@link SmartThingExt}
 */
public interface SmartThingExtFactory<T extends SmartThingExt> {

  /**
   * Create a new {@link SmartThingExt} instance
   *
   * @param locale {@link Locale}
   * @param storage {@link SQLiteStorage}
   * @return {@link SmartThingExt}
   */
  T create(Locale locale, SQLiteStorage storage);
}
