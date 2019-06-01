package com.github.longkerdandy.viki.home.mi.udp;

import static com.github.longkerdandy.viki.home.util.Jacksons.checkInteger;
import static com.github.longkerdandy.viki.home.util.Jacksons.checkString;
import static com.github.longkerdandy.viki.home.util.Jacksons.getInteger;
import static com.github.longkerdandy.viki.home.util.Jacksons.getString;
import static com.github.longkerdandy.viki.home.util.Networks.getIPAddress;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.longkerdandy.viki.home.mi.model.Gateway;
import com.github.longkerdandy.viki.home.util.Jacksons;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.GeneralSecurityException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Gateway} UDP protocol codec
 */
public class GatewayUDPCodec {

  private static final Logger logger = LoggerFactory.getLogger(GatewayUDPCodec.class);

  private static final byte[] IV = new byte[]{0x17, (byte) 0x99, 0x6d, 0x09, 0x3d, 0x28,
      (byte) 0xdd, (byte) 0xb3, (byte) 0xba, 0x69, 0x5a, 0x2e, 0x6f, 0x58, 0x56, 0x2e};

  private GatewayUDPCodec() {
  }

  /**
   * Send get_id_list message and receive get_id_list_ack message. (Protocol 1.x)
   *
   * @param address Remote address
   * @param port Port
   * @param timeout Socket timeout in milliseconds
   * @return get_id_list_ack message
   * @throws IOException when communication failed or timeout
   */
  public static Map<String, Object> list(InetAddress address, int port, int timeout)
      throws IOException {
    Map<String, Object> message = new LinkedHashMap<>();
    message.put("cmd", "get_id_list");
    SocketAddress remote = new InetSocketAddress(address, port);
    return unicast(message, remote, timeout);
  }

  /**
   * Send discovery message and receive discovery_rsp message. (Protocol 2.x)
   *
   * @param address Remote address
   * @param port Port
   * @param timeout Socket timeout in milliseconds
   * @return discovery_rsp message
   * @throws IOException when communication failed or timeout
   */
  public static Map<String, Object> discovery(InetAddress address, int port, int timeout)
      throws IOException {
    Map<String, Object> message = new LinkedHashMap<>();
    message.put("cmd", "discovery");
    SocketAddress remote = new InetSocketAddress(address, port);
    return unicast(message, remote, timeout);
  }

  /**
   * Send read message and receive read_ack or read_rsp message. (Protocol 1.x & 2.x)
   *
   * @param sid Sid
   * @param address Remote address
   * @param port Port
   * @param timeout Socket timeout in milliseconds
   * @return read_ack or read_rsp message
   * @throws IOException when communication failed or timeout
   */
  public static Map<String, Object> read(String sid, InetAddress address, int port, int timeout)
      throws IOException {
    Map<String, Object> message = new LinkedHashMap<>();
    message.put("cmd", "read");
    message.put("sid", sid);
    SocketAddress remote = new InetSocketAddress(address, port);
    return unicast(message, remote, timeout);
  }

  /**
   * Send write message and receive write_ack message. (Protocol 1.x)
   *
   * @param model Model
   * @param sid Sid
   * @param shortId Short id
   * @param key Api key
   * @param data Data
   * @param address Remote address
   * @param port Port
   * @param timeout Socket timeout in milliseconds
   * @return write_ack message
   * @throws IOException when communication failed or timeout
   */
  public static Map<String, Object> write(String model, String sid, int shortId, String key,
      Map<String, Object> data, InetAddress address, int port, int timeout) throws IOException {
    Map<String, Object> message = new LinkedHashMap<>();
    message.put("cmd", "write");
    message.put("model", model);
    message.put("sid", sid);
    message.put("short_id", shortId);
    data.put("key", key);
    message.put("data", data);
    SocketAddress remote = new InetSocketAddress(address, port);
    return unicast(message, remote, timeout);
  }

  /**
   * Send write message and receive write_rsp message. (Protocol 2.x)
   *
   * @param model Model
   * @param sid Sid
   * @param key Api key
   * @param params Parameters
   * @param address Remote address
   * @param port Port
   * @param timeout Socket timeout in milliseconds
   * @return write_ack message
   * @throws IOException when communication failed or timeout
   */
  public static Map<String, Object> write(String model, String sid, String key,
      Map<String, Object> params, InetAddress address, int port, int timeout) throws IOException {
    Map<String, Object> message = new LinkedHashMap<>();
    message.put("cmd", "write");
    message.put("model", model);
    message.put("sid", sid);
    params.put("key", key);
    message.put("params", params);
    SocketAddress remote = new InetSocketAddress(address, port);
    return unicast(message, remote, timeout);
  }

