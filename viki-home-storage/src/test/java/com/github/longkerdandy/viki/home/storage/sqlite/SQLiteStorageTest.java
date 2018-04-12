package com.github.longkerdandy.viki.home.storage.sqlite;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.Jdbi;
import org.junit.Test;

public class SQLiteStorageTest {

  @Test
  public void sampleDatabaseTest() throws IOException {
    String path = File.createTempFile("viki_home_", "db").getAbsolutePath();
    SQLiteStorage storage = new SQLiteStorage(new MapConfiguration(
        Map.of("storage.jdbc.url", "jdbc:sqlite:" + path,
            "storage.sqlite.pragma.foreign_keys", "true")));
    Jdbi jdbi = storage.getJdbi();

    jdbi.useHandle(handle -> {
      handle.execute("DROP TABLE IF EXISTS Genre");
      handle.execute("CREATE TABLE Genre\n"
          + "(\n"
          + "    GenreId INTEGER  NOT NULL,\n"
          + "    Name NVARCHAR(120),\n"
          + "    CONSTRAINT PK_Genre PRIMARY KEY (GenreId)\n"
          + ")");
    });

    jdbi.useHandle(handle -> {
      handle.execute("INSERT INTO Genre (GenreId, Name) VALUES (1, 'Rock')");
      handle.execute("INSERT INTO Genre (GenreId, Name) VALUES (2, 'Jazz')");
      handle.execute("INSERT INTO Genre (GenreId, Name) VALUES (3, 'Metal')");
      handle.execute("INSERT INTO Genre (GenreId, Name) VALUES (4, 'Alternative & Punk')");
      handle.execute("INSERT INTO Genre (GenreId, Name) VALUES (5, 'Rock And Roll')");
    });

    String name = jdbi.withHandle(handle ->
        handle.createQuery("SELECT Name FROM Genre WHERE GenreId = :id")
            .bind("id", 3)
            .mapTo(String.class)
            .findOnly());

    assert StringUtils.isNotEmpty(name);
    assert name.equals("Metal");
  }

  @Test
  public void parseSQLitePragmaTest() {
    Map<String, Object> map = Map.of(
        "storage.jdbc.url", "jdbc:sqlite::memory:",
        "storage.sqlite.pragma.encoding", "UTF-8",
        "storage.sqlite.pragma.secure_delete", "true");
    MapConfiguration config = new MapConfiguration(map);
    SQLiteStorage storage = new SQLiteStorage(config);
    Properties prop = storage.parseSQLitePragma(config);
    assert prop.size() == 2;
    assert prop.getProperty("encoding").equals("UTF-8");
    assert prop.getProperty("secure_delete").equals("true");
  }
}
