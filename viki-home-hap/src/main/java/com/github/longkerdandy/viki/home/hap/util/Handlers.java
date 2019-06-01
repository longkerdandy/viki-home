package com.github.longkerdandy.viki.home.hap.util;

import static io.netty.buffer.Unpooled.buffer;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.longkerdandy.viki.home.hap.http.response.ErrorResponse;
import com.github.longkerdandy.viki.home.hap.http.response.Status;
import com.github.longkerdandy.viki.home.hap.http.tlv.TLVEncoder;
import com.github.longkerdandy.viki.home.hap.http.tlv.TLVError;
import com.github.longkerdandy.viki.home.hap.http.tlv.TLVType;
import com.github.longkerdandy.viki.home.util.Jacksons;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.LinkedHashMap;

/**
 * HTTP Handler Util
 */
public final class Handlers {

  public static final String MIME_TLV8 = "application/pairing+tlv8";
  public static final String MIME_JSON = "application/hap+json";

  private Handlers() {
  }

  /**
   * Return success response with TLV items
   */
  public static void successTLVResponse(ChannelHandlerContext ctx,
      LinkedHashMap<TLVType, Object> tlv) {
    ByteBuf content = TLVEncoder.encode(tlv);
    writeResponse(ctx, MIME_TLV8, OK, content, true);
  }

  /**
   * Return success response with JSON frames
   */
  public static void successJSONResponse(ChannelHandlerContext ctx, HttpResponseStatus status,
      Object response) throws JsonProcessingException {
    ByteBuf content;
    if (response != null) {
      byte[] json = Jacksons.getWriter().writeValueAsBytes(response);
      content = buffer(json.length);
      content.writeBytes(json);
    } else {
      content = buffer(0);
    }
    writeResponse(ctx, MIME_JSON, status, content, true);
  }

  /**
   * Return error response to the client, with the following TLV items: kTLVType_Error <Error>
   */
  public static void errorTLVResponse(ChannelHandlerContext ctx, HttpResponseStatus status,
      TLVError error) {
    LinkedHashMap<TLVType, Object> tlv = new LinkedHashMap<>(2);
    tlv.put(TLVType.ERROR, error);
    ByteBuf content = TLVEncoder.encode(tlv);
    writeResponse(ctx, MIME_TLV8, status, content, false);
  }

  /**
   * Return error response to the client, with the following TLV items: kTLVType_State <State>
   * kTLVType_Error <Error>
   */
  public static void errorTLVResponse(ChannelHandlerContext ctx, HttpResponseStatus status,
      int state, TLVError error) {
    LinkedHashMap<TLVType, Object> tlv = new LinkedHashMap<>(2);
    tlv.put(TLVType.STATE, state);
    tlv.put(TLVType.ERROR, error);
    ByteBuf content = TLVEncoder.encode(tlv);
    writeResponse(ctx, MIME_TLV8, status, content, false);
  }

  /**
   * Return error response to the client, with JSON frame has single HAP Status Code
   */
  public static void errorJSONResponse(ChannelHandlerContext ctx, HttpResponseStatus status,
      Status hapStatus) throws JsonProcessingException {
    ErrorResponse response = new ErrorResponse(hapStatus);
    byte[] json = Jacksons.getWriter().writeValueAsBytes(response);
    ByteBuf content = buffer(json.length);
    content.writeBytes(json);
    writeResponse(ctx, MIME_JSON, status, content, false);
  }

  /**
   * Write response to the {@link ChannelHandlerContext}
   *
   * @param ctx {@link ChannelHandlerContext}
   * @param contentType HTTP Content Type
   * @param status {@link HttpResponseStatus}
   * @param content HTTP Content(Body) as {@link ByteBuf}
   */
  public static void writeResponse(ChannelHandlerContext ctx, String contentType,
      HttpResponseStatus status, ByteBuf content, boolean keepAlive) {
    FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, content);
    if (contentType != null) {
      response.headers().set(CONTENT_TYPE, contentType);
    }
    response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
    if (!keepAlive) {
      ctx.write(response).addListener(ChannelFutureListener.CLOSE);
    } else {
      response.headers().set(CONNECTION, KEEP_ALIVE);
      ctx.write(response);
    }
  }

  /**
   * Wrap byte array to {@link ByteBuf}
   *
   * @param bytes byte array
   * @return {@link ByteBuf}
   */
  public static ByteBuf bytesToBuffer(byte[] bytes) {
    return wrappedBuffer(bytes);
  }

  /**
   * Read {@link ByteBuf} to byte array
   *
   * @param buffer {@link ByteBuf}
   * @return byte array
   */
  public static byte[] bufferToBytes(ByteBuf buffer) {
    byte[] bytes = new byte[buffer.readableBytes()];
    buffer.readBytes(bytes);
    return bytes;
  }

  /**
   * Extract the local url path from the HttpRequest
   *
   * @param request {@link HttpRequest}
   * @return local url path
   */
  public static String extractLocalURL(HttpRequest request) {
    // Get requested URL
    String url = request.uri();

    // Extract path
    int idx;
    if ((idx = url.indexOf("://")) != -1) {
      int localPartStart = url.indexOf('/', idx + 3);
      if (localPartStart != -1) {
        url = url.substring(localPartStart + 1);
      } else {
        url = "/";
      }
    }

    return url;
  }
}
