package com.github.longkerdandy.viki.home.mi.storage;

import static com.github.longkerdandy.viki.home.util.SQLites.parseSQLitePragma;

import com.github.longkerdandy.viki.home.mi.model.Gateway;
import com.github.longkerdandy.viki.home.mi.model.ZigbeeThing;
import com.github.longkerdandy.viki.home.mi.storage.mapper.GatewayMapper;
import com.github.longkerdandy.viki.home.mi.storage.mapper.ZigbeeThingMapper;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.apache.commons.configuration2.AbstractConfiguration;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlite3.SQLitePlugin;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

/**
 * Storage for Mi Extension
 */
public class MiStorage {

  // Jdbi Instance
  private final Jdbi jdbi;
  // DataSource
  private final SQLiteDataSource ds;

  /**
   * Constructor
   *
   * @param config Storage configuration
   */
  public MiStorage(AbstractConfiguration config) {
    SQLiteConfig sc = new SQLiteConfig(parseSQLitePragma(config));
    this.ds = new SQLiteDataSource(sc);
    this.ds.setUrl(config.getString("storage.jdbc.url"));
    this.jdbi = Jdbi.create(this.ds).installPlugin(new SQLitePlugin());
    this.jdbi.registerRowMapper(new GatewayMapper());
    this.jdbi.registerRowMapper(new ZigbeeThingMapper());
  }

  /**
   * Get the Jdbi instance. Jdbi instances are thread-safe and do not own any database resources.
   * Typically applications create a single, shared Jdbi instance, and set up any common
   * configuration there.
   *
   * @return Jdbi instance
   */
  public Jdbi getJdbi() {
    return this.jdbi;
  }

  /**
   * Get the {@link DataSource} instance. This is used for database migration.
   *
   * @return {@link DataSource} instance
   */
  public DataSource getDataSource() {
    return this.ds;
  }

  /**
   * Initialize {@link MiStorage}
   */
  public void init() {
  }

  /**
   * Get {@link Gateway} by its id
   *
   * @param gid Gateway id
   * @return Optional {@link Gateway}
   */
  public Optional<Gateway> getGatewayById(String gid) {
    return this.jdbi.withHandle(handle ->
        handle.createQuery("SELECT * FROM ext_mi_gateway WHERE gid = :gid")
            .bind("gid", gid)
            .mapTo(Gateway.class)
            .findFirst()
    );
  }

  /**
   * List all the {@link Gateway}
   *
   * @return List of {@link Gateway}
   */
  public List<Gateway> listGateway() {
    return this.jdbi.withHandle(handle ->
        handle.createQuery("SELECT * FROM ext_mi_gateway")
            .mapTo(Gateway.class)
            .list()
    );
  }

  /**
   * Insert new {@link Gateway} if not exists
   *
   * @param gateway {@link Gateway}
   * @return True if successful
   */
  public boolean insetGateway(Gateway gateway) {
    return this.jdbi.withHandle(handle ->
        handle.createUpdate(
            "INSERT OR IGNORE INTO ext_mi_gateway(gid, model, protocol_version, token, address, port) "
                + "VALUES(:gid, :model, :protocol_version, :token, :address, :port)")
            .bind("gid", gateway.getGid())
            .bind("model", gateway.getModel())
            .bind("protocol_version", gateway.getProtocolVersion())
            .bind("token", gateway.getToken())
            .bind("address", gateway.getAddress().getHostAddress())
            .bind("port", gateway.getPort())
            .execute() == 1
    );
  }

  /**
   * Update the {@link Gateway} if exists
   *
   * @param gateway {@link Gateway}
   * @return True if successful
   */
  public boolean updateGateway(Gateway gateway) {
    return this.jdbi.withHandle(handle ->
        handle.createUpdate(
            "UPDATE ext_mi_gateway "
                + "SET model = :model, protocol_version = :protocol_version, token = :token, address = address, port = :port "
                + "WHERE gid = :gid")
            .bind("gid", gateway.getGid())
            .bind("model", gateway.getModel())
            .bind("protocol_version", gateway.getProtocolVersion())
            .bind("token", gateway.getToken())
            .bind("address", gateway.getAddress().getHostAddress())
            .bind("port", gateway.getPort())
            .execute() == 1
    );
  }

