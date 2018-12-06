package com.github.longkerdandy.viki.home.hap.http.handler;

import static com.github.longkerdandy.viki.home.hap.util.Ciphers.G_3072;
import static com.github.longkerdandy.viki.home.hap.util.Ciphers.H;
import static com.github.longkerdandy.viki.home.hap.util.Ciphers.N_3072;
import static com.github.longkerdandy.viki.home.hap.util.Ciphers.chaCha20Poly1305Decrypt;
import static com.github.longkerdandy.viki.home.hap.util.Ciphers.chaCha20Poly1305Encrypt;
import static com.github.longkerdandy.viki.home.hap.util.Ciphers.chaCha20Poly1305Nonce;
import static com.github.longkerdandy.viki.home.hap.util.Ciphers.ed25519Sign;
import static com.github.longkerdandy.viki.home.hap.util.Ciphers.ed25519Verify;
import static com.github.longkerdandy.viki.home.hap.util.Ciphers.hkdf;
import static com.github.longkerdandy.viki.home.hap.util.Handlers.MIME_TLV8;
import static com.github.longkerdandy.viki.home.hap.util.Handlers.bufferToBytes;
import static com.github.longkerdandy.viki.home.hap.util.Handlers.bytesToBuffer;
import static com.github.longkerdandy.viki.home.hap.util.Handlers.errorTLVResponse;
import static com.github.longkerdandy.viki.home.hap.util.Handlers.successTLVResponse;
import static com.nimbusds.srp6.BigIntegerUtils.bigIntegerFromBytes;
import static com.nimbusds.srp6.BigIntegerUtils.bigIntegerToBytes;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.longkerdandy.viki.home.hap.crypto.HAPClientEvidenceRoutine;
import com.github.longkerdandy.viki.home.hap.crypto.HAPSRP6Routines;
import com.github.longkerdandy.viki.home.hap.crypto.HAPSRP6ServerSession;
import com.github.longkerdandy.viki.home.hap.crypto.HAPServerEvidenceRoutine;
import com.github.longkerdandy.viki.home.hap.http.tlv.TLVDecoder;
import com.github.longkerdandy.viki.home.hap.http.tlv.TLVEncoder;
import com.github.longkerdandy.viki.home.hap.http.tlv.TLVError;
import com.github.longkerdandy.viki.home.hap.http.tlv.TLVType;
import com.github.longkerdandy.viki.home.hap.mdns.HAPmDNSAdvertiser;
import com.github.longkerdandy.viki.home.hap.model.Pairing;
import com.github.longkerdandy.viki.home.hap.storage.HAPStorage;
import com.nimbusds.srp6.SRP6CryptoParams;
import com.nimbusds.srp6.SRP6Exception;
import com.nimbusds.srp6.SRP6VerifierGenerator;
import com.nimbusds.srp6.XRoutineWithUserIdentity;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.AttributeKey;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles Pair Setup
 *
 * SRP coding convention: N, g: group parameters (prime and generator) H: hash function s: salt B,
 * b: server's public and private values A, a: client's public and private values I: user name (aka
 * "identity") P: password v: verifier k: SRP-6 multiplier u: random scrambling parameter S:
 * premaster secret K: session key M1, M2: client and server evidence messages
 */
public class PairSetupHandler {

  private static final Logger logger = LoggerFactory.getLogger(PairSetupHandler.class);

  // HomeKit Accessory Protocol related storage
  private final HAPStorage hapStorage;

  // mac address
  private final String macAddress;

  // password (aka Setup Code, PIN Code)
  // The Setup Code must conform to the format XXX-XX-XXX where each X is a 0-9 digit and dashes are required.
  private final String P;

  // mDNS
  private final HAPmDNSAdvertiser advertiser;

  /**
   * Constructor
   *
   * @param hapStorage {@link HAPStorage}
   * @param macAddress MAC Address
   * @param P Password (aka Setup Code, PIN Code)
   * @param advertiser {@link HAPmDNSAdvertiser}
   */
  public PairSetupHandler(HAPStorage hapStorage, String macAddress, String P,
      HAPmDNSAdvertiser advertiser) {
    this.hapStorage = hapStorage;
    this.macAddress = macAddress;
    this.P = P;
    this.advertiser = advertiser;
  }

