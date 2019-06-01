package com.github.longkerdandy.viki.home.mi;

import static com.github.longkerdandy.viki.home.mi.schema.SchemaMapping.actionToParam;
import static com.github.longkerdandy.viki.home.mi.schema.SchemaMapping.propToParam;
import static com.github.longkerdandy.viki.home.mi.udp.GatewayUDPCodec.encryptKey;
import static com.github.longkerdandy.viki.home.mi.udp.GatewayUDPCodec.write;
import static com.github.longkerdandy.viki.home.util.Configurations.getPropertiesConfiguration;

import com.github.longkerdandy.viki.home.ext.SmartThingExt;
import com.github.longkerdandy.viki.home.mi.model.Gateway;
import com.github.longkerdandy.viki.home.mi.model.ZigbeeThing;
import com.github.longkerdandy.viki.home.mi.schema.SchemaRepository;
import com.github.longkerdandy.viki.home.mi.service.GatewayDiscoveryService;
import com.github.longkerdandy.viki.home.mi.service.GatewayMulticastService;
import com.github.longkerdandy.viki.home.mi.storage.MiStorage;
import com.github.longkerdandy.viki.home.model.Action;
import com.github.longkerdandy.viki.home.model.Property;
import com.github.longkerdandy.viki.home.model.WriteResult;
import com.github.longkerdandy.viki.home.schema.ThingSchema;
import com.github.longkerdandy.viki.home.storage.SQLiteStorage;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XiaoMi smart home protocol extension
 */
public class MiProtocolExt implements SmartThingExt {

  public static final String EXT_NAME = "Mi Protocol";
  private static final Logger logger = LoggerFactory.getLogger(MiProtocolExt.class);

  // local
  protected final Locale locale;
  // storage
  protected final SQLiteStorage storage;
  // configuration
  private final PropertiesConfiguration config;
  // socket timeout
  protected final int timeout;
  // ext storage
  protected final MiStorage miStorage;
  // schemas
  protected final SchemaRepository schemas;
  // discovery service
  protected final GatewayDiscoveryService discovery;
  // multicast service
  protected final GatewayMulticastService multicast;

