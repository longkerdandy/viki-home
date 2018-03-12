package com.github.longkerdandy.viki.home.storage;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.Jdbi;
import org.junit.Test;

public class StorageFactoryTest {

  @Test
  public void sampleDatabaseTest() throws IOException {
    File tmp = File.createTempFile("viki_home_", "db");
    String path = tmp.getAbsolutePath();
    Map<String, Object> map = Map.of("storage.jdbc.url", "jdbc:sqlite:" + path);
    MapConfiguration config = new MapConfiguration(map);
    StorageFactory factory = new StorageFactory(config);
    Jdbi jdbi = factory.getJdbi();

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
    StorageFactory factory = new StorageFactory(config);
    Properties prop = factory.parseSQLitePragma(config);
    assert prop.size() == 2;
    assert prop.getProperty("encoding").equals("UTF-8");
    assert prop.getProperty("secure_delete").equals("true");
  }
}
