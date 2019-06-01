package com.github.longkerdandy.viki.home.hap.http.handler;

import static com.github.longkerdandy.viki.home.hap.util.Handlers.MIME_TLV8;
import static com.github.longkerdandy.viki.home.hap.util.Handlers.errorTLVResponse;
import static com.github.longkerdandy.viki.home.hap.util.Handlers.successTLVResponse;
import static com.github.longkerdandy.viki.home.hap.util.Handlers.writeResponse;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

import com.github.longkerdandy.viki.home.hap.http.tlv.TLVDecoder;
import com.github.longkerdandy.viki.home.hap.http.tlv.TLVEncoder;
import com.github.longkerdandy.viki.home.hap.http.tlv.TLVError;
import com.github.longkerdandy.viki.home.hap.http.tlv.TLVMethod;
import com.github.longkerdandy.viki.home.hap.http.tlv.TLVType;
import com.github.longkerdandy.viki.home.hap.mdns.HAPmDNSAdvertiser;
import com.github.longkerdandy.viki.home.hap.model.Pairing;
import com.github.longkerdandy.viki.home.hap.storage.HAPStorage;
import com.github.longkerdandy.viki.home.hap.storage.Registry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.AttributeKey;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles Pairings Management
 */
public class PairingsHandler {

  private static final Logger logger = LoggerFactory.getLogger(PairingsHandler.class);

  // HomeKit Accessory Protocol related storage
  private final HAPStorage hapStorage;
  // mDNS
  private final HAPmDNSAdvertiser advertiser;
  // session registry
  private Registry registry;

  /**
   * Constructor
   *
   * @param hapStorage {@link HAPStorage}
   * @param registry {@link Registry}
   * @param advertiser {@link HAPmDNSAdvertiser}
   */
  public PairingsHandler(HAPStorage hapStorage, Registry registry, HAPmDNSAdvertiser advertiser) {
    this.hapStorage = hapStorage;
    this.registry = registry;
    this.advertiser = advertiser;
  }

  /**
   * The method is called once we have received the complete {@link FullHttpRequest}, and the local
   * url is <code>"/pairings"</code>.
   *
   * Used for adding, removing, and listing Parings.
   *
   * @param ctx {@link ChannelHandlerContext}
   * @param request {@link FullHttpRequest}
   */
  public void handle(ChannelHandlerContext ctx, FullHttpRequest request) {
    logger.debug("Received pairing message from {}", ctx.channel().remoteAddress());

    // Check HTTP method and MIME type
    // Pairing requests are performed by sending a POST request to the accessory's HTTP server.
    // The MIME type for requests and responses is application/pairing+tlv8.
    HttpMethod httpMethod = request.method();
    String contentType = request.headers().get(CONTENT_TYPE);
    if (httpMethod != POST || contentType == null || !contentType.equalsIgnoreCase(MIME_TLV8)) {
      logger.warn("Pairing message with invalid HTTP Method {} and Content-Type {}",
          httpMethod, contentType);
      errorTLVResponse(ctx, METHOD_NOT_ALLOWED, TLVError.UNKNOWN);
      return;
    }

    // Parse TLV8 HTTP body
    ByteBuf buffer = request.content();
    Map<TLVType, ?> tlv = TLVDecoder.decode(buffer);

    if (!tlv.containsKey(TLVType.METHOD)) {
      logger.warn("Pairing message does not have TLV Method");
      errorTLVResponse(ctx, BAD_REQUEST, TLVError.UNKNOWN);
      return;
    }
    TLVMethod method = (TLVMethod) tlv.get(TLVType.METHOD);
    switch (method) {
      case ADD_PAIRING:
        handleAddPairing(ctx, tlv);
        break;
      case REMOVE_PAIRING:
        handleRemovePairing(ctx, tlv);
        break;
      case LIST_PAIRINGS:
        handleListPairing(ctx, tlv);
        break;
      default:
        logger.warn("Pairing message with unknown TLV Method {}", method);
        errorTLVResponse(ctx, BAD_REQUEST, TLVError.UNKNOWN);
    }
  }