  /**
   * Constructor
   */
  public MiProtocolExt(Locale locale, SQLiteStorage storage) {
    try {
      this.locale = locale;
      this.storage = storage;
      this.config = getPropertiesConfiguration("config/viki-home-mi.properties");
      this.timeout = config.getInt("udp.timeout", 5000);
      this.miStorage = new MiStorage(config);
      this.schemas = new SchemaRepository(this.locale,
          config.getString("path.schema", "schema"),
          config.getString("path.i18n", "i18n"));
      this.discovery = new GatewayDiscoveryService(this.storage, this.miStorage, this.schemas,
          config.getInt("discovery.period", 300 * 1000),
          config.getInt("discovery.duration", 5000), this.timeout);
      this.multicast = new GatewayMulticastService(this.storage, this.miStorage, this.schemas);
    } catch (ConfigurationException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public String getExtName() {
    return EXT_NAME;
  }

  @Override
  public void init() throws Exception {
    logger.info("Initializing Mi Protocol Extension ...");

    logger.info("Migrating Mi storage to new version if necessary ...");
    Flyway.configure()
        .dataSource(this.miStorage.getDataSource())
        .locations(this.config.getString("storage.migration.scripts"))
        .encoding(this.config.getString("storage.migration.encoding", "UTF-8"))
        .load()
        .migrate();

    logger.debug("Initializing Mi storage ...");
    this.miStorage.init();

    logger.debug("Loading thing schemas from path ...");
    this.schemas.load();

    logger.debug("Initializing discovery service ...");
    this.discovery.init();

    logger.debug("Initializing multicast service ...");
    this.multicast.init();

    logger.info("Mi Protocol Extension is initialized ...");
  }

  @Override
  public void destroy() {
    logger.info("Destroying Mi Protocol Extension ...");

    logger.info("Destroying discovery service Extension ...");
    this.discovery.destroy();

    logger.info("Destroying multicast service Extension ...");
    this.multicast.destroy();

    logger.info("Mi Protocol Extension is destroyed ...");
  }

  @Override
  public Map<String, ThingSchema> getSchemas() {
    return this.schemas.getSchemas();
  }

  @Override
  public CompletableFuture<WriteResult> performAction(String thingId, String schemaName,
      Action action) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        // Determine network protocol based on schema name
        if (schemaName.contains("zigbee")) {
          // Get ZigbeeThing and Gateway from storage
          Optional<ZigbeeThing> z = this.miStorage.getZigbeeThingByTid(thingId);
          if (z.isEmpty()) {
            logger.debug("ZigbeeThing with thing id {} not exist in storage", thingId);
            return WriteResult.NOT_EXIST;
          }
          Optional<Gateway> g = this.miStorage.getGatewayById(z.get().getGid());
          if (g.isEmpty()) {
            logger.error("Gateway not exist associate with ZigbeeThing {}", z.get());
            return WriteResult.INTERNAL_ERROR;
          }
          if (g.get().getPassword() == null) {
            logger.error("Gateway {} password is not configured", g.get());
            return WriteResult.NOT_CONFIGURED;
          }
          // Convert action to parameters
          Map<String, Object> params = actionToParam(z.get().getModel(), action);
          // Write message requires key validation based on AES encryption
          String encryptedKey = encryptKey(g.get().getPassword(), g.get().getToken());
          // Gateway's protocol version is 1.x
          if (g.get().isProtocolV1()) {
            Map<String, Object> r = write(z.get().getModel(), z.get().getSid(),
                z.get().getShortId(), encryptedKey, params, g.get().getAddress(),
                g.get().getPort(), this.timeout);
            if (r == null || r.isEmpty()) {
              logger.warn("Write to {} failed with empty response", z.get());
              return WriteResult.INTERNAL_ERROR;
            }
          }
          // Gateway's protocol version is 2.x
          else if (g.get().isProtocolV2()) {
            Map<String, Object> r = write(z.get().getModel(), z.get().getSid(),
                encryptedKey, params, g.get().getAddress(), g.get().getPort(), this.timeout);
            if (r == null || r.isEmpty()) {
              logger.warn("Write to {} failed with empty response", z.get());
              return WriteResult.INTERNAL_ERROR;
            }
          }
          // Gateway's protocol version is unknown
          else {
            logger.warn("Unsupported gateway protocol version {}", g.get());
            return WriteResult.INTERNAL_ERROR;
          }

          logger.debug("Perform action {} to thing {} succeeded", action, thingId);
          return WriteResult.SUCCESS;
        } else {
          logger.error("Unknown network protocol in schema name {}", schemaName);
          return WriteResult.INTERNAL_ERROR;
        }
      } catch (IOException e) {
        logger.warn("Perform action {} to thing {} failed with exception: ", action, thingId, e);
        return WriteResult.INTERNAL_ERROR;
      }
    });
  }

  @Override
  public CompletableFuture<WriteResult> writeProperty(String thingId, String schemaName,
      Property property) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        // Determine network protocol based on schema name
        if (schemaName.contains("zigbee")) {
          // Get ZigbeeThing and Gateway from storage
          Optional<ZigbeeThing> z = this.miStorage.getZigbeeThingByTid(thingId);
          if (z.isEmpty()) {
            logger.debug("ZigbeeThing with thing id {} not exist in storage", thingId);
            return WriteResult.NOT_EXIST;
          }
          Optional<Gateway> g = this.miStorage.getGatewayById(z.get().getGid());
          if (g.isEmpty()) {
            logger.error("Gateway not exist associate with ZigbeeThing {}", z.get());
            return WriteResult.INTERNAL_ERROR;
          }
          if (g.get().getPassword() == null) {
            logger.error("Gateway {} password is not configured", g.get());
            return WriteResult.NOT_CONFIGURED;
          }
          // Convert action to parameters
          Map<String, Object> params = propToParam(z.get().getModel(), property);
          // Write message requires key validation based on AES encryption
          String encryptedKey = encryptKey(g.get().getPassword(), g.get().getToken());
          // Gateway's protocol version is 1.x
          if (g.get().isProtocolV1()) {
            Map<String, Object> r = write(z.get().getModel(), z.get().getSid(),
                z.get().getShortId(), encryptedKey, params, g.get().getAddress(),
                g.get().getPort(), this.timeout);
            if (r == null || r.isEmpty()) {
              logger.warn("Write to {} failed with empty response", z.get());
              return WriteResult.INTERNAL_ERROR;
            }
          }
          // Gateway's protocol version is 2.x
          else if (g.get().isProtocolV2()) {
            Map<String, Object> r = write(z.get().getModel(), z.get().getSid(),
                encryptedKey, params, g.get().getAddress(), g.get().getPort(), this.timeout);
            if (r == null || r.isEmpty()) {
              logger.warn("Write to {} failed with empty response", z.get());
              return WriteResult.INTERNAL_ERROR;
            }
          }
          // Gateway's protocol version is unknown
          else {
            logger.warn("Unsupported gateway protocol version {}", g.get());
            return WriteResult.INTERNAL_ERROR;
          }

          logger.debug("Write property {} to thing {} succeeded", property, thingId);
          return WriteResult.SUCCESS;
        } else {
          logger.error("Unknown network protocol in schema name {}", schemaName);
          return WriteResult.INTERNAL_ERROR;
        }
      } catch (IOException e) {
        logger.warn("Write property {} to thing {} failed with exception: ", property, thingId, e);
        return WriteResult.INTERNAL_ERROR;
      }
    });
  }
}
