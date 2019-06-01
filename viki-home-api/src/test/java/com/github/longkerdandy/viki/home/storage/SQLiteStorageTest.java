package com.github.longkerdandy.viki.home.storage;

import com.github.longkerdandy.viki.home.model.DataType;
import com.github.longkerdandy.viki.home.model.Property;
import com.github.longkerdandy.viki.home.model.Thing;
import com.github.longkerdandy.viki.home.util.IdGenerator;
import com.github.longkerdandy.viki.home.util.SQLites;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

public class SQLiteStorageTest {

  private static SQLiteStorage storage;

  @BeforeClass
  public static void init() throws IOException {
    String path = File.createTempFile("viki_home_", ".db").getAbsolutePath();
    storage = new SQLiteStorage(new MapConfiguration(
        Map.of("storage.jdbc.url", "jdbc:sqlite:" + path,
            "storage.sqlite.pragma.foreign_keys", "true")));

    storage.getJdbi().useHandle(handle -> {
      handle.execute("DROP TABLE IF EXISTS core_thing");
      handle.execute("CREATE TABLE core_thing(\n"
          + "  id TEXT NOT NULL,\n"
          + "  _schema TEXT NOT NULL,\n"
          + "  heartbeat INTEGER NOT NULL,\n"
          + "  PRIMARY KEY (id ASC)\n"
          + ")");
    });

    storage.getJdbi().useHandle(handle -> {
      handle.execute("DROP TABLE IF EXISTS core_property");
      handle.execute("CREATE TABLE core_property(\n"
          + "  thing TEXT NOT NULL,\n"
          + "  name TEXT NOT NULL,\n"
          + "  type TEXT NOT NULL,\n"
          + "  _value TEXT NOT NULL,\n"
          + "  updated_at INTEGER NOT NULL,\n"
          + "  PRIMARY KEY (thing, name ASC),\n"
          + "  FOREIGN KEY (thing) REFERENCES core_thing(id) ON DELETE CASCADE ON UPDATE NO ACTION\n"
          + ")");
    });
  }

  @Test
  public void upsertPropertyTest() throws IOException {
    Thing t = new Thing(new IdGenerator().nextId(), "tesla:vehicle:model_3",
        null, LocalDateTime.now());
    Property<long[]> p = new Property<>("size", DataType.ARRAY_INTEGER,
        new long[]{4979L, 1964L, 1445L}, LocalDateTime.now());
    assert storage.insertThing(t);
    assert storage.upsertProperty(t.getId(), p);
    Optional<Property> r = storage.getPropertyByName(t.getId(), "size");
    assert r.isPresent();
    assert r.get().getName().equals("size");
    assert r.get().getType() == DataType.ARRAY_INTEGER;
    assert Arrays.equals((long[]) r.get().getValue(), new long[]{4979L, 1964L, 1445L});
    assert r.get().getUpdatedAt() != null;

    p = new Property<>("size", DataType.ARRAY_INTEGER,
        new long[]{4694L, 1849L, 1443L}, LocalDateTime.now());
    assert storage.upsertProperty(t.getId(), p);
    r = storage.getPropertyByName(t.getId(), "size");
    assert r.isPresent();
    assert r.get().getName().equals("size");
    assert r.get().getType() == DataType.ARRAY_INTEGER;
    assert Arrays.equals((long[]) r.get().getValue(), new long[]{4694L, 1849L, 1443L});
    assert r.get().getUpdatedAt() != null;
  }
}
