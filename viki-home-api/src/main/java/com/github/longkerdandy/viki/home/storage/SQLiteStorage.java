package com.github.longkerdandy.viki.home.storage;

import static com.github.longkerdandy.viki.home.model.DataType.BLOB;
import static com.github.longkerdandy.viki.home.util.Jacksons.getWriter;
import static com.github.longkerdandy.viki.home.util.SQLites.parseSQLitePragma;
import static java.time.ZoneId.systemDefault;
import static java.util.Base64.getEncoder;

import com.github.longkerdandy.viki.home.model.Property;
import com.github.longkerdandy.viki.home.model.Thing;
import com.github.longkerdandy.viki.home.storage.mapper.PropertyMapper;
import com.github.longkerdandy.viki.home.storage.mapper.ThingMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.apache.commons.configuration2.AbstractConfiguration;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlite3.SQLitePlugin;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

/**
 * SQLite Storage
 */
public class SQLiteStorage {

  // Jdbi Instance
  private final Jdbi jdbi;
  // SQLiteDataSource
  private final SQLiteDataSource ds;

  /**
   * Constructor
   *
   * @param config Storage Configuration
   */
  public SQLiteStorage(AbstractConfiguration config) {
    SQLiteConfig sc = new SQLiteConfig(parseSQLitePragma(config));
    this.ds = new SQLiteDataSource(sc);
    this.ds.setUrl(config.getString("storage.jdbc.url"));
    this.jdbi = Jdbi.create(ds).installPlugin(new SQLitePlugin());
    this.jdbi.registerRowMapper(new ThingMapper());
    this.jdbi.registerRowMapper(new PropertyMapper());
  }

  /**
   * Get the Jdbi instance. Jdbi instances are thread-safe and do not own any database resources.
   * Typically applications create a single, shared Jdbi instance, and set up any common
   * configuration there.
   *
   * @return Jdbi Instance
   */
  public Jdbi getJdbi() {
    return this.jdbi;
  }

  /**
   * Get the SQLiteDataSource instance.
   *
   * @return SQLiteDataSource instance
   */
  public SQLiteDataSource getDataSource() {
    return ds;
  }

  /**
   * Get {@link Thing} by its name
   *
   * @param id of {@link Thing}
   * @return Optional {@link Thing}
   */
  public Optional<Thing> getThingByName(String id) {
    return this.jdbi.withHandle(handle ->
        handle.createQuery("SELECT * FROM core_thing WHERE id = :id")
            .bind("id", id)
            .mapTo(Thing.class)
            .findFirst()
    );
  }

  /**
   * Touch {@link Thing} and update heartbeat timestamp
   *
   * @param id of {@link Thing}
   * @param heartbeat timestamp
   * @return True if successful
   */
  public boolean touchThing(String id, LocalDateTime heartbeat) {
    return this.jdbi.withHandle(handle ->
        handle.createUpdate(
            "UPDATE core_thing "
                + "SET heartbeat = :heartbeat "
                + "WHERE id = :id")
            .bind("id", id)
            .bind("heartbeat", heartbeat.atZone(systemDefault()).toEpochSecond())
            .execute()
    ) == 1;
  }

  /**
   * Insert {@link Thing}
   *
   * @param thing {@link Thing}
   * @return True if successful
   */
  public boolean insertThing(Thing thing) {
    return this.jdbi.withHandle(handle ->
        handle.createUpdate(
            "INSERT INTO core_thing(id, _schema, heartbeat) "
                + "VALUES(:id, :_schema, :heartbeat)")
            .bind("id", thing.getId())
            .bind("_schema", thing.getSchema())
            .bind("heartbeat", thing.getHeartbeat().atZone(systemDefault()).toEpochSecond())
            .execute()
    ) == 1;
  }

  /**
   * Update {@link Thing}
   *
   * @param thing {@link Thing}
   * @return True if successful
   */
  public boolean updateThing(Thing thing) {
    return this.jdbi.withHandle(handle ->
        handle.createUpdate(
            "UPDATE core_thing "
                + "SET _schema = :_schema, heartbeat = :heartbeat "
                + "WHERE id = :id")
            .bind("id", thing.getId())
            .bind("_schema", thing.getSchema())
            .bind("heartbeat", thing.getHeartbeat().atZone(systemDefault()).toEpochSecond())
            .execute()
    ) == 1;
  }

