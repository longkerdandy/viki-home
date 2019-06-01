package com.github.longkerdandy.viki.home.util;

import java.util.Map;
import java.util.Properties;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.Test;

public class SQLitesTest {

  @Test
  public void parseSQLitePragmaTest() {
    Map<String, Object> map = Map.of(
        "storage.jdbc.url", "jdbc:sqlite::memory:",
        "storage.sqlite.pragma.encoding", "UTF-8",
        "storage.sqlite.pragma.secure_delete", "true");
    MapConfiguration config = new MapConfiguration(map);
    Properties prop = SQLites.parseSQLitePragma(config);
    assert prop.size() == 2;
    assert prop.getProperty("encoding").equals("UTF-8");
    assert prop.getProperty("secure_delete").equals("true");
  }
}
