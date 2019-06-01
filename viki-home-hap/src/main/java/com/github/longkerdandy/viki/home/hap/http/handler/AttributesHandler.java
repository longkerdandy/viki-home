package com.github.longkerdandy.viki.home.hap.http.handler;

import static com.github.longkerdandy.viki.home.hap.util.Handlers.MIME_JSON;
import static com.github.longkerdandy.viki.home.hap.util.Handlers.errorJSONResponse;
import static com.github.longkerdandy.viki.home.hap.util.Handlers.extractLocalURL;
import static com.github.longkerdandy.viki.home.hap.util.Handlers.successJSONResponse;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpMethod.PUT;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.MULTI_STATUS;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.longkerdandy.viki.home.hap.http.request.CharacteristicsWriteRequest;
import com.github.longkerdandy.viki.home.hap.http.response.AccessoriesResponse;
import com.github.longkerdandy.viki.home.hap.http.response.CharacteristicReadResponseTarget;
import com.github.longkerdandy.viki.home.hap.http.response.CharacteristicWriteResponseTarget;
import com.github.longkerdandy.viki.home.hap.http.response.CharacteristicsReadResponse;
import com.github.longkerdandy.viki.home.hap.http.response.CharacteristicsWriteResponse;
import com.github.longkerdandy.viki.home.hap.http.response.Status;
import com.github.longkerdandy.viki.home.hap.model.Accessory;
import com.github.longkerdandy.viki.home.hap.model.Characteristic;
import com.github.longkerdandy.viki.home.hap.storage.HAPStorage;
import com.github.longkerdandy.viki.home.util.Jacksons;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.AttributeKey;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles Accessory Attributes
 */
public class AttributesHandler {

  private static final Logger logger = LoggerFactory.getLogger(AttributesHandler.class);

  // Regexp for read characteristics id parameter
  private static final Pattern ID_PATTERN = Pattern.compile("^(\\d+.\\d+)(,\\d+.\\d+)*$");

  // HomeKit Accessory Protocol related storage
  private final HAPStorage hapStorage;

  /**
   * Constructor
   *
   * @param hapStorage {@link HAPStorage}
   */
  public AttributesHandler(HAPStorage hapStorage) {
    this.hapStorage = hapStorage;
  }

  /**
   * Lookup Value by Name in List of {@link NameValuePair}
   *
   * @param pairs List of {@link NameValuePair}
   * @param name Name wanted
   * @return Value if present
   */
  protected static Optional<String> lookupNameValuePairs(List<NameValuePair> pairs, String name) {
    return pairs.stream()
        .filter(pair -> pair.getName().equals(name))
        .map(NameValuePair::getValue)
        .findFirst();
  }

  /**
   * The method is called once we have received the complete {@link FullHttpRequest}
   *
   * The accessory attribute database is a list of HAP Accessory objects, Service objects, and
   * Characteristic objects serialized into JSON.
   *
   * @param ctx {@link ChannelHandlerContext}
   * @param request {@link FullHttpRequest}
   */
  public void handle(ChannelHandlerContext ctx, FullHttpRequest request)
      throws JsonProcessingException {
    logger.debug("Received attribute message from {}", ctx.channel().remoteAddress());

    // Check MIME type
    // The MIME type for requests and responses is application/hap+json.
    ByteBuf content = request.content();
    String contentType = request.headers().get(CONTENT_TYPE);
    if (content.readableBytes() > 0 &&
        (contentType == null || !contentType.equalsIgnoreCase(MIME_JSON))) {
      logger.warn("Pairing message with invalid Content-Type {}", contentType);
      errorJSONResponse(ctx, BAD_REQUEST, Status.INSUFFICIENT_PRIVILEGES);
      return;
    }

    // HTTP Method
    HttpMethod httpMethod = request.method();
    // Extract requested resource URL path
    String localURL = extractLocalURL(request);

    if (localURL.startsWith("/accessories") && httpMethod == GET) {
      handleReadAccessories(ctx);
    } else if (localURL.startsWith("/characteristics") && httpMethod == GET) {
      handleReadCharacteristics(ctx, request);
    } else if (localURL.startsWith("/characteristics") && httpMethod == PUT) {
      handleWriteCharacteristics(ctx, content);
    } else if (localURL.startsWith("/identify") && httpMethod == POST) {
      handleIdentify(ctx);
    } else {
      logger.warn("Attribute message with unknown HTTP Method {} and URL {}", httpMethod, localURL);
      errorJSONResponse(ctx, NOT_FOUND, Status.INSUFFICIENT_PRIVILEGES);
    }
  }

