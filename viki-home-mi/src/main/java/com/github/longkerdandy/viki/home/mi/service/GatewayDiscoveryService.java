package com.github.longkerdandy.viki.home.mi.service;

import static com.github.longkerdandy.viki.home.mi.udp.GatewayUDPCodec.decodeResponse;
import static com.github.longkerdandy.viki.home.mi.udp.GatewayUDPCodec.discovery;
import static com.github.longkerdandy.viki.home.mi.udp.GatewayUDPCodec.encodeRequest;
import static com.github.longkerdandy.viki.home.mi.udp.GatewayUDPCodec.list;
import static com.github.longkerdandy.viki.home.mi.udp.GatewayUDPCodec.read;
import static com.github.longkerdandy.viki.home.util.Networks.getIPAddress;

import com.github.longkerdandy.viki.home.mi.model.Gateway;
import com.github.longkerdandy.viki.home.mi.model.ZigbeeThing;
import com.github.longkerdandy.viki.home.mi.schema.SchemaRepository;
import com.github.longkerdandy.viki.home.mi.storage.MiStorage;
import com.github.longkerdandy.viki.home.model.Thing;
import com.github.longkerdandy.viki.home.storage.SQLiteStorage;
import com.github.longkerdandy.viki.home.util.IdGenerator;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Gateway} discovery service
 */
public class GatewayDiscoveryService {

  private static final Logger logger = LoggerFactory.getLogger(GatewayDiscoveryService.class);

  private final SQLiteStorage storage;
  private final MiStorage miStorage;
  private final SchemaRepository schemas;
  private final IdGenerator idGen;
  private final long period;
  private final int duration;
  private final int timeout;
  private final ScheduledExecutorService schedulerGroup;
  private final ExecutorService workerGroup;

  /***
   * Constructor
   *
   * @param storage {@link SQLiteStorage}
   * @param miStorage {@link MiStorage}
   * @param schemas {@link SchemaRepository}
   * @param period between successive executions in milliseconds
   * @param duration of each execution in milliseconds
   * @param timeout socket timeout in milliseconds
   */
  public GatewayDiscoveryService(SQLiteStorage storage, MiStorage miStorage,
      SchemaRepository schemas, long period, int duration, int timeout) {
    this.storage = storage;
    this.miStorage = miStorage;
    this.schemas = schemas;
    this.idGen = new IdGenerator(new SecureRandom());
    this.period = period;
    this.duration = duration;
    this.timeout = timeout;
    this.schedulerGroup = Executors.newScheduledThreadPool(1);
    this.workerGroup = Executors.newFixedThreadPool(4);
  }

