package com.github.longkerdandy.viki.home.hap.http.handler;

import static com.github.longkerdandy.viki.home.hap.util.Ciphers.chaCha20Poly1305Decrypt;
import static com.github.longkerdandy.viki.home.hap.util.Ciphers.chaCha20Poly1305Encrypt;
import static com.github.longkerdandy.viki.home.hap.util.Ciphers.chaCha20Poly1305Nonce;
import static com.github.longkerdandy.viki.home.hap.util.Ciphers.curve25519KeyGen;
import static com.github.longkerdandy.viki.home.hap.util.Ciphers.curve25519SharedSecret;
import static com.github.longkerdandy.viki.home.hap.util.Ciphers.ed25519Sign;
import static com.github.longkerdandy.viki.home.hap.util.Ciphers.ed25519Verify;
import static com.github.longkerdandy.viki.home.hap.util.Ciphers.hkdf;
import static com.github.longkerdandy.viki.home.hap.util.Handlers.MIME_TLV8;
import static com.github.longkerdandy.viki.home.hap.util.Handlers.bufferToBytes;
import static com.github.longkerdandy.viki.home.hap.util.Handlers.bytesToBuffer;
import static com.github.longkerdandy.viki.home.hap.util.Handlers.errorTLVResponse;
import static com.github.longkerdandy.viki.home.hap.util.Handlers.successTLVResponse;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;

import com.github.longkerdandy.viki.home.hap.http.frame.FrameCodec;
import com.github.longkerdandy.viki.home.hap.http.tlv.TLVDecoder;
import com.github.longkerdandy.viki.home.hap.http.tlv.TLVEncoder;
import com.github.longkerdandy.viki.home.hap.http.tlv.TLVError;
import com.github.longkerdandy.viki.home.hap.http.tlv.TLVType;
import com.github.longkerdandy.viki.home.hap.model.Pairing;
import com.github.longkerdandy.viki.home.hap.storage.HAPStorage;
import com.github.longkerdandy.viki.home.hap.storage.Registry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.AttributeKey;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.curve25519.Curve25519KeyPair;

/**
 * Handles Pair Verify
 */
public class PairVerifyHandler {

  private static final Logger logger = LoggerFactory.getLogger(PairVerifyHandler.class);

  // HomeKit Accessory Protocol related storage
  private final HAPStorage hapStorage;

  // mac address
  private final String macAddress;

  // session registry
  private Registry registry;

  /**
   * Constructor
   *
   * @param hapStorage {@link HAPStorage}
   * @param macAddress MAC Address
   * @param registry {@link Registry}
   */
  public PairVerifyHandler(HAPStorage hapStorage, String macAddress, Registry registry) {
    this.hapStorage = hapStorage;
    this.macAddress = macAddress;
    this.registry = registry;
  }

  /**
   * The method is called once we have received the complete {@link FullHttpRequest}, and the local
   * url is <code>"/pair-verify"</code>.
   *
   * Used for Pair Verify.
   *
   * @param ctx {@link ChannelHandlerContext}
   * @param request {@link FullHttpRequest}
   */
  public void handle(ChannelHandlerContext ctx, FullHttpRequest request) {
    logger.debug("Received pair verify message from {}", ctx.channel().remoteAddress());

    // Check HTTP method and MIME type
    // Pairing requests are performed by sending a POST request to the accessory's HTTP server.
    // The MIME type for requests and responses is application/pairing+tlv8.
    HttpMethod httpMethod = request.method();
    String contentType = request.headers().get(CONTENT_TYPE);
    if (httpMethod != POST || contentType == null || !contentType.equalsIgnoreCase(MIME_TLV8)) {
      logger.warn("Pair verify message with invalid HTTP Method {} and Content-Type {}",
          httpMethod, contentType);
      errorTLVResponse(ctx, METHOD_NOT_ALLOWED, TLVError.UNKNOWN);
      return;
    }

    // Parse TLV8 HTTP body
    ByteBuf buffer = request.content();
    Map<TLVType, ?> tlv = TLVDecoder.decode(buffer);

    if (!tlv.containsKey(TLVType.STATE)) {
      logger.warn("Pair verify message does not have TLV State");
      errorTLVResponse(ctx, BAD_REQUEST, TLVError.UNKNOWN);
      return;
    }
    int state = (Integer) tlv.get(TLVType.STATE);
    switch (state) {
      case 1:
        handleM1State(ctx, tlv);
        break;
      case 3:
        handleM3State(ctx, tlv);
        break;
      default:
        logger.warn("Pair verify message with unknown TLV State {}", state);
        errorTLVResponse(ctx, BAD_REQUEST, TLVError.UNKNOWN);
    }
  }