  /**
   * Insert {@link Thing} and its {@link Property}s
   *
   * @param thing {@link Thing}
   * @return True if successful
   * @throws IOException when value serialization failed
   */
  public boolean insertThingWithProperties(Thing thing) throws IOException {
    return this.jdbi.inTransaction(handle -> {
      int rows = handle.createUpdate(
          "INSERT INTO core_thing(id, _schema, heartbeat) "
              + "VALUES(:id, :_schema, :heartbeat)")
          .bind("id", thing.getId())
          .bind("_schema", thing.getSchema())
          .bind("heartbeat", thing.getHeartbeat().atZone(systemDefault()).toEpochSecond())
          .execute();
      if (rows != 1) {
        return false;
      }

      for (Property property : thing.getProperties()) {
        String v = property.getType() == BLOB ?
            getEncoder().encodeToString((byte[]) property.getValue()) :
            getWriter().writeValueAsString(property.getValue());
        rows = handle.createUpdate(
            "INSERT INTO core_property(thing, name, type, _value, updated_at) "
                + "VALUES(:thing, :name, :type, :_value, :updated_at) "
                + "ON CONFLICT(thing, name) DO UPDATE SET "
                + "type = excluded.type, "
                + "_value = excluded._value,"
                + "updated_at = excluded.updated_at")
            .bind("thing", thing.getId())
            .bind("name", property.getName())
            .bind("type", property.getType().value())
            .bind("_value", v)
            .bind("updated_at", property.getUpdatedAt().atZone(systemDefault()).toEpochSecond())
            .execute();
        if (rows != 1) {
          return false;
        }
      }

      return true;
    });
  }

  /**
   * Update {@link Thing} and its {@link Property}s
   *
   * @param thing {@link Thing}
   * @return True if successful
   * @throws IOException when value serialization failed
   */
  public boolean updateThingWithProperties(Thing thing) throws IOException {
    return this.jdbi.inTransaction(handle -> {
      int rows = handle.createUpdate(
          "UPDATE core_thing "
              + "SET _schema = :_schema, heartbeat = :heartbeat "
              + "WHERE id = :id")
          .bind("id", thing.getId())
          .bind("_schema", thing.getSchema())
          .bind("heartbeat", thing.getHeartbeat().atZone(systemDefault()).toEpochSecond())
          .execute();
      if (rows != 1) {
        return false;
      }

      for (Property property : thing.getProperties()) {
        String v = property.getType() == BLOB ?
            getEncoder().encodeToString((byte[]) property.getValue()) :
            getWriter().writeValueAsString(property.getValue());
        rows = handle.createUpdate(
            "INSERT INTO core_property(thing, name, type, _value, updated_at) "
                + "VALUES(:thing, :name, :type, :_value, :updated_at) "
                + "ON CONFLICT(thing, name) DO UPDATE SET "
                + "type = excluded.type, "
                + "_value = excluded._value,"
                + "updated_at = excluded.updated_at")
            .bind("thing", thing.getId())
            .bind("name", property.getName())
            .bind("type", property.getType().value())
            .bind("_value", v)
            .bind("updated_at", property.getUpdatedAt().atZone(systemDefault()).toEpochSecond())
            .execute();
        if (rows != 1) {
          return false;
        }
      }

      return true;
    });
  }

  /**
   * Get {@link Property} by its name
   *
   * @param thing id
   * @param name of {@link Property}
   * @return Optional {@link Property}
   */
  public Optional<Property> getPropertyByName(String thing, String name) {
    return this.jdbi.withHandle(handle ->
        handle.createQuery("SELECT * FROM core_property WHERE thing = :thing AND name = :name")
            .bind("thing", thing)
            .bind("name", name)
            .mapTo(Property.class)
            .findFirst()
    );
  }

  /**
   * Update or insert {@link Property}
   *
   * @param thing id
   * @param property {@link Property}
   * @return True if successful
   * @throws IOException when value serialization failed
   */
  public boolean upsertProperty(String thing, Property property) throws IOException {
    String v = property.getType() == BLOB ?
        getEncoder().encodeToString((byte[]) property.getValue()) :
        getWriter().writeValueAsString(property.getValue());
    return this.jdbi.withHandle(handle ->
        handle.createUpdate(
            "INSERT INTO core_property(thing, name, type, _value, updated_at) "
                + "VALUES(:thing, :name, :type, :_value, :updated_at) "
                + "ON CONFLICT(thing, name) DO UPDATE SET "
                + "type = excluded.type, "
                + "_value = excluded._value,"
                + "updated_at = excluded.updated_at")
            .bind("thing", thing)
            .bind("name", property.getName())
            .bind("type", property.getType().value())
            .bind("_value", v)
            .bind("updated_at", property.getUpdatedAt().atZone(systemDefault()).toEpochSecond())
            .execute()
    ) == 1;
  }
}