  /**
   * A controller obtains the attribute database by sending the accessory an HTTP GET request to
   * /accessories: GET /accessories HTTP/1.1 Host: lights.local:12345 If the controller is paired
   * with the accessory then the accessory will return the attribute database in the message body of
   * the HTTP response. Controllers should cache the attribute database and monitor the current
   * configuration number (c#) for changes, which indicates that the controller should get the
   * attribute database and update its cache. Unpaired controllers must not cache the attribute
   * database. If an updated attribute database shows that it is missing some services and
   * characteristics, then these service and characteristics will be deleted from the home.
   *
   * @param ctx {@link ChannelHandlerContext}
   */
  protected void handleReadAccessories(ChannelHandlerContext ctx) throws JsonProcessingException {
    logger.debug("Handle read accessories message");

    // Load iOSDevicePairingID
    AttributeKey<String> k = AttributeKey.valueOf("iOSDevicePairingID");
    String iOSDevicePairingID = ctx.channel().attr(k).get();

    // Check verify status
    if (iOSDevicePairingID == null) {
      logger.error("Missing iOSDevicePairingID from pair verify stage");
      errorJSONResponse(ctx, BAD_REQUEST, Status.INSUFFICIENT_AUTHORIZATION);
      return;
    }

    // Get all the Accessories
    List<Accessory> accessories = this.hapStorage.getAccessories();

//    // Remove Characteristic without PAIRED_READ Permission
//    for (Accessory accessory : accessories) {
//      for (Service service : accessory.getServices()) {
//        service.getCharacteristics().removeIf(c -> !c.getPermissions().contains(PAIRED_READ));
//      }
//    }

    logger.debug("Send back read accessories message response");
    // Send the response to the iOS device
    AccessoriesResponse response = new AccessoriesResponse(accessories);
    successJSONResponse(ctx, OK, response);
  }

