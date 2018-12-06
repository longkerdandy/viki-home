package com.github.longkerdandy.viki.home.hap.storage;

import static com.github.longkerdandy.viki.home.hap.util.Ciphers.ed25519KeyGen;

import com.github.longkerdandy.viki.home.hap.http.response.Status;
import com.github.longkerdandy.viki.home.hap.http.request.CharacteristicWriteRequestTarget;
import com.github.longkerdandy.viki.home.hap.http.response.CharacteristicWriteResponseTarget;
import com.github.longkerdandy.viki.home.hap.model.Accessory;
import com.github.longkerdandy.viki.home.hap.model.Characteristic;
import com.github.longkerdandy.viki.home.hap.model.Pairing;
import com.github.longkerdandy.viki.home.hap.model.Service;
import com.github.longkerdandy.viki.home.hap.storage.mapper.AccessoryMapper;
import com.github.longkerdandy.viki.home.hap.storage.mapper.BridgeInformationMapper;
import com.github.longkerdandy.viki.home.hap.storage.mapper.CharacteristicMapper;
import com.github.longkerdandy.viki.home.hap.storage.mapper.PairingMapper;
import com.github.longkerdandy.viki.home.hap.storage.mapper.ServiceMapper;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import org.jdbi.v3.core.Jdbi;

/**
 * Storage Layer for HomeKit Accessory Protocol
 */
public class HAPStorage {

  // Jdbi Instance
  private final Jdbi jdbi;

  // Jdbi mappers
  private final BridgeInformationMapper bridgeInformationMapper;
  private final AccessoryMapper accessoryMapper;
  private final ServiceMapper serviceMapper;
  private final CharacteristicMapper characteristicMapper;
  private final PairingMapper pairingMapper;

  /**
   * Constructor
   *
   * @param jdbi Jdbi Instance
   */
  public HAPStorage(Jdbi jdbi) {
    this.jdbi = jdbi;
    this.bridgeInformationMapper = new BridgeInformationMapper();
    this.accessoryMapper = new AccessoryMapper();
    this.serviceMapper = new ServiceMapper();
    this.characteristicMapper = new CharacteristicMapper();
    this.pairingMapper = new PairingMapper();
  }

  /**
   * Initialize {@link HAPStorage}
   */
  public void init() {
    // key generation
    KeyPair keyPair = ed25519KeyGen();
    byte[] privateKey = ((EdDSAPrivateKey) keyPair.getPrivate()).getSeed();
    byte[] publicKey = ((EdDSAPublicKey) keyPair.getPublic()).getAbyte();
    // save key pair if bridge's values are null
    this.jdbi.useHandle(handle ->
        handle.createUpdate(
            "UPDATE ext_hap_bridge SET "
                + "private_key = CASE WHEN private_key IS NULL THEN :privateKey ELSE private_key END, "
                + "public_key = CASE WHEN public_key IS NULL THEN :publicKey ELSE public_key END "
                + "WHERE aid = 1")
            .bind("privateKey", privateKey)
            .bind("publicKey", publicKey)
            .execute()
    );
  }