  /**
   * The method is called once we have received the complete {@link FullHttpRequest}, and the local
   * url is <code>"/pair-setup"</code>.
   *
   * Used for Pair Setup.
   *
   * @param ctx {@link ChannelHandlerContext}
   * @param request {@link FullHttpRequest}
   */
  public void handle(ChannelHandlerContext ctx, FullHttpRequest request) {
    logger.debug("Received pair setup message from {}", ctx.channel().remoteAddress());

    // Check HTTP method and MIME type
    // Pairing requests are performed by sending a POST request to the accessory's HTTP server.
    // The MIME type for requests and responses is application/pairing+tlv8.
    HttpMethod httpMethod = request.method();
    String contentType = request.headers().get(CONTENT_TYPE);
    if (httpMethod != POST || contentType == null || !contentType.equalsIgnoreCase(MIME_TLV8)) {
      logger.warn("Pair setup message with invalid HTTP Method {} and Content-Type {}",
          httpMethod, contentType);
      errorTLVResponse(ctx, METHOD_NOT_ALLOWED, TLVError.UNKNOWN);
      return;
    }

    // Parse TLV8 HTTP body
    ByteBuf buffer = request.content();
    Map<TLVType, ?> tlv = TLVDecoder.decode(buffer);

    if (!tlv.containsKey(TLVType.STATE)) {
      logger.warn("Pair setup message does not have TLV State");
      errorTLVResponse(ctx, BAD_REQUEST, TLVError.UNKNOWN);
      return;
    }
    int state = (Integer) tlv.get(TLVType.STATE);
    switch (state) {
      case 1:
        handleM1State(ctx);
        break;
      case 3:
        handleM3State(ctx, tlv);
        break;
      case 5:
        handleM5State(ctx, tlv);
        break;
      default:
        logger.warn("Pair setup message with unknown TLV State {}", state);
        errorTLVResponse(ctx, BAD_REQUEST, TLVError.UNKNOWN);
    }
  }

