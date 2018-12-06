package com.github.longkerdandy.viki.home.hap.mdns;

import com.github.longkerdandy.viki.home.hap.model.Characteristic;
import com.github.longkerdandy.viki.home.hap.storage.HAPStorage;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * mDNS advertiser for HomeKit Accessory Protocol Bridge
 */
public class HAPmDNSAdvertiser {

  private static final Logger logger = LoggerFactory.getLogger(HAPmDNSAdvertiser.class);

  // mac address
  private final String macAddress;
  // http server port
  private final int port;
  // storage
  private final HAPStorage hapStorage;
  // mDNS
  private final JmDNS jmDNS;

  /**
   * Constructor
   *
   * @param address {@link InetAddress}
   * @param hostname Hostname
   * @param macAddress Mac Address
   * @param port HTTP Server Port
   * @param hapStorage {@link HAPStorage}
   * @throws IOException if an exception occurs during the socket creation
   */
  public HAPmDNSAdvertiser(InetAddress address, String hostname, String macAddress, int port,
      HAPStorage hapStorage) throws IOException {
    this.macAddress = macAddress;
    this.port = port;
    this.hapStorage = hapStorage;
    this.jmDNS = JmDNS.create(address, hostname);
  }

  /**
   * Register the HomeKit Accessory Protocol Bridge service to mDNS.
   *
   * @throws IOException if there is an error in the underlying protocol, such as a TCP error.
   */
  public void registerBridgeService() throws IOException {
    this.jmDNS.registerService(getBridgeServiceInfo());
  }

  /**
   * Re-announce the HomeKit Accessory Protocol Bridge service to mDNS asynchronously.
   */
  public void reloadBridgeServiceAsync() {
    CompletableFuture.runAsync(() -> {
      ServiceInfo[] services = this.jmDNS.list("_hap._tcp.local.");
      services[0].setText(getBridgeProperties());
    }, Executors.newSingleThreadExecutor());
  }

  /**
   * Shutdown the advertiser.
   *
   * @throws IOException if an I/O error occurs
   */
  public void shutdown() throws IOException {
    this.jmDNS.unregisterAllServices();
    this.jmDNS.close();
  }

  /**
   * Build the HomeKit Accessory Protocol Bridge ServiceInfo from storage
   *
   * @return {@link ServiceInfo}
   */
  protected ServiceInfo getBridgeServiceInfo() {
    // Get bridge properties
    HashMap<String, String> props = getBridgeProperties();

    // The name of the Bonjour service is the user-visible name of the accessory, e.g. "LED Bulb M123", and must
    // match the name provided in the Accessory Information Service of the HAP Accessory object that has an
    // instanceID of 1.
    Optional<Characteristic> name = this.hapStorage
        .getCharacteristicByType(1, "00000023-0000-1000-8000-0026BB765291");
    if (name.isEmpty()) {
      throw new IllegalStateException("Bridge's name characteristic not exist.");
    }

    return ServiceInfo.create("_hap._tcp.local.",
        (String) name.get().getValue(), this.port, 0, 0, props);
  }

  /**
   * Build the HomeKit Accessory Protocol Bridge properties from storage
   *
   * @return Properties
   */
  protected HashMap<String, String> getBridgeProperties() {
    // Get bridge information from storage
    Map<String, ?> info = this.hapStorage.getBridgeInformation();

    // Current configuration number. Required.
    // Must update when an accessory, service, or characteristic is added or removed on the accessory
    // server.
    // Accessories must increment the config number after a firmware update.
    // This must have a range of 1-4294967295 and wrap to 1 when it overflows. (down to 2147483647)
    // This value must persist across reboots, power cycles, etc.
    int configNum = (Integer) info.get("config_num");

    // Feature flags (e.g. "0x3" for bits 0 and 1). Required if non-zero.
    int featureFlag = 0;

    // Model name of the accessory (e.g. "Device1,1"). Required.
    Optional<Characteristic> model = this.hapStorage
        .getCharacteristicByType(1, "00000021-0000-1000-8000-0026BB765291");
    if (model.isEmpty()) {
      throw new IllegalStateException("Bridge's model characteristic not exist.");
    }

    // Protocol version string <major>.<minor> (e.g. "1.0"). Required if value is not "1.0".
    String protocolVersion = (String) info.get("protocol_version");

    // Current state number. Required.
    int stateNum = (Integer) info.get("state_num");

    // Status flags (e.g. "0x04" for bit 3). Value should be an unsigned integer. Required.
    int statusFlag = (Integer) info.get("status_flag");

    // Accessory Category Identifier. Required. Indicates the category that best describes the primary
    // function of the accessory. This must have a range of 1-65535.
    // This must persist across reboots, power cycles, etc.
    int categoryId = (Integer) info.get("category_id");

    HashMap<String, String> props = new HashMap<>();
    props.put("c#", String.valueOf(configNum));
    props.put("ff", String.valueOf(featureFlag));
    // Device ID of the accessory. The Device ID must be formatted as
    // "XX:XX:XX:XX:XX:XX", where "XX" is a hexadecimal string representing a byte. Required.
    // This value is also used as the accessory's Pairing Identifier.
    props.put("id", this.macAddress);
    props.put("md", (String) model.get().getValue());
    props.put("pv", protocolVersion);
    props.put("s#", String.valueOf(stateNum));
    props.put("sf", String.valueOf(statusFlag));
    props.put("ci", String.valueOf(categoryId));

    return props;
  }
}
