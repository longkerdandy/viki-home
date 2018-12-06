package com.github.longkerdandy.viki.home.hap.http.frame;

import static com.github.longkerdandy.viki.home.hap.util.Ciphers.chaCha20Poly1305Decrypt;
import static com.github.longkerdandy.viki.home.hap.util.Ciphers.chaCha20Poly1305Encrypt;
import static com.github.longkerdandy.viki.home.hap.util.Ciphers.chaCha20Poly1305Nonce;
import static com.github.longkerdandy.viki.home.hap.util.Ciphers.longToLittleEndian;
import static io.netty.buffer.Unpooled.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.util.AttributeKey;
import java.security.GeneralSecurityException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encoder and decoder for HomeKit Accessory Protocol's HTTP Frame
 *
 * Each HTTP message is split into frames no larger than 1024 bytes. Each frame has the following
 * format: <2:AAD for little endian length of encrypted data (n) in bytes> <n:encrypted data
 * according to AEAD algorithm, up to 1024 bytes> <16:authTag according to AEAD algorithm>
 */
public class FrameCodec extends ByteToMessageCodec<ByteBuf> {

  private static final Logger logger = LoggerFactory.getLogger(FrameCodec.class);

  // count
  private long inboundFrameCount = 0;
  private long outboundFrameCount = 0;

  // previous decrypted frames
  private ByteBuf frames = buffer(1024);

  /**
   * Constructor
   */
  public FrameCodec() {
    super();
  }

  /**
   * Constructor
   *
   * @param inboundFrameCount inbound frame count
   * @param outboundFrameCount outbound frame count
   */
  protected FrameCodec(long inboundFrameCount, long outboundFrameCount) {
    this();
    this.inboundFrameCount = inboundFrameCount;
    this.outboundFrameCount = outboundFrameCount;
  }

  @Override
  protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {
    logger.debug("Encoding http frame to {}", ctx.channel().remoteAddress());

    // Check AccessoryToControllerKey
    AttributeKey<byte[]> k = AttributeKey.valueOf("AccessoryToControllerKey");
    byte[] AccessoryToControllerKey = ctx.channel().attr(k).get();
    if (AccessoryToControllerKey == null) {
      logger.error("Sent http frame without AccessoryToControllerKey, closing the connection.");
      ctx.close();
    }

    // Must received some inbound frames first
    if (this.inboundFrameCount == 0) {
      out.writeBytes(msg);
    }

    // Because each HTTP message is split into frames no larger than 1024 bytes,
    // Loop to encrypt each frame, save encrypted frame into buffer.
    while (msg.readableBytes() > 0) {
      // Encrypt the message data
      int length = Math.min(msg.readableBytes(), 1024);
      byte[] additionalData = new byte[2];
      buffer(2).writeShortLE(length).readBytes(additionalData);
      byte[] nonce = chaCha20Poly1305Nonce(longToLittleEndian(this.outboundFrameCount++));
      byte[] plainText = new byte[length];
      msg.readBytes(plainText);
      byte[] cipherText = chaCha20Poly1305Encrypt(AccessoryToControllerKey, plainText, nonce,
          additionalData);

      // Write to the output buffer
      if (out.writableBytes() < 2 + length + 16) {
        out.capacity(out.readableBytes() + 2 + length + 16);
      }
      out.writeShortLE(length);
      out.writeBytes(cipherText);
    }
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
      throws GeneralSecurityException {
    logger.debug("Decoding http frame from {}", ctx.channel().remoteAddress());

    // Check ControllerToAccessoryKey
    AttributeKey<byte[]> k = AttributeKey.valueOf("ControllerToAccessoryKey");
    byte[] ControllerToAccessoryKey = ctx.channel().attr(k).get();
    if (ControllerToAccessoryKey == null) {
      logger.error("Received http frame without ControllerToAccessoryKey, closing the connection.");
      ctx.close();
    }

    // Because each HTTP message is split into frames no larger than 1024 bytes,
    // Loop to check and decrypt each frame, save decrypted frame into buffer.
    // If there is any incomplete frame, wait for further incoming data.
    // If decrypt or verify fails, exception will be thrown and connection will be closed.
    while (in.readableBytes() >= 2 + 16) {
      // Check the frame integrity without messing its readerIndex
      int length = in.getShortLE(in.readerIndex());
      int availableLength = in.readableBytes() - 2 - 16;

      // Not enough data
      if (availableLength < length) {
        return;
      }

      // Decrypt the encrypted data
      byte[] nonce = chaCha20Poly1305Nonce(longToLittleEndian(this.inboundFrameCount++));
      byte[] additionalData = new byte[2];
      in.readBytes(additionalData);
      byte[] cipherText = new byte[length + 16];
      in.readBytes(cipherText, 0, length + 16);
      byte[] plainText = chaCha20Poly1305Decrypt(ControllerToAccessoryKey, cipherText, nonce,
          additionalData);

      // Save the decrypted frame, expand if needed
      if (this.frames.writableBytes() < length) {
        this.frames.capacity(this.frames.readableBytes() + length);
      }
      this.frames.writeBytes(plainText);
    }

    // Add the combined frames to the result
    out.add(this.frames);

    // Create new buffer for decrypted frames
    this.frames = buffer(1024);
  }
}
