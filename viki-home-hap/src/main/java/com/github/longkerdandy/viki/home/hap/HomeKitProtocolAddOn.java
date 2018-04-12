package com.github.longkerdandy.viki.home.hap;

import com.github.longkerdandy.viki.home.addon.ProtocolAddOn;
import com.github.longkerdandy.viki.home.hap.model.Characteristic;
import com.github.longkerdandy.viki.home.hap.storage.HAPStorage;
import com.github.longkerdandy.viki.home.storage.Storage;
import com.github.longkerdandy.viki.home.util.Networks;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import org.apache.commons.configuration2.AbstractConfiguration;

/**
 * HomeKit Accessory Protocol Add-on
 */
public class HomeKitProtocolAddOn implements ProtocolAddOn {

  // port
  private final int port;
  // storage
  private final HAPStorage hapStorage;
  // mDNS
  private final JmDNS jmDNS;

  /**
   * Constructor
   */
  public HomeKitProtocolAddOn(AbstractConfiguration config, Storage storage) {
    this.port = config.getInt("hap.discover.port");
    this.hapStorage = new HAPStorage(storage.getJdbi());
    try {
      // this.jmDNS = JmDNS.create(Networks.getLocalInetAddress());
      this.jmDNS = JmDNS.create(Networks.getLocalInetAddress(), Networks.getLocalHostname());
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public String getAddOnName() {
    return "HomeKit Protocol Add-on";
  }

  @Override
  public void init() {
    initDiscovery();
  }

  /**
   * Initialize Bonjour discovery service
   */
  protected void initDiscovery() {
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
    // Device ID (Device ID (page 36)) of the accessory. The Device ID must be formatted as
    // "XX:XX:XX:XX:XX:XX", where "XX" is a hexadecimal string representing a byte. Required.
    // This value is also used as the accessory's Pairing Identifier.
    String deviceId;
    try {
      deviceId = Networks.getLocalMacAddress();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    // Model name of the accessory (e.g. "Device1,1"). Required.
    Characteristic<String> model = this.hapStorage
        .getCharacteristicByType(1, "00000021-0000-1000-8000-0026BB765291");
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
    props.put("id", deviceId);
    props.put("md", model.getValue());
    props.put("pv", protocolVersion);
    props.put("s#", String.valueOf(stateNum));
    props.put("sf", String.valueOf(statusFlag));
    props.put("ci", String.valueOf(categoryId));

    // The name of the Bonjour service is the user-visible name of the accessory, e.g. "LED Bulb M123", and must
    // match the name provided in the Accessory Information Service of the HAP Accessory object that has an
    // instanceID of 1.
    Characteristic<String> name = this.hapStorage
        .getCharacteristicByType(1, "00000023-0000-1000-8000-0026BB765291");

    // register service
    try {
      this.jmDNS.registerService(
          ServiceInfo.create("_hap._tcp.local.", name.getValue(), this.port, 0, 0, props));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void destroy() {
    // unregister all services
    if (this.jmDNS != null) {
      this.jmDNS.unregisterAllServices();
    }
  }
}