  /**
   * Initialize
   */
  @SuppressWarnings("unchecked")
  public void init() {
    this.schedulerGroup.scheduleAtFixedRate(() -> {
      try (DatagramSocket socket = new DatagramSocket()) {
        // Create whois message
        Map<String, Object> whois = new LinkedHashMap<>();
        whois.put("cmd", "whois");

        // Socket options setting
        // Use socket timeout to stop the infinite loop
        socket.setSoTimeout(this.duration);
        SocketAddress remote = new InetSocketAddress(InetAddress.getByName("224.0.0.50"), 4321);

        // Encode and send the whois message
        byte[] input = encodeRequest(whois);
        socket.send(new DatagramPacket(input, input.length, remote));
        logger.debug("Sent 'whois' message to 224.0.0.50");

        // Reused response message buffer
        byte[] buf = new byte[1024];

        // noinspection InfiniteLoopStatement
        while (true) {
          // Receive the iam message, this will block
          DatagramPacket packet = new DatagramPacket(buf, buf.length);
          socket.receive(packet);
          Map<String, Object> iam = decodeResponse(packet.getData());
          logger.debug("Received 'iam' message from {}", getIPAddress(packet.getAddress()));

          // Dispatch a new worker thread to handle future discovery.
          // Worker thread will list(discovery) gateway and loop each associated zigbee thing,
          // then inert or update to extension storage and core storage, emit events if necessary.
          this.workerGroup.submit(() -> {
            try {
              Gateway gateway;
              List<String> sids;
              InetAddress address = InetAddress.getByName((String) iam.get("ip"));
              Integer port = (Integer) iam.get("port");

              // List gateway, protocol 1.x
              if ("gateway".equals(iam.get("model"))) {
                Map<String, Object> listAck = list(address, port, this.timeout);
                gateway = new Gateway((String) listAck.get("sid"), (String) iam.get("model"),
                    (String) iam.get("proto_version"), null,
                    (String) listAck.get("token"), address, port);
                sids = (List<String>) listAck.get("data");
                sids.add(gateway.getGid());
              }

              // Discovery gateway, protocol 2.x
              // To retrieve protocol version, read gateway after discovery
              else if ("gateway.aq1".equals(iam.get("model"))) {
                Map<String, Object> discoveryRsp = discovery(address, port, this.timeout);
                Map<String, Object> readRsp = read((String) discoveryRsp.get("sid"),
                    address, port, this.timeout);
                gateway = new Gateway((String) discoveryRsp.get("sid"),
                    (String) iam.get("model"),
                    (String) ((Map<String, Object>) readRsp.get("params")).get("proto_version"),
                    null, (String) discoveryRsp.get("token"), address, port);
                sids = new ArrayList<>(
                    ((Map<String, Object>) discoveryRsp.get("params")).keySet());
                sids.add(gateway.getGid());
              }

              // Unsupported gateway model
              else {
                logger.debug("Unsupported gateway model {}", iam.get("model"));
                return;
              }

              // Insert or update gateway
              if (this.miStorage.insetGateway(gateway)) {
                logger.debug("Gateway {} has been inserted", gateway);
                // TODO: Send extension event
              } else if (this.miStorage.updateGateway(gateway)) {
                logger.debug("Gateway {} has been updated", gateway);
              } else {
                logger.warn("Failed to insert or update gateway {}", gateway);
                return;
              }

              // Loop and read zigbee things associated with gateway
              // Insert or each zigbee thing and thing, emit events if necessary
              for (String sid : sids) {
                Map<String, Object> readRsp = read(sid, address, port, this.timeout);
                String tid = this.idGen.nextId();
                String model = (String) readRsp.get("model");
                ZigbeeThing zigbeeThing = new ZigbeeThing(sid, gateway.getGid(), tid, model,
                    readRsp.containsKey("short_id") ? (Integer) readRsp.get("short_id") : null);
                // If ZigbeeThing is new, Thing will be inserted and event will be emitted
                if (this.miStorage.insetZigbeeThing(zigbeeThing)) {
                  logger.debug("ZigbeeThing {} has been inserted", zigbeeThing);
                  Thing t = zigbeeThing.toThing(this.schemas.getSchemaByModel(model), readRsp);
                  if (this.storage.insertThingWithProperties(t)) {
                    logger.debug("Thing {} and its properties has been inserted", t);
                    // TODO: Send thing event
                  } else {
                    logger.error("Failed to insert Thing {}, data maybe corrupted", t);
                  }
                }
                // If ZigbeeThing already exists, Thing will not be updated or touched,
                // which leaves to the 'report' and 'heartbeat' operations
                else if (this.miStorage.updateZigbeeThing(zigbeeThing).isPresent()) {
                  logger.debug("ZigbeeThing {} has been updated", zigbeeThing);
                } else {
                  logger.warn("Failed to insert or update zigbee thing {}", zigbeeThing);
                }
              }
            } catch (IOException e) {
              logger.warn("Exception happened when trying to communicate with zigbee things:", e);
            } catch (Exception e) {
              // Catch and logging top level exception here because it isn't the main thread
              logger.error("Error happened when trying to communicate with zigbee things:", e);
            }
          });
        }
      } catch (SocketTimeoutException ignore) {
        // Timeout is the expected behavior, when there is no more iam response, the loop will end
      } catch (IOException e) {
        logger.warn("Exception happened when trying to discover zigbee things:", e);
      } catch (Exception e) {
        // Catch and logging top level exception here because it isn't the main thread
        logger.error("Error happened when trying to discover zigbee things:", e);
      }
    }, 0, this.period, TimeUnit.MILLISECONDS);
  }

  /**
   * Destroy
   */
  public void destroy() {
    this.workerGroup.shutdown();
    this.schedulerGroup.shutdown();
  }
}