  /**
   * Send unicast request message and receive response
   *
   * @param message Message
   * @param remote Remote address
   * @param timeout Socket timeout in milliseconds
   * @return Response message
   * @throws IOException when communication failed or timeout
   */
  public static Map<String, Object> unicast(Map<String, Object> message, SocketAddress remote,
      int timeout) throws IOException {
    try (DatagramSocket socket = new DatagramSocket()) {
      // socket options
      socket.setSoTimeout(timeout);

      // encode and send the message
      byte[] input = encodeRequest(message);
      socket.send(new DatagramPacket(input, input.length, remote));
      logger.debug("Sent {} message to {}", message.get("cmd"), getIPAddress(remote));

      // receive the acknowledge message, this will block
      byte[] output = new byte[1024];  // magic number
      DatagramPacket p = new DatagramPacket(output, output.length);
      socket.receive(p);

      // decode the received message and return
      Map<String, Object> rsp = decodeResponse(p.getData());
      logger.debug("Received {} message to {}", rsp.get("cmd"), getIPAddress(remote));
      return rsp;
    }
  }

  /**
   * Generate encrypted key with AES CBC
   *
   * @param key Gateway key
   * @param token Gateway token
   * @return Encrypted key
   */
  public static String encryptKey(String key, String token) {
    try {
      IvParameterSpec iv = new IvParameterSpec(IV);
      SecretKeySpec spec = new SecretKeySpec(key.getBytes(UTF_8), "AES");
      Cipher cipher = Cipher.getInstance("AES/CBC/NoPADDING");
      cipher.init(Cipher.ENCRYPT_MODE, spec, iv);
      byte[] encrypted = cipher.doFinal(token.getBytes(UTF_8));
      return Hex.encodeHexString(encrypted, false);
    } catch (GeneralSecurityException e) {
      // never happens
      throw new IllegalStateException("Exception during AES CBC encryption", e);
    }
  }

  /**
   * Encode message to json
   *
   * @param message Message in the format of {@link Map}
   * @return JSON byte[]
   * @throws IOException when encoding failed
   */
  public static byte[] encodeRequest(Map<String, Object> message) throws IOException {
    // protocol v1.x
    if (message.containsKey("data") && message.get("data") != null) {
      message.put("data", Jacksons.getWriter().writeValueAsString(message.get("data")));
    }
    // protocol v2.x
    if (message.containsKey("params") && message.get("params") instanceof Map
        && message.get("params") != null) {
      message.put("params", ((Map) message.get("params")).entrySet());
    }
    return Jacksons.getWriter().writeValueAsBytes(message);
  }