  /**
   * Handle add pairing M1 state and return M2 state
   *
   * M1: iOS Device -> Accessory -- `Add Pairing Request'
   *
   * The iOS device performs the following steps: 1. Get the Pairing Identifier,
   * AdditionalControllerPairingIdentifier, and the Ed25519 long-term public key of the additional
   * controller to pair, AdditionalControllerLTPK, via an out-of-band mechanism. 2. Construct the
   * request TLV with the following items: kTLVType_State <M1> kTLVType_Method <Add Pairing>
   * kTLVType_Identifier AdditionalControllerPairingIdentifier kTLVType_PublicKey
   * AdditionalControllerLTPK kTLVType_Permissions AdditionalControllerPermissions 3. Send the TLV
   * over the HAP session established via Pair Verify, which provides bidirectional, authenticated
   * encryption.
   *
   * M2: Accessory -> iOS Device -- `Add Pairing Response'
   *
   * When the accessory receives the request, it must perform the following steps: 1. Validate the
   * received data against the established HAP session as described in the transport-specific
   * chapters. 2. Verify that the controller sending the request has the admin bit set in the local
   * pairings list. If not, accessory must abort and respond with the following TLV items:
   * kTLVType_State <M2> kTLVType_Error kTLVError_Authentication 3. If a pairing for
   * AdditionalControllerPairingIdentifier exists, it must perform the following steps: a. If the
   * AdditionalControllerLTPK does not match the stored long-term public key for
   * AdditionalControllerPairingIdentifier, respond with the following TLV items: kTLVType_State
   * <M2> kTLVType_Error kTLVError_Unknown b. Update the permissions of the controller to match
   * AdditionalControllerPermissions. 4. Otherwise, if a pairing for AdditionalControllerPairingIdentifier
   * does not exist, it must perform the following steps: a. Check if the accessory has space to
   * support an additional pairing; the minimum number of supported pairings is 16 pairings. If not,
   * accessory must abort and respond with the following TLV items: kTLVType_State <M2>
   * kTLVType_Error kTLVError_MaxPeers b. Save the additional controller's
   * AdditionalControllerPairingIdentifier, AdditionalControllerLTPK and
   * AdditionalControllerPermissions to a persistent store. If an error occurs while saving,
   * accessory must abort and respond with the following TLV items: kTLVType_State <M2>
   * kTLVType_Error kTLVError_Unknown 5. Construct a response with the following TLV items:
   * kTLVType_State <M2> 6. Send the response over the HAP session established via Pair Verify,
   * which provides bidirectional, authenticated encryption.
   *
   * When the iOS device receives this response, it performs the following steps: 1. Validate the
   * received data against the established HAP session. 2. Validate that the received TLV contains
   * no errors. 3. Send the accessory's long-term public key and Pairing Identifier to the
   * additional controller via an out-of-band mechanism.
   *
   * @param ctx {@link ChannelHandlerContext}
   * @param tlv TLV Items
   */
  protected void handleAddPairing(ChannelHandlerContext ctx, Map<TLVType, ?> tlv) {
    logger.debug("Handle add pairing message with M1 state");

    // Check state
    int state = tlv.containsKey(TLVType.STATE) ? (Integer) tlv.get(TLVType.STATE) : 0;
    if (state != 1) {
      logger.warn("Add pairing message with invalid TLV State {}", state);
      errorTLVResponse(ctx, BAD_REQUEST, TLVError.UNKNOWN);
      return;
    }

    // Load iOSDevicePairingID
    AttributeKey<String> k = AttributeKey.valueOf("iOSDevicePairingID");
    String iOSDevicePairingID = ctx.channel().attr(k).get();

    // Check verify status
    if (iOSDevicePairingID == null) {
      logger.error("Missing iOSDevicePairingID from pair verify stage");
      errorTLVResponse(ctx, BAD_REQUEST, 2, TLVError.AUTHENTICATION);
      return;
    }

    // Check iOSDevice has admin permission
    Optional<Pairing> pairing = this.hapStorage.getPairingById(iOSDevicePairingID);
    if (pairing.isEmpty() || pairing.get().getPermissions() != 1) {
      logger.warn("The iOSDevice does not have administrator permission");
      errorTLVResponse(ctx, BAD_REQUEST, 2, TLVError.AUTHENTICATION);
      return;
    }

    // Save additional controller's pairing
    String AdditionalControllerPairingIdentifier = (String) tlv.get(TLVType.IDENTIFIER);
    byte[] AdditionalControllerLTPK = (byte[]) tlv.get(TLVType.PUBLIC_KEY);
    int AdditionalControllerPermissions = (Integer) tlv.get(TLVType.PERMISSIONS);
    Pairing additionalPairing = new Pairing(AdditionalControllerPairingIdentifier,
        AdditionalControllerLTPK, AdditionalControllerPermissions);
    if (!this.hapStorage.savePairing(additionalPairing)) {
      logger.warn("Fail to add AdditionalControllerPairingIdentifier and AdditionalControllerLTPK");
      errorTLVResponse(ctx, BAD_REQUEST, 2, TLVError.UNKNOWN);
      return;
    }

    // Construct the response
    LinkedHashMap<TLVType, Object> result = new LinkedHashMap<>(1);
    result.put(TLVType.STATE, 2);

    logger.debug("Send back add pairing message with M2 state");
    // Send the response to the iOS device
    successTLVResponse(ctx, result);
  }