  /**
   * Handle M1 state and return M2 state
   *
   * M1: iOS Device -> Accessory -- `SRP Start Request'
   *
   * The iOS device sends a request to the accessory with the following TLV items: kTLVType_State
   * <M1> kTLVType_Method <Pair Setup>
   *
   * M2: Accessory -> iOS Device -- `SRP Start Response'
   *
   * When the accessory receives <M1>, it must perform the following steps: 1. If the accessory is
   * already paired it must respond with the following TLV items: kTLVType_State <M2>
   * kTLVType_Error
   * <kTLVError_Unavailable> 2. If the accessory has received more than 100 unsuccessful
   * authentication attempts it must respond with the following TLV items: kTLVType_State <M2>
   * kTLVType_Error <kTLVError_MaxTries> 3. If the accessory is currently performing a Pair Setup
   * operation with a different controller it must respond with the following TLV items:
   * kTLVType_State <M2> kTLVType_Error <kTLVError_Busy> 4. Create new SRP session. 5. Set SRP
   * username to Pair-Setup. 6. Generate 16 bytes of random salt and set it. 7. If the accessory can
   * display a random setup code then it must generate a random setup code and set it. If the
   * accessory cannot display a random setup code then it must retrieve the SRP verifier for the
   * Setup Code, e.g. from an EEPROM, and set the verifier. The Setup Code must conform to the
   * format XXX-XX-XXX where each X is a 0-9 digit and dashes are required. 8. Present the Setup
   * Code to the user, e.g. display it on the accessory's screen. If the accessory doesn't have a
   * screen then the Setup Code may be on a printed label. 9. Generate an SRP public key.
   *
   * @param ctx {@link ChannelHandlerContext}
   */
  protected void handleM1State(ChannelHandlerContext ctx) {
    logger.debug("Handle pair setup message with M1 state");

    // Fact: iOS device will send TLVMethod.RESERVED
    //    // Check kTLVType_Method
    //    if (tlv.get(TLVType.METHOD) != TLVMethod.PAIR_SETUP) {
    //      logger.warn("Pair setup M1 message with wrong TLV Method {}", tlv.get(TLVType.METHOD));
    //      return errorTLVResponse(ctx, request, 400, 2, TLVError.UNKNOWN);
    //    }

    // If the accessory is already paired?
    Map<String, ?> bridgeInfo = hapStorage.getBridgeInformation();
    int statusFlag = (Integer) bridgeInfo.get("status_flag");
    if (statusFlag != 1) {
      logger.warn("The accessory is already paired or wrong status: {}", statusFlag);
      errorTLVResponse(ctx, BAD_REQUEST, 2, TLVError.UNAVAILABLE);
      return;
    }

    // Create new SRP session
    SRP6CryptoParams config = new SRP6CryptoParams(N_3072, G_3072, H);
    HAPSRP6ServerSession session = new HAPSRP6ServerSession(config);
    session.setClientEvidenceRoutine(new HAPClientEvidenceRoutine());
    session.setServerEvidenceRoutine(new HAPServerEvidenceRoutine());

    // Save SRP session
    ctx.channel().attr(AttributeKey.valueOf("SRPSession")).set(session);

    // Set SRP username to Pair-Setup
    String I = "Pair-Setup";

    // Generate 16 bytes of random salt
    BigInteger s = bigIntegerFromBytes(new HAPSRP6Routines().generateRandomSalt(16));

    // If the accessory cannot display a random setup code then it must retrieve the SRP verifier for the Setup Code.
    SRP6VerifierGenerator verifierGenerator = new SRP6VerifierGenerator(config);
    verifierGenerator.setXRoutine(new XRoutineWithUserIdentity());
    BigInteger v = verifierGenerator.generateVerifier(s, I, P);

    // Generate an SRP public key
    BigInteger B = session.step1(I, s, v);

    // Respond to the iOS device's request
    LinkedHashMap<TLVType, Object> result = new LinkedHashMap<>(3);
    result.put(TLVType.STATE, 2);
    result.put(TLVType.PUBLIC_KEY, bigIntegerToBytes(B));
    result.put(TLVType.SALT, bigIntegerToBytes(s));

    logger.debug("Send back pair setup message with M2 state");
    // Send the response to the iOS device
    successTLVResponse(ctx, result);
  }