  /**
   * Decode json to response message
   *
   * @param json JSON byte[]
   * @return Response message in the format of {@link Map}
   * @throws IOException when decoding failed
   */
  public static Map<String, Object> decodeResponse(byte[] json) throws IOException {
    // ordered map
    Map<String, Object> result = new LinkedHashMap<>();

    // read into JsonNode
    JsonNode node = Jacksons.getMapper().readTree(json);

    // parse command field
    String cmd = checkString(node, "cmd");
    if (!Set.of("iam", "get_id_list_ack", "discovery_rsp", "report", "heartbeat",
        "read_ack", "write_ack", "read_rsp", "write_rsp").contains(cmd)) {
      throw new IOException("Unknown command type " + cmd);
    }
    result.put("cmd", cmd);

    // parse iam (ip, port fields etc)
    if ("iam".equals(cmd)) {
      result.put("ip", checkString(node, "ip"));
      result.put("port", checkInteger(node, "port"));
      // protocol v1.x only
      String sid = getString(node, "sid");
      String protocolVersion = getString(node, "proto_version");
      if (sid != null) {
        result.put("sid", sid);
      }
      if (protocolVersion != null) {
        result.put("proto_version", protocolVersion);
      }
      // protocol v2.x only
      String protocol = getString(node, "protocol");
      if (protocol != null) {
        result.put("protocol", protocol);
      }
    }

    // parse model field
    if ("iam".equals(cmd) || "report".equals(cmd) || "heartbeat".equals(cmd)
        || "read_ack".equals(cmd) || "write_ack".equals(cmd)
        || "read_rsp".equals(cmd) || "write_rsp".equals(cmd)) {
      result.put("model", checkString(node, "model"));
    }

    // parse sid field
    if ("get_id_list_ack".equals(cmd) || "discovery_rsp".equals(cmd)
        || "report".equals(cmd) || "heartbeat".equals(cmd)
        || "read_ack".equals(cmd) || "write_ack".equals(cmd)
        || "read_rsp".equals(cmd) || "write_rsp".equals(cmd)) {
      result.put("sid", checkString(node, "sid"));
    }

    // parse short id
    // protocol v1.x only
    if ("report".equals(cmd) || "heartbeat".equals(cmd)
        || "read_ack".equals(cmd) || "write_ack".equals(cmd)) {
      Integer shortId = getInteger(node, "short_id");
      if (shortId != null) {
        result.put("short_id", shortId);
      }
    }

    // parse token field
    if ("get_id_list_ack".equals(cmd) || "discovery_rsp".equals(cmd)
        || ("heartbeat".equals(cmd) && node.hasNonNull("token"))) {
      result.put("token", checkString(node, "token"));
    }

    // parse data field
    // protocol v1.x only
    if (("get_id_list_ack".equals(cmd) && node.hasNonNull("data"))) {
      List<String> data = Jacksons.getReader(new TypeReference<List<String>>() {
      }).readValue(node.path("data").asText());
      result.put("data", data);
    }

    // parse device list field
    // protocol v2.x only
    if ("discovery_rsp".equals(cmd)) {
      LinkedHashMap<String, String> devices = new LinkedHashMap<>();
      JsonNode devicesNode = node.path("dev_list");
      if (devicesNode.isArray()) {
        for (JsonNode subNode : devicesNode) {
          if (subNode.isObject()
              && subNode.hasNonNull("sid") && subNode.hasNonNull("model")) {
            devices.put(subNode.path("sid").asText(), subNode.path("model").asText());
          }
        }
      }
      result.put("dev_list", devices);
    }

    // parse data or params field
    if ("report".equals(cmd) || "heartbeat".equals(cmd)
        || "read_ack".equals(cmd) || "write_ack".equals(cmd)
        || "read_rsp".equals(cmd) || "write_rsp".equals(cmd)) {
      // protocol v1.x only
      if (node.hasNonNull("data")) {
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        JsonNode dataNode = node.path("data");
        if (dataNode.isTextual()) {
          JsonNode subNode = Jacksons.getMapper().readTree(dataNode.asText());
          if (subNode.isObject()) {
            Iterator<Entry<String, JsonNode>> iterator = subNode.fields();
            while (iterator.hasNext()) {
              Entry<String, JsonNode> entry = iterator.next();
              if (entry.getValue().isIntegralNumber()) {
                data.put(entry.getKey(), entry.getValue().asLong());
              } else if (entry.getValue().isFloatingPointNumber()) {
                data.put(entry.getKey(), entry.getValue().asDouble());
              } else if (entry.getValue().isTextual()) {
                data.put(entry.getKey(), entry.getValue().asText());
              } else if (entry.getValue().isBoolean()) {
                data.put(entry.getKey(), entry.getValue().asBoolean());
              } else if (entry.getValue().isBinary()) {
                data.put(entry.getKey(), entry.getValue().binaryValue());
              }
            }
          }
        }
        result.put("data", data);
      }
      // protocol v2.x only
      if (node.hasNonNull("params")) {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        JsonNode paramsNode = node.path("params");
        if (paramsNode.isArray()) {
          for (JsonNode subNode : paramsNode) {
            if (subNode.isObject()) {
              Iterator<Entry<String, JsonNode>> iterator = subNode.fields();
              while (iterator.hasNext()) {
                Entry<String, JsonNode> entry = iterator.next();
                if (entry.getValue().isIntegralNumber()) {
                  params.put(entry.getKey(), entry.getValue().asLong());
                } else if (entry.getValue().isFloatingPointNumber()) {
                  params.put(entry.getKey(), entry.getValue().asDouble());
                } else if (entry.getValue().isTextual()) {
                  params.put(entry.getKey(), entry.getValue().asText());
                } else if (entry.getValue().isBoolean()) {
                  params.put(entry.getKey(), entry.getValue().asBoolean());
                } else if (entry.getValue().isBinary()) {
                  params.put(entry.getKey(), entry.getValue().binaryValue());
                }
              }
            }
          }
        }
        result.put("params", params);
      }
    }

    return result;
  }
}
