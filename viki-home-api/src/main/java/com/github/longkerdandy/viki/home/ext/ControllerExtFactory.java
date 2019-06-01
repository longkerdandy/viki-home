package com.github.longkerdandy.viki.home.ext;

import com.github.longkerdandy.viki.home.storage.SQLiteStorage;
import java.util.Locale;

/**
 * Extension Factory for {@link ControllerExt}
 *
 * @param <T> {@link ControllerExt}
 */
public interface ControllerExtFactory<T extends ControllerExt> {

  /**
   * Create a new {@link ControllerExt} instance
   *
   * @param locale {@link Locale}
   * @param storage {@link SQLiteStorage}
   * @return {@link ControllerExt}
   */
  T create(Locale locale, SQLiteStorage storage);
}