  /**
   * The accessory must support reading values from one or more characteristics via a single HTTP
   * request. To read the value of a characteristic, the controller sends an HTTP GET request to
   * /characteristics with a query string that conforms to Section 3.4 of RFC 3986.
   *
   * @param ctx {@link ChannelHandlerContext}
   * @param request {@link FullHttpRequest}
   */
  protected void handleReadCharacteristics(ChannelHandlerContext ctx, FullHttpRequest request)
      throws JsonProcessingException {
    logger.debug("Handle read characteristics message");

    // Load iOSDevicePairingID
    AttributeKey<String> k = AttributeKey.valueOf("iOSDevicePairingID");
    String iOSDevicePairingID = ctx.channel().attr(k).get();

    // Check verify status
    if (iOSDevicePairingID == null) {
      logger.error("Missing iOSDevicePairingID from pair verify stage");
      errorJSONResponse(ctx, BAD_REQUEST, Status.INSUFFICIENT_AUTHORIZATION);
      return;
    }

    // Parse URL parameters
    List<CharacteristicReadResponseTarget> characteristics = new ArrayList<>();
    boolean fail = false;
    Optional<String> id;
    Optional<String> meta;
    Optional<String> perms;
    Optional<String> type;
    Optional<String> ev;

    try {
      String uri = request.uri();
      List<NameValuePair> params = URLEncodedUtils.parse(new URI(uri), StandardCharsets.UTF_8);
      id = lookupNameValuePairs(params, "id");
      meta = lookupNameValuePairs(params, "meta");
      perms = lookupNameValuePairs(params, "perms");
      type = lookupNameValuePairs(params, "type");
      ev = lookupNameValuePairs(params, "ev");

      // The identifiers for the characteristics to be read must be formatted as <Accessory
      // Instance ID>.<Characteristic Instance ID>, as a comma-separated list. For
      // example, to read the values of characteristics with instance ID "4" and "8" on an accessory
      // with an instanceID of "1" the URL parameter would be id=1.4,1.8. id is required for
      // all GET requests.
      if (id.isEmpty()) {
        logger.warn("Missing id from URL parameters");
        errorJSONResponse(ctx, BAD_REQUEST, Status.INVALID_VALUE);
        return;
      }
      Matcher matcher = ID_PATTERN.matcher(id.get());
      if (!matcher.find()) {
        logger.warn("Invalid id URL parameter {}", id.get());
        errorJSONResponse(ctx, BAD_REQUEST, Status.INVALID_VALUE);
        return;
      }
    } catch (URISyntaxException e) {
      logger.error("Error when parsing request url", e);
      errorJSONResponse(ctx, BAD_REQUEST, Status.INVALID_VALUE);
      return;
    }

    // Read Characteristics from storage
    for (String pair : id.get().split(",")) {
      long accessoryId = Long.parseLong(pair.split(".")[0]);
      long instanceId = Long.parseLong(pair.split(".")[1]);
      Optional<Characteristic> characteristic = this.hapStorage
          .getCharacteristicById(accessoryId, instanceId);

      if (characteristic.isPresent()) {
        CharacteristicReadResponseTarget target = new CharacteristicReadResponseTarget(
            accessoryId, instanceId, characteristic.get().getValue());

        // Boolean value that determines whether or not the response should include metadata. If
        // meta is not present it must be assumed to be "0". If meta is "1", then the response must
        // include the following properties if they exist for the characteristic: "format", "unit",
        // "minValue", "maxValue", "minStep", and "maxLen"
        if (meta.isPresent() && meta.get().equals("1")) {
          target.setFormat(characteristic.get().getFormat());
          target.setUnit(characteristic.get().getUnit());
          target.setMinValue(characteristic.get().getMinValue());
          target.setMaxValue(characteristic.get().getMaxValue());
          target.setMinStep(characteristic.get().getMinStep());
          target.setMaxLength(characteristic.get().getMaxLength());
        }

        // Boolean value that determines whether or not the response should include the
        // permissions of the characteristic. If perms is not present it must be assumed to be "0"
        if (perms.isPresent() && perms.get().equals("1")) {
          //noinspection unchecked
          target.setPermissions(characteristic.get().getPermissions());
        }

        // Boolean value that determines whether or not the response should include the type of
        // characteristic. If type is not present it must be assumed to be "0".
        if (type.isPresent() && type.get().equals("1")) {
          target.setType(characteristic.get().getType());
        }

        // Boolean value that determines whether or not the "ev" property of the characteristic
        // should be included in the response. If ev is not present it must be assumed to be "0".
        if (ev.isPresent() && ev.get().equals("1")) {
          target.setEnableEvent(characteristic.get().getEnableEvent());
        }

        // Add to results
        characteristics.add(target);

      } else {
        // Mark as failed
        fail = true;

        CharacteristicReadResponseTarget target = new CharacteristicReadResponseTarget(
            accessoryId, instanceId, Status.RESOURCE_NOT_EXIST);

        // Add to results
        characteristics.add(target);
      }
    }

    // Clear Status field if no failure
    if (!fail) {
      for (CharacteristicReadResponseTarget target : characteristics) {
        target.setStatus(null);
      }
    }

    logger.debug("Send back read characteristics message response");
    // Send the response to the iOS device
    CharacteristicsReadResponse response = new CharacteristicsReadResponse(characteristics);
    if (fail) {
      successJSONResponse(ctx, MULTI_STATUS, response);
    } else {
      successJSONResponse(ctx, OK, response);
    }
  }