  /**
   * Get bridge information
   *
   * @return Map of Bridge Information
   */
  public Map<String, ?> getBridgeInformation() {
    return this.jdbi.withHandle(handle ->
        handle.createQuery(
            "SELECT aid, config_num, protocol_version, state_num, status_flag, category_id, private_key, public_key FROM ext_hap_bridge WHERE aid = 1")
            .map(bridgeInformationMapper)
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
   * Change bridge's status_flag 0, means unpaired 1, means paired 2, means not join a Wifi network
   * 3, means has problem
   *
   * @param statusFlag Status Flag indicate paired or not
   */
  public void changeBridgeStatus(int statusFlag) {
    this.jdbi.useHandle(handle ->
        handle.createUpdate("UPDATE ext_hap_bridge SET status_flag = :statusFlag WHERE aid = 1")
            .bind("statusFlag", statusFlag)
            .execute()
    );
  }

  /**
   * Get {@link Accessory} from storage
   *
   * @return List of {@link Accessory}
   */
  public List<Accessory> getAccessories() {
    List<Accessory> accessories = this.jdbi.withHandle(handle ->
        handle.createQuery("SELECT * FROM ext_hap_accessory")
            .map(accessoryMapper)
            .list());
    for (Accessory accessory : accessories) {
      List<Service> services = getServicesByAccessory(accessory.getInstanceId());
      accessory.setServices(services);
    }
    return accessories;
  }

  /**
   * Get {@link Service} by {@code accessoryId}
   *
   * @param accessoryId Accessory Id
   * @return List of {@link Service}
   */
  public List<Service> getServicesByAccessory(long accessoryId) {
    List<Service> services = this.jdbi.withHandle(handle ->
        handle.createQuery("SELECT * FROM ext_hap_service WHERE aid = :aid")
            .bind("aid", accessoryId)
            .map(serviceMapper)
            .list());
    for (Service service : services) {
      List<Characteristic> characteristics = getCharacteristicsByService(
          accessoryId, service.getInstanceId());
      service.setCharacteristics(characteristics);
    }
    return services;
  }

  /**
   * Get {@link Characteristic} by {@code accessoryId} and {@code instanceId}
   *
   * @param accessoryId Accessory Id
   * @param instanceId Instance Id
   * @return List of {@link Characteristic}
   */
  public Optional<Characteristic> getCharacteristicById(long accessoryId, long instanceId) {
    return this.jdbi.withHandle(handle ->
        handle.createQuery("SELECT * FROM ext_hap_characteristic WHERE aid = :aid AND cid = :cid")
            .bind("aid", accessoryId)
            .bind("cid", instanceId)
            .map(characteristicMapper)
            .findFirst());
  }

  /**
   * Get {@link Characteristic} by {@code accessoryId} and {@code serviceId}
   *
   * @param accessoryId Accessory Id
   * @param serviceId Service Id
   * @return List of {@link Characteristic}
   */
  public List<Characteristic> getCharacteristicsByService(long accessoryId, long serviceId) {
    return this.jdbi.withHandle(handle ->
        handle.createQuery("SELECT * FROM ext_hap_characteristic WHERE aid = :aid AND sid = :sid")
            .bind("aid", accessoryId)
            .bind("sid", serviceId)
            .map(characteristicMapper)
            .list());
  }

  /**
   * Get {@link Characteristic} by {@code accessoryId} and {@code type}
   *
   * @param accessoryId Accessory Id
   * @param type Characteristic Type
   * @return Characteristic
   */
  public Optional<Characteristic> getCharacteristicByType(int accessoryId, String type) {
    return this.jdbi.withHandle(handle ->
        handle.createQuery("SELECT * FROM ext_hap_characteristic WHERE aid = :aid AND type = :type")
            .bind("aid", accessoryId)
            .bind("type", type)
            .map(characteristicMapper)
            .findFirst());
  }

  /**
   * Save bunch of {@link CharacteristicWriteRequestTarget} to storage
   *
   * @param targets List of {@link CharacteristicWriteRequestTarget}
   * @return Null if operation succeed or List of {@link CharacteristicWriteResponseTarget} contain
   * error code
   */
  public List<CharacteristicWriteResponseTarget> saveCharacteristics(
      List<CharacteristicWriteRequestTarget> targets) {
    boolean fail = false;
    List<CharacteristicWriteResponseTarget> results = new ArrayList<>();

    for (CharacteristicWriteRequestTarget target : targets) {
      long aid = target.getAccessoryId();
      long iid = target.getInstanceId();

      // At least one of "value" or "ev" will be present in the characteristic write request object.
      if (target.getValue() == null && target.getEnableEvent() == null) {
        results.add(new CharacteristicWriteResponseTarget(aid, iid, Status.INVALID_VALUE));
        fail = true;
        continue;
      }

      // Check characteristic's existence and format
      Optional<Characteristic> characteristic = getCharacteristicById(aid, iid);
      if (characteristic.isEmpty()) {
        results.add(new CharacteristicWriteResponseTarget(aid, iid, Status.RESOURCE_NOT_EXIST));
        fail = true;
        continue;
      }

      // Save characteristic to storage
      String _value = target.getValue() == null ? null : target.getValue().toString();
      Boolean enable_event = target.getEnableEvent();
      this.jdbi.withHandle(handle ->
          handle.createUpdate(
              "UPDATE ext_hap_characteristic SET _value = :_value, enable_event = :enable_event WHERE aid = :aid AND cid = :cid")
              .bind("_value", _value)
              .bind("enable_event", enable_event)
              .bind("aid", aid)
              .bind("cid", iid)
              .execute());
      results.add(new CharacteristicWriteResponseTarget(aid, iid, Status.SUCCESS));
    }

    return fail ? results : null;
  }

  /**
   * Save {@link Pairing} to storage
   *
   * If a pairing for {@code paringId} exists, it must perform the following steps: a. If the {@code
   * publicKey} does not match the stored long-term public key for {@code paringId}, return false.
   * b. Update the permissions of the controller to match {@code permissions}. Otherwise, if a
   * pairing for {@code paringId} does not exist, it must perform the following steps: a. Check if
   * the accessory has space to support an additional pairing; the minimum number of supported
   * pairings is 16 pairings. b. Save the additional controller's {@code paringId}, {@code
   * publicKey} and {@code permissions} to a persistent store.
   *
   * @param pairing to be saved
   * @return True if successful
   */
  public boolean savePairing(Pairing pairing) {
    String pairingId = pairing.getParingId();
    byte[] publicKey = pairing.getPublicKey();
    int permissions = pairing.getPermissions();
    return this.jdbi.inTransaction(handle -> {
      Optional<Pairing> pairingInStorage = handle
          .createQuery("SELECT * FROM ext_hap_pairing WHERE pairing_id = :pairingId")
          .bind("pairingId", pairingId)
          .map(pairingMapper)
          .findFirst();
      if (pairingInStorage.isPresent()) {
        if (!Arrays.equals(publicKey, pairingInStorage.get().getPublicKey())) {
          return false;
        } else if (permissions != pairingInStorage.get().getPermissions()) {
          handle.createUpdate(
              "UPDATE ext_hap_pairing SET permissions = :permissions WHERE pairing_id = :pairingId")
              .bind("permissions", permissions)
              .bind("pairingId", pairingId)
              .execute();
        }
      } else {
        handle.createUpdate(
            "INSERT INTO ext_hap_pairing (pairing_id, public_key, permissions) VALUES (:pairingId, :publicKey, :permissions)")
            .bind("pairingId", pairingId)
            .bind("publicKey", publicKey)
            .bind("permissions", permissions)
            .execute();
      }
      return true;
    });
  }

  /**
   * Get {@link Pairing} by id from storage
   *
   * @param pairingId Pairing Id
   * @return {@link Pairing} may not present
   */
  public Optional<Pairing> getPairingById(String pairingId) {
    return this.jdbi.withHandle(handle ->
        handle.createQuery("SELECT * FROM ext_hap_pairing WHERE pairing_id = :pairingId")
            .bind("pairingId", pairingId)
            .map(pairingMapper)
            .findFirst());
  }

  /**
   * Get {@link Pairing} from storage
   *
   * @return List of {@link Pairing}
   */
  public List<Pairing> getPairings() {
    return this.jdbi.withHandle(handle ->
        handle.createQuery("SELECT * FROM ext_hap_pairing")
            .map(pairingMapper)
            .list());
  }

  /**
   * Remove {@link Pairing} by id from storage
   *
   * @param pairingId Pairing Id
   */
  public void removePairingById(String pairingId) {
    this.jdbi.useHandle(handle ->
        handle.createUpdate("DELETE FROM ext_hap_pairing WHERE pairing_id = :pairingId")
            .bind("pairingId", pairingId)
            .execute());
  }

  /**
   * Clear all the {@link Pairing} if no admin controller left
   *
   * @return List of removed {@link Pairing}
   */
  public List<Pairing> clearPairingsIfNoAdmin() {
    return this.jdbi.inTransaction(handle -> {
      int count = handle
          .createQuery("SELECT count(*) FROM ext_hap_pairing WHERE permissions = 1")
          .mapTo(Integer.class)
          .findOnly();
      if (count == 0) {
        List<Pairing> pairings = handle.createQuery("SELECT * FROM ext_hap_pairing")
            .map(pairingMapper)
            .list();
        handle.createUpdate("DELETE from ext_hap_pairing")
            .execute();
        return pairings;
      } else {
        return Collections.emptyList();
      }
    });
  }
}