  /**
   * Handle M1 state and return M2 state
   *
   * M1: iOS Device -> Accessory -- `Verify Start Request`
   *
   * The iOS device generates a new, random Curve25519 key pair and sends a request to the accessory
   * with the following TLV items: kTLVType_State <M1> kTLVType_PublicKey <iOS device's Curve25519
   * public key>
   *
   * M2: Accessory -> iOS Device -- `Verify Start Response'
   *
   * When the accessory receives <M1>, it must perform the following steps: 1. Generate new, random
   * Curve25519 key pair. 2. Generate the shared secret, SharedSecret, from its Curve25519 secret
   * key and the iOS device's Curve25519 public key. 3. Construct AccessoryInfo by concatenating the
   * following items in order: a. Accessory's Curve25519 public key. b. Accessory's Pairing
   * Identifier, AccessoryPairingID. c. iOS device's Curve25519 public key from the received <M1>
   * TLV. 4. Use Ed25519 to generate AccessorySignature by signing AccessoryInfo with its long-term
   * secret key, AccessoryLTSK. 5. Construct a sub-TLV with the following items:
   * kTLVType_Identifier
   * <AccessoryPairingID> kTLVType_Signature <AccessorySignature> 6. Derive the symmetric session
   * encryption key, SessionKey, from the Curve25519 shared secret by using HKDF-SHA-512 with the
   * following parameters: InputKey = <Curve25519 shared secret> Salt = "Pair-Verify-Encrypt-Salt"
   * Info = "Pair-Verify-Encrypt-Info" OutputSize = 32 bytes 7. Encrypt the sub-TLV, encryptedData,
   * and generate the 16-byte auth tag, authTag. This uses the ChaCha20-Poly1305 AEAD algorithm with
   * the following parameters: encryptedData, authTag = ChaCha20-Poly1305(SessionKey,
   * Nonce="PV-Msg02", AAD=<none>, Msg=<Sub-TLV>) 8. Construct the response with the following TLV
   * items: kTLVType_State <M2> kTLVType_PublicKey <Accessory's SRP proof> kTLVType_EncryptedData
   * <encryptedData with authTag appended> 9. Send the response to the iOS device.
   *
   * @param ctx {@link ChannelHandlerContext}
   * @param tlv TLV Items
   */
  protected void handleM1State(ChannelHandlerContext ctx, Map<TLVType, ?> tlv) {
    logger.debug("Handle pair verify message with M1 state");

    // Check kTLVType_PublicKey
    if (tlv.get(TLVType.PUBLIC_KEY) == null) {
      logger.warn("Pair verify M1 message missing PublicKey");
      errorTLVResponse(ctx, BAD_REQUEST, 2, TLVError.UNKNOWN);
      return;
    }
    byte[] iOSDevicePK = (byte[]) tlv.get(TLVType.PUBLIC_KEY);

    // Generate SharedSecret
    Curve25519KeyPair AccessoryKeyPair = curve25519KeyGen();
    byte[] SharedSecret = curve25519SharedSecret(iOSDevicePK, AccessoryKeyPair.getPrivateKey());

    // Concatenate AccessoryInfo
    String AccessoryPairingID = this.macAddress;
    byte[] AccessoryInfo = ArrayUtils.addAll(ArrayUtils.addAll(AccessoryKeyPair.getPublicKey(),
        AccessoryPairingID.getBytes(StandardCharsets.UTF_8)), iOSDevicePK);

    // Load AccessoryLTSK
    Map<String, ?> bridgeInfo = this.hapStorage.getBridgeInformation();
    byte[] AccessoryLTSK = (byte[]) bridgeInfo.get("private_key");

    // Use Ed25519 to generate AccessorySignature by signing AccessoryInfo with AccessoryLTSK
    byte[] AccessorySignature;
    try {
      AccessorySignature = ed25519Sign(AccessoryLTSK, AccessoryInfo);
    } catch (GeneralSecurityException e) {
      logger.error("Error when generating AccessorySignature", e);
      errorTLVResponse(ctx, BAD_REQUEST, 2, TLVError.UNKNOWN);
      return;
    }

    // Derive SessionKey from the SharedSecret by using HKDF-SHA-512
    byte[] SessionKey = hkdf(SharedSecret, "Pair-Verify-Encrypt-Salt", "Pair-Verify-Encrypt-Info",
        32);

    // Save iOSDevicePK, SessionKey, AccessoryPK
    ctx.channel().attr(AttributeKey.valueOf("iOSDevicePK")).set(iOSDevicePK);
    ctx.channel().attr(AttributeKey.valueOf("AccessoryPK")).set(AccessoryKeyPair.getPublicKey());
    ctx.channel().attr(AttributeKey.valueOf("SessionKey")).set(SessionKey);
    ctx.channel().attr(AttributeKey.valueOf("SharedSecret")).set(SharedSecret);

    // Construct the sub-TLV
    // Missing document!!!
    LinkedHashMap<TLVType, Object> encodeSubTLV = new LinkedHashMap<>(2);
    encodeSubTLV.put(TLVType.IDENTIFIER, AccessoryPairingID);
    encodeSubTLV.put(TLVType.SIGNATURE, AccessorySignature);

    // Encrypt the sub-TLV to encryptedData and generate the 16 byte auth tag
    byte[] nonce = chaCha20Poly1305Nonce("PV-Msg02");
    byte[] plainText = bufferToBytes(TLVEncoder.encode(encodeSubTLV));
    byte[] cipherText = chaCha20Poly1305Encrypt(SessionKey, plainText, nonce);

    // Construct the response
    LinkedHashMap<TLVType, Object> result = new LinkedHashMap<>(3);
    result.put(TLVType.STATE, 2);
    result.put(TLVType.PUBLIC_KEY, AccessoryKeyPair.getPublicKey());
    result.put(TLVType.ENCRYPTED_DATA, cipherText);

    logger.debug("Send back pair verify message with M2 state");
    // Send the response to the iOS device
    successTLVResponse(ctx, result);
  }