  /**
   * Handle M3 state and return M4 state
   *
   * M3: iOS Device -> Accessory -- `SRP Verify Request'
   *
   * When the iOS device receives <M2>, it will check for kTLVType_Error. If present, the iOS device
   * will abort the setup process and report the error to the user. If kTLVType_Error is not
   * present, the user is prompted to enter the Setup Code provided by the accessory. Once the user
   * has entered the Setup Code, the iOS device performs the following steps:
   *
   * 1. Create a new SRP session. 2. Set the SRP username to Pair-Setup. 3. Set salt provided by the
   * accessory in the <M2>. 4. Generate its SRP public key. 5. Set the Setup Code as entered by the
   * user. 6. Compute the SRP shared secret key. 7. Generate iOS device-side SRP proof. 8. Send a
   * request to the accessory with the following TLV items: kTLVType_State     <M3>
   * kTLVType_PublicKey <iOS device's SRP public key> kTLVType_Proof     <iOS device's SRP proof>
   *
   * M4: Accessory -> iOS Device -- `SRP Verify Response'
   *
   * When the accessory receives <M3>, it must perform the following steps: 1. Use the iOS device's
   * SRP public key to compute the SRP shared secret key. 2. Verify the iOS device's SRP proof. If
   * verification fails, the accessory must respond with the following TLV items: kTLVType_State
   * <M4> kTLVType_Error kTLVError_Authentication 3. Generate the accessory-side SRP proof. 4.
   * Construct the response with the following TLV items: kTLVType_State <M4> kTLVType_Proof
   * <Accessory's SRP proof> 5. Send the response to the iOS device.
   *
   * @param ctx {@link ChannelHandlerContext}
   * @param tlv TLV Items
   */
  protected void handleM3State(ChannelHandlerContext ctx, Map<TLVType, ?> tlv) {
    logger.debug("Handle pair setup message with M3 state");

    // Check kTLVType_PublicKey and kTLVType_Proof
    if (tlv.get(TLVType.PUBLIC_KEY) == null || tlv.get(TLVType.PROOF) == null) {
      logger.warn("Pair setup M3 message without Public Key or Proof");
      errorTLVResponse(ctx, BAD_REQUEST, 4, TLVError.UNKNOWN);
      return;
    }

    // Read PublicKey and Proof
    BigInteger A = bigIntegerFromBytes((byte[]) tlv.get(TLVType.PUBLIC_KEY));
    BigInteger M1 = bigIntegerFromBytes((byte[]) tlv.get(TLVType.PROOF));

    // Load SRP session
    AttributeKey<HAPSRP6ServerSession> k = AttributeKey.valueOf("SRPSession");
    HAPSRP6ServerSession session = ctx.channel().attr(k).get();
    if (session == null) {
      logger.error("Missing SRP Session from pair setup state M1");
      errorTLVResponse(ctx, BAD_REQUEST, 4, TLVError.UNKNOWN);
      return;
    }

    try {
      // Generate the accessory-side SRP proof
      BigInteger M2 = session.step2(A, M1);

      // Construct the response
      LinkedHashMap<TLVType, Object> result = new LinkedHashMap<>(2);
      result.put(TLVType.STATE, 4);
      result.put(TLVType.PROOF, bigIntegerToBytes(M2));

      logger.debug("Send back pair setup message with M4 state");
      // Send the response to the iOS device
      successTLVResponse(ctx, result);
    } catch (SRP6Exception e) {
      logger.warn("Fail to verify SRP proof: {}", ExceptionUtils.getMessage(e));
      // Verify the iOS device's SRP proof, If verification fails
      errorTLVResponse(ctx, BAD_REQUEST, 4, TLVError.AUTHENTICATION);
    }
  }

