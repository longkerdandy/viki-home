package com.github.longkerdandy.viki.home.mi.service;

import static com.github.longkerdandy.viki.home.mi.udp.GatewayUDPCodec.decodeResponse;
import static com.github.longkerdandy.viki.home.util.Networks.getIPAddress;

import com.github.longkerdandy.viki.home.mi.model.Gateway;
import com.github.longkerdandy.viki.home.mi.model.ZigbeeThing;
import com.github.longkerdandy.viki.home.mi.schema.SchemaRepository;
import com.github.longkerdandy.viki.home.mi.storage.MiStorage;
import com.github.longkerdandy.viki.home.model.Thing;
import com.github.longkerdandy.viki.home.schema.ThingSchema;
import com.github.longkerdandy.viki.home.storage.SQLiteStorage;
import com.github.longkerdandy.viki.home.util.Networks;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Gateway} multicast service
 *
 * Handles multicast messages like 'report' 'heartbeat'
 */
public class GatewayMulticastService {

  private static final Logger logger = LoggerFactory.getLogger(GatewayMulticastService.class);

  private final SQLiteStorage storage;
  private final MiStorage miStorage;
  private final SchemaRepository schemas;
  private final ExecutorService workerGroup;

  /***
   * Constructor
   *
   * @param storage {@link SQLiteStorage}
   * @param miStorage {@link MiStorage}
   * @param schemas {@link SchemaRepository}
   */
  public GatewayMulticastService(SQLiteStorage storage, MiStorage miStorage,
      SchemaRepository schemas) {
    this.storage = storage;
    this.miStorage = miStorage;
    this.schemas = schemas;
    this.workerGroup = Executors.newFixedThreadPool(1);
  }

  /**
   * Initialize
   */
  public void init() {
    this.workerGroup.submit(() -> {
      try {
        // Create MulticastSocket and join the group
        MulticastSocket socket = new MulticastSocket(9898);
        socket.setInterface(Networks.getLocalInetAddress());
        socket.joinGroup(InetAddress.getByName("224.0.0.50"));

        // Reused response message buffer
        byte[] buf = new byte[1024];

        // Loop and receive incoming messages
        // noinspection InfiniteLoopStatement
        while (true) {
          // Receive packet, this will block
          DatagramPacket packet = new DatagramPacket(buf, buf.length);
          socket.receive(packet);
          Map<String, Object> msg = decodeResponse(packet.getData());

          String cmd = (String) msg.get("cmd");
          switch (cmd) {
            // When 'report' is received, we assume the thing's status has changed.
            // Thing and its properties will be updated, event will be emitted.
            case "report":
              logger.debug("Received report message from {}", getIPAddress(packet.getAddress()));
              Optional<ZigbeeThing> r1 = this.miStorage.getZigbeeThingById((String) msg.get("sid"));
              if (r1.isPresent()) {
                ZigbeeThing zigbeeThing = r1.get();
                zigbeeThing = new ZigbeeThing(zigbeeThing.getSid(), zigbeeThing.getGid(),
                    zigbeeThing.getTid(), (String) msg.get("model"),
                    msg.containsKey("short_id") ? (Integer) msg.get("short_id") : null);
                if (this.miStorage.updateZigbeeThing(zigbeeThing).isPresent()) {
                  logger.debug("ZigbeeThing {} has been updated", zigbeeThing);
                  ThingSchema schema = this.schemas.getSchemaByModel(zigbeeThing.getModel());
                  Thing t = zigbeeThing.toThing(schema, msg);
                  if (this.storage.updateThingWithProperties(t)) {
                    logger.debug("Thing {} and its properties has been updated", t);
                    // TODO: Send thing event
                  } else {
                    logger.error("Failed to update Thing {}, data maybe corrupted", t);
                  }
                }
              }
              break;
            // When 'heartbeat' is received, we assume the thing is still alive.
            // Thing's timestamp will be updated, event will not be emitted
            case "heartbeat":
              logger.debug("Received heartbeat message from {}", getIPAddress(packet.getAddress()));
              Optional<ZigbeeThing> r2 = this.miStorage.getZigbeeThingById((String) msg.get("sid"));
              if (r2.isPresent()) {
                ZigbeeThing zigbeeThing = r2.get();
                String tid = zigbeeThing.getTid();
                if (this.storage.touchThing(tid, LocalDateTime.now())) {
                  logger.debug("Thing {} has been touched", tid);
                } else {
                  logger.error("Failed to touch Thing {}, data maybe corrupted", tid);
                }
              }
              if ("gateway".equals(msg.get("model")) || "gateway.aq1".equals(msg.get("model"))) {
                String gid = (String) msg.get("sid");
                if (this.miStorage.updateGatewayToken(gid, (String) msg.get("token"))) {
                  logger.debug("Gateway {} token has been updated", gid);
                }
              }
              break;
            default:
              logger.warn("Unsupported {} message from {}", cmd, getIPAddress(packet.getAddress()));
          }
        }
      } catch (IOException e) {
        logger.warn("Exception happened when trying to receive multicast messages: ", e);
      } catch (Exception e) {
        // Catch and logging top level exception here because it isn't the main thread
        logger.error("Error happened when trying to discover zigbee things:", e);
      }
    });
  }

  /**
   * Destroy
   */
  public void destroy() {
    this.workerGroup.shutdown();
  }
}