  /**
   * The accessory must support writing values to one or more characteristics via a single HTTP
   * request. To write one or more characteristic values, the controller sends an HTTP PUT request
   * to /characteristics. The HTTP body is a JSON object that contains an array of Characteristic
   * Write Request objects.
   *
   * @param ctx {@link ChannelHandlerContext}
   * @param body HAP JSON HTTP Body as {@link ByteBuf}
   */
  protected void handleWriteCharacteristics(ChannelHandlerContext ctx, ByteBuf body)
      throws JsonProcessingException {
    logger.debug("Handle write characteristics message");

    // Load iOSDevicePairingID
    AttributeKey<String> k = AttributeKey.valueOf("iOSDevicePairingID");
    String iOSDevicePairingID = ctx.channel().attr(k).get();

    // Check verify status
    if (iOSDevicePairingID == null) {
      logger.error("Missing iOSDevicePairingID from pair verify stage");
      errorJSONResponse(ctx, BAD_REQUEST, Status.INSUFFICIENT_AUTHORIZATION);
      return;
    }

    try {
      // Parse HAP JSON HTTP body
      byte[] json = new byte[body.readableBytes()];
      body.readBytes(json);
      CharacteristicsWriteRequest request = Jacksons.getReader(CharacteristicsWriteRequest.class)
          .readValue(json);

      // Empty targets
      if (request.getCharacteristics() == null || request.getCharacteristics().isEmpty()) {
        logger.warn("Missing write characteristics targets");
        errorJSONResponse(ctx, BAD_REQUEST, Status.INVALID_VALUE);
        return;
      }

      // Identify routine
      if (request.getCharacteristics().size() == 1
          && request.getCharacteristics().get(0).getAccessoryId() == 1L
          && request.getCharacteristics().get(0).getInstanceId() == 2L) {
        logger.debug("Redirect to handle identify routine");
        handleIdentify(ctx);
        return;
      }

      // Save characteristics to storage
      List<CharacteristicWriteResponseTarget> results = this.hapStorage
          .saveCharacteristics(request.getCharacteristics());

      logger.debug("Send back write characteristics message response");
      // Send the response to the iOS device
      if (results != null) {
        CharacteristicsWriteResponse response = new CharacteristicsWriteResponse(results);
        successJSONResponse(ctx, MULTI_STATUS, response);
      } else {
        successJSONResponse(ctx, NO_CONTENT, null);
      }
    } catch (IOException e) {
      logger.error("Error when decoding HAP JSON");
      errorJSONResponse(ctx, BAD_REQUEST, Status.INVALID_VALUE);
    }
  }

  /**
   * An identify routine must exist for all accessories. There are two ways that the identify
   * routine may be invoked. Both of these methods must be supported: Via an Identify URL if the
   * accessory is unpaired, as described in Identify HTTP URL. Via a characteristic write to the
   * identify characteristic, public.hap.characteristic.identify. The identify routine is used to
   * locate the accessory. The routine should run no longer than five seconds. For example, a
   * lightbulb accessory may implement an identify routine as follows: Blink the lightbulb (turn on
   * to full brightness, then off) three times for 500 ms each time
   *
   * @param ctx {@link ChannelHandlerContext}
   */
  protected void handleIdentify(ChannelHandlerContext ctx) throws JsonProcessingException {
    logger.debug("Handle identify message");

    // Fact: identify is called during add accessory process

    // Load iOSDevicePairingID
    //    AttributeKey<String> k = AttributeKey.valueOf("iOSDevicePairingID");
    //    String iOSDevicePairingID = ctx.channel().attr(k).get();

    // The URL is only valid if the accessory is unpaired, i.e. it has no paired controllers. If the accessory has paired
    // controllers then the accessory must return 400 Bad Request for any HTTP requests to the /identify URL
    //    if (iOSDevicePairingID != null) {
    //      logger.error("The bridge has already been paired");
    //      errorJSONResponse(ctx, BAD_REQUEST, Status.INSUFFICIENT_PRIVILEGES);
    //      return;
    //    }

    logger.debug("Send back identify message response");
    // Otherwise, for an unpaired accessory, it will fulfill the identify request and return a 204 No Content HTTP
    // status code
    successJSONResponse(ctx, NO_CONTENT, null);
  }
}