  /**
   * Handle M3 state and return M4 state
   *
   * M3: iOS Device -> Accessory -- `Verify Finish Request'
   *
   * When the iOS device receives <M2>, it performs the following steps: 1. Generate the shared
   * secret, SharedSecret, from its Curve25519 secret key and the accessory's Curve25519 public key.
   * 2. Derive the symmetric session encryption key, SessionKey, in the same manner as the
   * accessory. 3. Verify the 16-byte auth tag, authTag, against the received encryptedData. If this
   * fails, the setup process will be aborted and an error will be reported to the user. 4. Decrypt
   * the sub-TLV from the received encryptedData. 5. Use the accessory's Pairing Identifier to look
   * up the accessory's long-term public key, AccessoryLTPK, in its list of paired accessories. If
   * not found, the setup process will be aborted and an error will be reported to the user. 6. Use
   * Ed25519 to verify AccessorySignature using AccessoryLTPK against AccessoryInfo. If this fails,
   * the setup process will be aborted and an error will be reported to the user. 7. Construct
   * iOSDeviceInfo by concatenating the following items in order: a. iOS Device's Curve25519 public
   * key. b. iOS Device's Pairing Identifier, iOSDevicePairingID. c. Accessory's Curve25519 public
   * key from the received <M2> TLV. 8. Use Ed25519 to generate iOSDeviceSignature by signing
   * iOSDeviceInfo with its long-term secret key, iOSDeviceLTSK. 9. Construct a sub-TLV with the
   * following items: kTLVType_Identifier <iOSDevicePairingID> kTLVType_Signature
   * <iOSDeviceSignature> 10. Encrypt the sub-TLV, encryptedData, and generate the 16-byte auth
   * tag, authTag. This uses the ChaCha20-Poly1305 AEAD algorithm with the following parameters:
   * encryptedData, authTag = ChaCha20-Poly1305(SessionKey, Nonce="PV-Msg03", AAD=<none>,
   * Msg=<Sub-TLV>) 11. Construct the request with the following TLV items: kTLVType_State <M3>
   * kTLVType_EncryptedData <encryptedData with authTag appended> 12. Send the request to the
   * accessory.
   *
   * M4: Accessory -> iOS Device -- `Verify Finish Response'
   *
   * When the accessory receives <M3>, it must perform the following steps: 1. Verify the iOS
   * device's authTag, which is appended to the encryptedData and contained within the
   * kTLVType_EncryptedData TLV item, against encryptedData. If verification fails, the accessory
   * must respond with the following TLV items: kTLVType_State <M4> kTLVType_Error
   * kTLVError_Authentication 2. Decrypt the sub-TLV in encryptedData. If decryption fails, the
   * accessory must respond with the following TLV items: kTLVType_State <M4> kTLVType_Error
   * kTLVError_Authentication 3. Use the iOS device's Pairing Identifier, iOSDevicePairingID, to
   * look up the iOS device's long-term public key, iOSDeviceLTPK, in its list of paired
   * controllers. If not found, the accessory must respond with the following TLV items:
   * kTLVType_State <M4> kTLVType_Error kTLVError_Authentication 4. Use Ed25519 to verify
   * iOSDeviceSignature using iOSDeviceLTPK against iOSDeviceInfo contained in the decrypted
   * sub-TLV. If decryption fails, the accessory must respond with the following TLV items:
   * kTLVType_State <M4> kTLVType_Error kTLVError_Authentication 5. Send the response to the iOS
   * device with the following TLV items: kTLVType_State <M4>
   *
   * When the iOS device receives <M4>, the Pair Verify process is complete. If a subsequent Pair
   * Verify request from another controller occurs in the middle of a Pair Verify transaction the
   * accessory must honor both Pair Verify requests and maintain separate secure sessions for each
   * controller. If a subsequent Pair Verify request from the same controller occurs in the middle
   * of the Pair Verify process then the accessory must immediately tear down the existing session
   * with the controller and must accept the newest request.
   *
   * @param ctx {@link ChannelHandlerContext}
   * @param tlv TLV Items
   */
  protected void handleM3State(ChannelHandlerContext ctx, Map<TLVType, ?> tlv) {
    logger.debug("Handle pair verify message with M3 state");

    // Verify the encryptedData
    if (tlv.get(TLVType.ENCRYPTED_DATA) == null
        || ((byte[]) tlv.get(TLVType.ENCRYPTED_DATA)).length <= 16) {
      logger.warn("Pair verify M3 message with invalid EncryptedData");
      errorTLVResponse(ctx, BAD_REQUEST, 4, TLVError.AUTHENTICATION);
      return;
    }

    // Load iOSDevicePK, SessionKey, AccessoryPK
    AttributeKey<byte[]> k = AttributeKey.valueOf("iOSDevicePK");
    byte[] iOSDevicePK = ctx.channel().attr(k).get();
    k = AttributeKey.valueOf("AccessoryPK");
    byte[] AccessoryPK = ctx.channel().attr(k).get();
    k = AttributeKey.valueOf("SessionKey");
    byte[] SessionKey = ctx.channel().attr(k).get();
    k = AttributeKey.valueOf("SharedSecret");
    byte[] SharedSecret = ctx.channel().attr(k).get();
    if (iOSDevicePK == null || AccessoryPK == null || SessionKey == null || SharedSecret == null) {
      logger.warn("Pair verify M3 message missing iOSDevicePK AccessoryPK SessionKey SharedSecret");
      errorTLVResponse(ctx, BAD_REQUEST, 4, TLVError.UNKNOWN);
      return;
    }

    // Decrypt the sub-TLV in encryptedData
    byte[] nonce = chaCha20Poly1305Nonce("PV-Msg03");
    byte[] cipherText = (byte[]) tlv.get(TLVType.ENCRYPTED_DATA);
    byte[] plainText;
    try {
      plainText = chaCha20Poly1305Decrypt(SessionKey, cipherText, nonce);
    } catch (GeneralSecurityException e) {
      logger.error("Error when decrypting the sub-TLV in encryptedData", e);
      errorTLVResponse(ctx, BAD_REQUEST, 4, TLVError.AUTHENTICATION);
      return;
    }

    // Decode sub-TLV
    Map<TLVType, ?> decodeSubTLV = TLVDecoder.decode(bytesToBuffer(plainText));
    String iOSDevicePairingID = (String) decodeSubTLV.get(TLVType.IDENTIFIER);
    byte[] iOSDeviceSignature = (byte[]) decodeSubTLV.get(TLVType.SIGNATURE);

    // Load iOSDeviceLTPK
    Optional<Pairing> pairing = this.hapStorage.getPairingById(iOSDevicePairingID);
    if (pairing.isEmpty()) {
      logger.warn("Pairing information not exist");
      errorTLVResponse(ctx, BAD_REQUEST, 4, TLVError.AUTHENTICATION);
      return;
    }
    byte[] iOSDeviceLTPK = pairing.get().getPublicKey();

    // Concatenate iOSDeviceInfo
    byte[] iOSDeviceInfo = ArrayUtils.addAll(ArrayUtils.addAll(iOSDevicePK,
        iOSDevicePairingID.getBytes(StandardCharsets.UTF_8)), AccessoryPK);

    // Verify the signature
    try {
      if (!ed25519Verify(iOSDeviceLTPK, iOSDeviceInfo, iOSDeviceSignature)) {
        logger.warn("Fail to verify the signature");
        errorTLVResponse(ctx, BAD_REQUEST, 4, TLVError.AUTHENTICATION);
        return;
      }
    } catch (GeneralSecurityException e) {
      logger.error("Error when verifying the signature", e);
      errorTLVResponse(ctx, BAD_REQUEST, 4, TLVError.AUTHENTICATION);
      return;
    }

    // Save iOSDevicePairingID, AccessoryToControllerKey, ControllerToAccessoryKey
    ctx.channel().attr(AttributeKey.valueOf("iOSDevicePairingID")).set(iOSDevicePairingID);
    byte[] AccessoryToControllerKey = hkdf(SharedSecret,
        "Control-Salt", "Control-Read-Encryption-Key", 32);
    byte[] ControllerToAccessoryKey = hkdf(SharedSecret,
        "Control-Salt", "Control-Write-Encryption-Key", 32);
    ctx.channel().attr(AttributeKey.valueOf("AccessoryToControllerKey"))
        .set(AccessoryToControllerKey);
    ctx.channel().attr(AttributeKey.valueOf("ControllerToAccessoryKey"))
        .set(ControllerToAccessoryKey);

    // Save session
    this.registry.saveSession(iOSDevicePairingID, ctx);

    // Construct the response
    LinkedHashMap<TLVType, Object> result = new LinkedHashMap<>(1);
    result.put(TLVType.STATE, 4);

    logger.debug("Send back pair verify message with M4 state");
    // Send the response to the iOS device
    successTLVResponse(ctx, result);

    // Finished pair stage and http frame codec
    ctx.pipeline().addAfter("log", "frame", new FrameCodec());
  }
}