  /**
   * Update Gateway's password
   *
   * @param gid Gateway id
   * @param password Password
   * @return True if successful
   */
  public boolean updateGatewayPassword(String gid, String password) {
    return this.jdbi.withHandle(handle ->
        handle.createUpdate("UPDATE ext_mi_gateway SET password = :password WHERE gid = :gid")
            .bind("gid", gid)
            .bind("password", password)
            .execute() == 1
    );
  }

  /**
   * Update Gateway's token
   *
   * @param gid Gateway id
   * @param token Token
   * @return True if successful
   */
  public boolean updateGatewayToken(String gid, String token) {
    return this.jdbi.withHandle(handle ->
        handle.createUpdate("UPDATE ext_mi_gateway SET token = :token WHERE gid = :gid")
            .bind("gid", gid)
            .bind("token", token)
            .execute() == 1
    );
  }

  /**
   * List all the {@link ZigbeeThing}
   *
   * @return List of {@link ZigbeeThing}
   */
  public List<ZigbeeThing> listZigbeeThing() {
    return this.jdbi.withHandle(handle ->
        handle.createQuery("SELECT * FROM ext_mi_zigbee_thing")
            .mapTo(ZigbeeThing.class)
            .list()
    );
  }

  /**
   * Get {@link ZigbeeThing} by its id
   *
   * @param sid ZigbeeThing id
   * @return Optional {@link ZigbeeThing}
   */
  public Optional<ZigbeeThing> getZigbeeThingById(String sid) {
    return this.jdbi.withHandle(handle ->
        handle.createQuery("SELECT * FROM ext_mi_zigbee_thing WHERE sid = :sid")
            .bind("sid", sid)
            .mapTo(ZigbeeThing.class)
            .findFirst()
    );
  }

  /**
   * Get {@link ZigbeeThing} by Thing's id
   *
   * @param tid Thing id
   * @return Optional {@link ZigbeeThing}
   */
  public Optional<ZigbeeThing> getZigbeeThingByTid(String tid) {
    return this.jdbi.withHandle(handle ->
        handle.createQuery("SELECT * FROM ext_mi_zigbee_thing WHERE tid = :tid")
            .bind("tid", tid)
            .mapTo(ZigbeeThing.class)
            .findFirst()
    );
  }

  /**
   * Insert new {@link ZigbeeThing} if not exists
   *
   * @param thing {@link ZigbeeThing}
   * @return True if successful
   */
  public boolean insetZigbeeThing(ZigbeeThing thing) {
    return this.jdbi.withHandle(handle ->
        handle.createUpdate(
            "INSERT OR IGNORE INTO ext_mi_zigbee_thing(sid, gid, tid, model, short_id) "
                + "VALUES(:sid, :gid, :tid, :model, :short_id)")
            .bind("sid", thing.getSid())
            .bind("gid", thing.getGid())
            .bind("tid", thing.getTid())
            .bind("model", thing.getModel())
            .bind("short_id", thing.getShortId())
            .execute() == 1
    );
  }

  /**
   * Update the {@link ZigbeeThing} if exists
   *
   * @param thing {@link ZigbeeThing}
   * @return Optional tid
   */
  public Optional<String> updateZigbeeThing(ZigbeeThing thing) {
    return this.jdbi.inTransaction(handle -> {
      int rows = handle.createUpdate(
          "UPDATE ext_mi_zigbee_thing "
              + "SET gid = :gid, model = :model, short_id = :short_id "
              + "WHERE sid = :sid")
          .bind("sid", thing.getSid())
          .bind("gid", thing.getGid())
          .bind("model", thing.getModel())
          .bind("short_id", thing.getShortId())
          .execute();
      if (rows == 1) {
        return handle.createQuery("SELECT tid FROM ext_mi_zigbee_thing WHERE sid = :sid")
            .bind("sid", thing.getSid())
            .mapTo(String.class)
            .findFirst();
      }
      return Optional.empty();
    });
  }
}
