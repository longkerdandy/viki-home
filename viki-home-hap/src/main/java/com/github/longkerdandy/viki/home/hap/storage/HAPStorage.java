package com.github.longkerdandy.viki.home.hap.storage;

import com.github.longkerdandy.viki.home.hap.model.Characteristic;
import com.github.longkerdandy.viki.home.hap.storage.mapper.CharacteristicMapper;
import java.util.Map;
import org.jdbi.v3.core.Jdbi;

/**
 * Storage Layer for Homekit Accessory Protocol
 */
public class HAPStorage {

  // Jdbi Instance
  private final Jdbi jdbi;
  // Mappers
  private final CharacteristicMapper characteristicMapper;

  /**
   * Constructor
   *
   * @param jdbi Jdbi Instance
   */
  public HAPStorage(Jdbi jdbi) {
    this.jdbi = jdbi;
    this.characteristicMapper = new CharacteristicMapper();
  }

  /**
   * Get bridge information
   *
   * @return Map of Bridge Information
   */
  public Map<String, ?> getBridgeInformation() {
    return this.jdbi.withHandle(handle ->
        handle.createQuery(
            "SELECT aid, config_num, protocol_version, state_num, status_flag, category_id FROM ext_hap_bridge WHERE aid = 1")
            .map((rs, ctx) -> Map.of(
                "aid", rs.getInt("aid"),
                "config_num", rs.getInt("config_num"),
                "protocol_version", rs.getString("protocol_version"),
                "state_num", rs.getInt("state_num"),
                "status_flag", rs.getInt("status_flag"),
                "category_id", rs.getInt("category_id")))
            .findOnly());
  }

  /**
   * Get next accessory id
   *
   * @return Next Accessory Id
   */
  public Long nextAccessoryId() {
    return this.jdbi.inTransaction(handle -> {
      handle.execute("UPDATE ext_hap_bridge SET aid_counter = aid_counter + 1 WHERE aid = 1");
      return handle.createQuery("SELECT aid_counter FROM ext_hap_bridge WHERE aid = 1")
          .mapTo(Long.class)
          .findOnly();
    });
  }

  /**
   * Get Characteristic by Accessory Id and Characteristic Type
   *
   * @param accessoryId Accessory Id
   * @param type Characteristic Type
   * @return Characteristic
   */
  @SuppressWarnings("unchecked")
  public <T> Characteristic<T> getCharacteristicByType(int accessoryId, String type) {
    return this.jdbi.withHandle(handle ->
        handle.createQuery("SELECT * FROM ext_hap_characteristic WHERE aid = :aid AND type = :type")
            .bind("aid", accessoryId)
            .bind("type", type)
            .map(characteristicMapper)
            .findOnly());
  }
}