  /**
   * Handle M5 state and return M6 state
   *
   * M5: iOS Device -> Accessory -- `Exchange Request
   *
   * <M4> Verification
   *
   * When the iOS device receives <M4>, it performs the following steps: 1. Check for
   * kTLVType_Error. If present and it's set to kTLVError_Authentication, the user will be prompted
   * that the Setup Code was incorrect and allowed to try again. If kTLVType_Error is set to any
   * other error code, then the setup process will be aborted and an error will be reported to the
   * user. The accessory resets to <M1> for Pair Setup. 2. Verify accessory's SRP proof. If this
   * fails, the setup process will be aborted and an error will be reported to the user.
   *
   * <M5> Request Generation
   *
   * Once <M4> Verification is complete, the iOS device performs the following steps to generate
   * the
   * <M5> request: 1. Generate its Ed25519 long-term public key, iOSDeviceLTPK, and long-term
   * secret key, iOSDeviceLTSK, if they don't exist. 2. Derive iOSDeviceX from the SRP shared secret
   * by using HKDF-SHA-512 with the following parameters: InputKey = <SRP shared secret> Salt =
   * "Pair-Setup-Controller-Sign-Salt" Info = "Pair-Setup-Controller-Sign-Info" OutputSize = 32
   * bytes 3. Concatenate iOSDeviceX with the iOS device's Pairing Identifier, iOSDevicePairingID,
   * and its long-term public key, iOSDeviceLTPK. The data must be concatenated in order such that
   * the final data is iOSDeviceX, iOSDevicePairingID, iOSDeviceLTPK. The concatenated value will be
   * referred to as iOSDeviceInfo. 4. Generate iOSDeviceSignature by signing iOSDeviceInfo with its
   * long-term secret key, iOSDeviceLTSK, using Ed25519. 5. Construct a sub-TLV with the following
   * TLV items: kTLVType_Identifier  <iOSDevicePairingID> kTLVType_PublicKey   <iOSDeviceLTPK>
   * kTLVType_Signature   <iOSDeviceSignature> 6. Encrypt the sub-TLV, encryptedData, and generate
   * the 16 byte auth tag, authTag. This uses the ChaCha20-Poly1305 AEAD algorithm with the
   * following parameters: encryptedData, authTag = ChaCha20-Poly1305(SessionKey, Nonce="PS-Msg05",
   * AAD=<none>, Msg=<Sub-TLV>) 7. Send the request to the accessory with the following TLV items:
   * kTLVType_State         <M5> kTLVType_EncryptedData <encryptedData with authTag appended>
   *
   * M6: Accessory -> iOS Device -- `Exchange Response'
   *
   * <M5> Verification
   *
   * When the accessory receives <M5>, it must perform the following steps: 1. Verify the iOS
   * device's authTag, which is appended to the encryptedData and contained within the
   * kTLVType_EncryptedData TLV item, from encryptedData. If verification fails, the accessory must
   * respond with the following TLV items: kTLVType_State <M6> kTLVType_Error
   * kTLVError_Authentication 2. Decrypt the sub-TLV in encryptedData. If decryption fails, the
   * accessory must respond with the following TLV items: kTLVType_State <M6> kTLVType_Error
   * kTLVError_Authentication 3. Derive iOSDeviceX from the SRP shared secret by using HKDF-SHA-512
   * with the following parameters: InputKey = <SRP shared secret> Salt =
   * "Pair-Setup-Controller-Sign-Salt" Info = "Pair-Setup-Controller-Sign-Info" OutputSize = 32
   * bytes 4. Construct iOSDeviceInfo by concatenating iOSDeviceX with the iOS device's Pairing
   * Identifier, iOSDevicePairingID, from the decrypted sub-TLV and the iOS device's long-term
   * public key, iOSDeviceLTPK from the decrypted sub-TLV. The data must be concatenated in order
   * such that the final data is iOSDeviceX, iOSDevicePairingID, iOSDeviceLTPK. 5. Use Ed25519 to
   * verify the signature of the constructed iOSDeviceInfo with the iOSDeviceLTPK from the decrypted
   * sub-TLV. If signature verification fails, the accessory must respond with the following TLV
   * items: kTLVType_State <M6> kTLVType_Error kTLVError_Authentication 6. Persistently save the
   * iOSDevicePairingID and iOSDeviceLTPK as a pairing. If the accessory cannot accept any
   * additional pairings, it must respond with the following TLV items: kTLVType_State <M6>
   * kTLVType_Error kTLVError_MaxPeers
   *
   * <M6> Response Generation
   *
   * Once <M5> Verification is complete, the accessory must perform the following steps to generate
   * the <M6> response: 1. Generate its Ed25519 long-term public key, AccessoryLTPK, and long-term
   * secret key, AccessoryLTSK, if they don't exist. 2. Derive AccessoryX from the SRP shared secret
   * by using HKDF-SHA-512 with the following parameters: InputKey = <SRP shared secret> Salt =
   * "Pair-Setup-Accessory-Sign-Salt" Info = "Pair-Setup-Accessory-Sign-Info" OutputSize = 32 bytes
   * 3. Concatenate AccessoryX with the accessory's Pairing Identifier, AccessoryPairingID, and its
   * long-term public key, AccessoryLTPK. The data must be concatenated in order such that the final
   * data is AccessoryX, AccessoryPairingID, AccessoryLTPK. The concatenated value will be referred
   * to as AccessoryInfo. 4. Use Ed25519 to generate AccessorySignature by signing AccessoryInfo
   * with its long-term secret key, AccessoryLTSK. 5. Construct the sub-TLV with the following TLV
   * items: kTLVType_Identifier <AccessoryPairingID> kTLVType_PublicKey <AccessoryLTPK>
   * kTLVType_Signature <AccessorySignature> 6. Encrypt the sub-TLV, encryptedData, and generate the
   * 16 byte auth tag, authTag. This uses the ChaCha20-Poly1305 AEAD algorithm with the following
   * parameters: encryptedData, authTag = ChaCha20-Poly1305(SessionKey, Nonce="PS-Msg06",
   * AAD=<none>, Msg=<Sub-TLV>) 7. Send the response to the iOS device with the following TLV items:
   * kTLVType_State <M6> kTLVType_EncryptedData <encryptedData with authTag appended>
   *
   * <M6> Verification by iOS Device
   *
   * When the iOS device receives <M6>, it performs the following steps: 1. Verifies authTag, which
   * is appended to the encryptedData and contained within the kTLVType_EncryptedData TLV item, from
   * encryptedData. If this fails, the setup process will be aborted and an error will be reported
   * to the user. 2. Decrypts the sub-TLV in encryptedData. If this fails, the setup process will be
   * aborted and an error will be reported to the user. 3. Uses Ed25519 to verify the signature of
   * AccessoryInfo using AccessoryLTPK. If this fails, the setup process will be aborted and an
   * error will be reported to the user. 4. Persistently saves AccessoryPairingID and AccessoryLTPK
   * as a pairing.
   *
   * Pair Setup is now complete.
   *
   * @param ctx {@link ChannelHandlerContext}
   * @param tlv TLV Items
   */
  protected void handleM5State(ChannelHandlerContext ctx, Map<TLVType, ?> tlv) {
    logger.debug("Handle pair setup message with M5 state");

    // Verify the encryptedData
    if (tlv.get(TLVType.ENCRYPTED_DATA) == null
        || ((byte[]) tlv.get(TLVType.ENCRYPTED_DATA)).length <= 16) {
      logger.warn("Pair setup M5 message with invalid Encrypted Data");
      errorTLVResponse(ctx, BAD_REQUEST, 6, TLVError.AUTHENTICATION);
      return;
    }

    // Load SRP session
    AttributeKey<HAPSRP6ServerSession> k = AttributeKey.valueOf("SRPSession");
    HAPSRP6ServerSession session = ctx.channel().attr(k).get();
    if (session == null) {
      logger.error("Missing SRP Session from pair setup state M1");
      errorTLVResponse(ctx, BAD_REQUEST, 6, TLVError.UNKNOWN);
      return;
    }

    // Session Key
    // Missing document!!!
    byte[] K = session.getSessionKeyHash();
    byte[] SessionKey = hkdf(K, "Pair-Setup-Encrypt-Salt", "Pair-Setup-Encrypt-Info", 32);

    // Decrypt the sub-TLV in encryptedData
    byte[] nonce = chaCha20Poly1305Nonce("PS-Msg05");
    byte[] cipherText = (byte[]) tlv.get(TLVType.ENCRYPTED_DATA);
    byte[] plainText;
    try {
      plainText = chaCha20Poly1305Decrypt(SessionKey, cipherText, nonce);
    } catch (GeneralSecurityException e) {
      logger.error("Error when decrypting the sub-TLV in encryptedData", e);
      errorTLVResponse(ctx, BAD_REQUEST, 6, TLVError.AUTHENTICATION);
      return;
    }

    // Derive iOSDeviceX
    byte[] iOSDeviceX = hkdf(K, "Pair-Setup-Controller-Sign-Salt",
        "Pair-Setup-Controller-Sign-Info", 32);

    // Construct iOSDeviceInfo by concatenating iOSDeviceX, iOSDevicePairingID, iOSDeviceLTPK
    Map<TLVType, ?> decodeSubTLV = TLVDecoder.decode(bytesToBuffer(plainText));
    byte[] iOSDevicePairingID = ((String) decodeSubTLV.get(TLVType.IDENTIFIER)).getBytes(UTF_8);
    byte[] iOSDeviceLTPK = (byte[]) decodeSubTLV.get(TLVType.PUBLIC_KEY);
    byte[] iOSDeviceSignature = (byte[]) decodeSubTLV.get(TLVType.SIGNATURE);
    byte[] iOSDeviceInfo = ArrayUtils.addAll(
        ArrayUtils.addAll(iOSDeviceX, iOSDevicePairingID), iOSDeviceLTPK);

    // Verify the signature
    try {
      if (!ed25519Verify(iOSDeviceLTPK, iOSDeviceInfo, iOSDeviceSignature)) {
        logger.warn("Fail to verify the signature");
        errorTLVResponse(ctx, BAD_REQUEST, 6, TLVError.AUTHENTICATION);
        return;
      }
    } catch (GeneralSecurityException e) {
      logger.error("Error when verifying the signature", e);
      errorTLVResponse(ctx, BAD_REQUEST, 6, TLVError.AUTHENTICATION);
      return;
    }

    // Save iOSDevicePairingID and iOSDeviceLTPK as admin
    Pairing pairing = new Pairing(new String(iOSDevicePairingID, StandardCharsets.UTF_8),
        iOSDeviceLTPK, 1);
    if (!this.hapStorage.savePairing(pairing)) {
      logger.warn("Fail to save iOSDevicePairingID and iOSDeviceLTPK as admin");
      errorTLVResponse(ctx, INTERNAL_SERVER_ERROR, 6, TLVError.UNKNOWN);
      return;
    }

    // Mark the bridge as paired
    // this.hapStorage.changeBridgeStatus(0);

    // Reload mDNS service
    this.advertiser.reloadBridgeServiceAsync();

    // Load AccessoryLTPK and AccessoryLTSK
    Map<String, ?> bridgeInfo = this.hapStorage.getBridgeInformation();
    byte[] AccessoryLTSK = (byte[]) bridgeInfo.get("private_key");
    byte[] AccessoryLTPK = (byte[]) bridgeInfo.get("public_key");

    // Derive AccessoryX from the SRP shared secret by using HKDF-SHA-512
    byte[] AccessoryX = hkdf(K,
        "Pair-Setup-Accessory-Sign-Salt", "Pair-Setup-Accessory-Sign-Info", 32);

    // Concatenate AccessoryX, AccessoryPairingID and AccessoryLTPK as AccessoryInfo
    String AccessoryPairingID = this.macAddress;
    byte[] AccessoryInfo = ArrayUtils.addAll(
        ArrayUtils.addAll(AccessoryX, AccessoryPairingID.getBytes(StandardCharsets.UTF_8)),
        AccessoryLTPK);

    // Use Ed25519 to generate AccessorySignature by signing AccessoryInfo with AccessoryLTSK
    byte[] AccessorySignature;
    try {
      AccessorySignature = ed25519Sign(AccessoryLTSK, AccessoryInfo);
    } catch (GeneralSecurityException e) {
      logger.error("Error when generating AccessorySignature", e);
      errorTLVResponse(ctx, BAD_REQUEST, 6, TLVError.AUTHENTICATION);
      return;
    }

    // Construct the sub-TLV
    LinkedHashMap<TLVType, Object> encodeSubTLV = new LinkedHashMap<>(3);
    encodeSubTLV.put(TLVType.IDENTIFIER, AccessoryPairingID);
    encodeSubTLV.put(TLVType.PUBLIC_KEY, AccessoryLTPK);
    encodeSubTLV.put(TLVType.SIGNATURE, AccessorySignature);

    // Encrypt the sub-TLV to encryptedData and generate the 16 byte auth tag
    nonce = chaCha20Poly1305Nonce("PS-Msg06");
    plainText = bufferToBytes(TLVEncoder.encode(encodeSubTLV));
    cipherText = chaCha20Poly1305Encrypt(SessionKey, plainText, nonce);

    // Construct the response
    LinkedHashMap<TLVType, Object> result = new LinkedHashMap<>(2);
    result.put(TLVType.STATE, 6);
    result.put(TLVType.ENCRYPTED_DATA, cipherText);

    logger.debug("Send back pair setup message with M6 state");
    // Send the response to the iOS device
    successTLVResponse(ctx, result);
  }
}