  /**
   * Handle remove pairing M1 state and return M2 state
   *
   * This is used to remove a previously established pairing. Remove Pairing can only be performed
   * by admin controllers, that have established a secure session with the accessory using the Pair
   * Verify procedure. Authenticated encryption is used for all encrypted data. After a remove
   * pairing is completed, the accessory must tear down any existing connections with the removed
   * controller within 5 seconds. Until those existing connections are closed, the accessory must
   * refuse any requests, by returning the appropriate transport specific HAP Status codes in the
   * response: 1. IP: -70401 If the last remaining admin controller pairing is removed, all pairings
   * on the accessory must be removed.
   *
   * M1: iOS Device -> Accessory -- `Remove Pairing Request'
   *
   * The iOS device performs the following steps: 1. Get the Pairing Identifier of the additional
   * controller to remove, RemovedControllerPairingIdentifier, via an out-of-band mechanism. 2.
   * Construct the request TLV with the following items: kTLVType_State <M1> kTLVType_Method <Remove
   * Pairing> kTLVType_Identifier RemovedControllerPairingIdentifier 3. Send the TLV over the HAP
   * session established via Pair Verify (page 47), which provides bidirectional, authenticated
   * encryption.
   *
   * M2: Accessory -> iOS Device -- `Remove Pairing Response'
   *
   * When the accessory receives the request, it must perform the following steps: 1. Validate the
   * received data against the established HAP session as described in the transport-specific
   * chapters. 2. Verify that the controller sending the request has the admin bit set in the local
   * pairings list. If not, accessory must abort and respond with the following TLV items:
   * kTLVType_State <M2> kTLVType_Error kTLVError_Authentication 3. If the pairing exists, remove
   * RemovedControllerPairingIdentifier and its corresponding long-term public key from persistent
   * storage. If a pairing for RemovedControllerPairingIdentifier does not exist, the accessory must
   * return success. Otherwise, if an error occurs during removal, accessory must abort and respond
   * with the following TLV items: kTLVType_State <M2> kTLVType_Error kTLVError_Unknown 4. Construct
   * a response with the following TLV items: kTLVType_State <M2> 5. Send the response over the HAP
   * session established via Pair Verify (page 47), which provides bidirectional, authenticated
   * encryption. 6. If the controller requested the accessory to remove its own pairing the
   * accessory must invalidate the HAP session immediately after the response is sent. 7. If there
   * are any established HAP sessions with the controller that was removed, then these connections
   * must be immediately torn down.
   *
   * @param ctx {@link ChannelHandlerContext}
   * @param tlv TLV Items
   */
  protected void handleRemovePairing(ChannelHandlerContext ctx, Map<TLVType, ?> tlv) {
    logger.debug("Handle remove pairing message with M1 state");

    // Check state
    int state = tlv.containsKey(TLVType.STATE) ? (Integer) tlv.get(TLVType.STATE) : 0;
    if (state != 1) {
      logger.warn("Remove pairing message with invalid TLV State {}", state);
      errorTLVResponse(ctx, BAD_REQUEST, TLVError.UNKNOWN);
      return;
    }

    // Load iOSDevicePairingID
    AttributeKey<String> k = AttributeKey.valueOf("iOSDevicePairingID");
    String iOSDevicePairingID = ctx.channel().attr(k).get();

    // Check verify status
    if (iOSDevicePairingID == null) {
      logger.error("Missing iOSDevicePairingID from pair verify stage");
      errorTLVResponse(ctx, BAD_REQUEST, 2, TLVError.AUTHENTICATION);
      return;
    }

    // Check iOSDevice has admin permission
    Optional<Pairing> pairing = this.hapStorage.getPairingById(iOSDevicePairingID);
    if (pairing.isEmpty() || pairing.get().getPermissions() != 1) {
      logger.warn("The iOSDevice does not have administrator permission");
      errorTLVResponse(ctx, BAD_REQUEST, 2, TLVError.AUTHENTICATION);
      return;
    }

    // Remove remote controller's pairing
    String RemovedControllerPairingIdentifier = (String) tlv.get(TLVType.IDENTIFIER);
    this.hapStorage.removePairingById(RemovedControllerPairingIdentifier);

    logger.debug("Send back remove pairing message with M2 state");
    // Construct the response
    LinkedHashMap<TLVType, Object> result = new LinkedHashMap<>(1);
    result.put(TLVType.STATE, 2);

    // Send the response to the iOS device
    ByteBuf content = TLVEncoder.encode(result);
    if (iOSDevicePairingID.equals(RemovedControllerPairingIdentifier)) {
      writeResponse(ctx, MIME_TLV8, OK, content, false);
    } else {
      writeResponse(ctx, MIME_TLV8, OK, content, true);

      // Tear down RemovedControllerPairingIdentifier's connection
      ChannelHandlerContext c = this.registry.getSession(RemovedControllerPairingIdentifier);
      if (c != null) {
        logger.debug("Disconnecting required controller {}", RemovedControllerPairingIdentifier);
        c.close();
      }
    }

    // Clear parings is no admin controller exist
    List<Pairing> pairings = this.hapStorage.clearPairingsIfNoAdmin();

    // Tear down any exist connections
    for (Pairing p : pairings) {
      ChannelHandlerContext c = this.registry.getSession(p.getParingId());
      if (c != null) {
        logger.debug("Disconnecting {} due to no administrator controller exist", p.getParingId());
        c.close();
      }
    }

    pairings = this.hapStorage.getPairings();
    if (pairings.isEmpty()) {
      // Mark the bridge as unpaired if necessary
      this.hapStorage.changeBridgeStatus(1);

      // Reload mDNS service
      this.advertiser.reloadBridgeServiceAsync();
    }
  }

