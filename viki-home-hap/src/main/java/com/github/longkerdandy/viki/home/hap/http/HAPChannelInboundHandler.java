package com.github.longkerdandy.viki.home.hap.http;

import static com.github.longkerdandy.viki.home.hap.util.Handlers.extractLocalURL;
import static com.github.longkerdandy.viki.home.hap.util.Handlers.writeResponse;
import static io.netty.buffer.Unpooled.buffer;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.longkerdandy.viki.home.hap.http.handler.AttributesHandler;
import com.github.longkerdandy.viki.home.hap.http.handler.PairSetupHandler;
import com.github.longkerdandy.viki.home.hap.http.handler.PairVerifyHandler;
import com.github.longkerdandy.viki.home.hap.http.handler.PairingsHandler;
import com.github.longkerdandy.viki.home.hap.mdns.HAPmDNSAdvertiser;
import com.github.longkerdandy.viki.home.hap.storage.HAPStorage;
import com.github.longkerdandy.viki.home.hap.storage.Registry;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ChannelInboundHandler} implementation for HomeKit Accessory Protocol
 */
public class HAPChannelInboundHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  private static final Logger logger = LoggerFactory.getLogger(HAPChannelInboundHandler.class);

  // handlers
  private final PairSetupHandler pairSetupHandler;
  private final PairVerifyHandler pairVerifyHandler;
  private final PairingsHandler pairingsHandler;
  private final AttributesHandler attributesHandler;
  // session registry
  private final Registry registry;

  /**
   * Constructor
   *
   * @param hapStorage {@link HAPStorage}
   * @param macAddress MAC Address
   * @param pinCode Password (aka Setup Code, PIN Code)
   * @param registry {@link Registry}
   * @param advertiser {@link HAPmDNSAdvertiser}
   */
  public HAPChannelInboundHandler(HAPStorage hapStorage, String macAddress, String pinCode,
      Registry registry, HAPmDNSAdvertiser advertiser) {
    this.pairSetupHandler = new PairSetupHandler(hapStorage, macAddress, pinCode, advertiser);
    this.pairVerifyHandler = new PairVerifyHandler(hapStorage, macAddress, registry);
    this.pairingsHandler = new PairingsHandler(hapStorage, registry, advertiser);
    this.attributesHandler = new AttributesHandler(hapStorage);
    this.registry = registry;
  }

  /**
   * Calls {@link ChannelHandlerContext#fireChannelReadComplete()} to forward to the next {@link
   * ChannelInboundHandler} in the {@link ChannelPipeline}.
   *
   * Flush the channel
   */
  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    logger.debug("Channel read complete, flush the channel.");
    ctx.flush();
  }

  /**
   * Is called for each message of type FullHttpRequest.
   *
   * @param ctx the {@link ChannelHandlerContext} which this handler belongs to
   * @param request the {@link FullHttpRequest} to handle
   */
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request)
      throws JsonProcessingException {
    // Extract local url from request
    String localURL = extractLocalURL(request);

    // Distribute request to sub handlers
    if (localURL.startsWith("/pair-setup")) {
      this.pairSetupHandler.handle(ctx, request);
    } else if (localURL.startsWith("/pair-verify")) {
      this.pairVerifyHandler.handle(ctx, request);
    } else if (localURL.startsWith("/pairings")) {
      this.pairingsHandler.handle(ctx, request);
    } else if (localURL.startsWith("/accessories") ||
        localURL.startsWith("/characteristics") || localURL.startsWith("/identify")) {
      this.attributesHandler.handle(ctx, request);
    } else {
      logger.warn("Received message with invalid local url {}", localURL);
      writeResponse(ctx, request.headers().get(CONTENT_TYPE), NOT_FOUND, buffer(0), false);
    }
  }

  /**
   * Calls {@link ChannelHandlerContext#fireChannelInactive()} to forward to the next {@link
   * ChannelInboundHandler} in the {@link ChannelPipeline}.
   *
   * Check iOSDevicePairingID and remove from registry if necessary
   */
  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    // Load iOSDevicePairingID
    AttributeKey<String> k = AttributeKey.valueOf("iOSDevicePairingID");
    String iOSDevicePairingID = ctx.channel().attr(k).get();

    // Remove from registry
    if (iOSDevicePairingID != null) {
      if (this.registry.removeSession(iOSDevicePairingID, ctx)) {
        logger.info("Connection has been closed, session {} has been removed from registry",
            iOSDevicePairingID);
      }
    }

    ctx.fireChannelInactive();
  }

  /**
   * Calls {@link ChannelHandlerContext#fireExceptionCaught(Throwable)} to forward to the next
   * {@link ChannelHandler} in the {@link ChannelPipeline}.
   *
   * Log and close the connection here
   */
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    logger.error("Unhandled exception: {}", ExceptionUtils.getMessage(cause), cause);
    ctx.close();
  }
}