  /**
   * Handle list pairing M1 state and return M2 state
   *
   * This is used to remove a previously established pairing. Remove Pairing can only be performed
   * by admin controllers, that have established a secure session with the accessory using the Pair
   * Verify procedure. Authenticated encryption is used for all encrypted data. After a remove
   * pairing is completed, the accessory must tear down any existing connections with the removed
   * controller within 5 seconds. Until those existing connections are closed, the accessory must
   * refuse any requests, by returning the appropriate transport specific HAP Status codes in the
   * response: 1. IP: -70401 If the last remaining admin controller pairing is removed, all pairings
   * on the accessory must be removed.
   *
   * M1: iOS Device -> Accessory -- `Remove Pairing Request'
   *
   * The iOS device performs the following steps: 1. Get the Pairing Identifier of the additional
   * controller to remove, RemovedControllerPairingIdentifier, via an out-of-band mechanism. 2.
   * Construct the request TLV with the following items: kTLVType_State <M1> kTLVType_Method <Remove
   * Pairing> kTLVType_Identifier RemovedControllerPairingIdentifier 3. Send the TLV over the HAP
   * session established via Pair Verify (page 47), which provides bidirectional, authenticated
   * encryption.
   *
   * M2: Accessory -> iOS Device -- `Remove Pairing Response'
   *
   * When the accessory receives the request, it must perform the following steps: 1. Validate the
   * received data against the established HAP session as described in the transport-specific
   * chapters. 2. Verify that the controller sending the request has the admin bit set in the local
   * pairings list. If not, accessory must abort and respond with the following TLV items:
   * kTLVType_State <M2> kTLVType_Error kTLVError_Authentication 3. If the pairing exists, remove
   * RemovedControllerPairingIdentifier and its corresponding long-term public key from persistent
   * storage. If a pairing for RemovedControllerPairingIdentifier does not exist, the accessory must
   * return success. Otherwise, if an error occurs during removal, accessory must abort and respond
   * with the following TLV items: kTLVType_State <M2> kTLVType_Error kTLVError_Unknown 4. Construct
   * a response with the following TLV items: kTLVType_State <M2> 5. Send the response over the HAP
   * session established via Pair Verify (page 47), which provides bidirectional, authenticated
   * encryption. 6. If the controller requested the accessory to remove its own pairing the
   * accessory must invalidate the HAP session immediately after the response is sent. 7. If there
   * are any established HAP sessions with the controller that was removed, then these connections
   * must be immediately torn down.
   *
   * @param ctx {@link ChannelHandlerContext}
   * @param tlv TLV Items
   */
  protected void handleListPairing(ChannelHandlerContext ctx, Map<TLVType, ?> tlv) {
    logger.debug("Handle list pairing message with M1 state");

    // Check state
    int state = tlv.containsKey(TLVType.STATE) ? (Integer) tlv.get(TLVType.STATE) : 0;
    if (state != 1) {
      logger.warn("List pairing message with invalid TLV State {}", state);
      errorTLVResponse(ctx, BAD_REQUEST, TLVError.UNKNOWN);
      return;
    }

    // Load iOSDevicePairingID
    AttributeKey<String> k = AttributeKey.valueOf("iOSDevicePairingID");
    String iOSDevicePairingID = ctx.channel().attr(k).get();

    // Check verify status
    if (iOSDevicePairingID == null) {
      logger.error("Missing iOSDevicePairingID from pair verify stage");
      errorTLVResponse(ctx, BAD_REQUEST, 2, TLVError.AUTHENTICATION);
      return;
    }

    // Check iOSDevice has admin permission
    Optional<Pairing> pairing = this.hapStorage.getPairingById(iOSDevicePairingID);
    if (pairing.isEmpty() || pairing.get().getPermissions() != 1) {
      logger.warn("The iOSDevice does not have administrator permission");
      errorTLVResponse(ctx, BAD_REQUEST, 2, TLVError.AUTHENTICATION);
      return;
    }

    // Load all the pairings
    List<Pairing> pairings = this.hapStorage.getPairings();

    // Construct the response
    LinkedHashMap<TLVType, Object> result = new LinkedHashMap<>(pairings.size() * 4);
    result.put(TLVType.STATE, 2);
    for (Pairing p : pairings) {
      if (result.size() > 1) {
        result.put(TLVType.SEPARATOR, null);
      }
      result.put(TLVType.IDENTIFIER, p.getParingId());
      result.put(TLVType.PUBLIC_KEY, p.getPublicKey());
      result.put(TLVType.PERMISSIONS, p.getPermissions());
    }

    logger.debug("Send back list pairing message with M2 state");
    // Send the response to the iOS device
    successTLVResponse(ctx, result);
  }
}
