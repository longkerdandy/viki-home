package com.github.longkerdandy.viki.home.hap.http.frame;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.longkerdandy.viki.home.hap.http.request.CharacteristicWriteRequestTarget;
import com.github.longkerdandy.viki.home.hap.http.request.CharacteristicsWriteRequest;
import com.github.longkerdandy.viki.home.hap.util.Ciphers;
import com.github.longkerdandy.viki.home.util.Jacksons;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.BeforeClass;
import org.junit.Test;
import org.whispersystems.curve25519.Curve25519KeyPair;

public class FrameCodecTest {

  private static byte[] AccessoryToControllerKey;
  private static byte[] ControllerToAccessoryKey;

  @BeforeClass
  public static void init() {
    Curve25519KeyPair iOSDeviceKeyPair = Ciphers.curve25519KeyGen();
    Curve25519KeyPair AccessoryKeyPair = Ciphers.curve25519KeyGen();
    byte[] SharedSecret = Ciphers.curve25519SharedSecret(
        iOSDeviceKeyPair.getPublicKey(), AccessoryKeyPair.getPrivateKey());
    AccessoryToControllerKey = Ciphers
        .hkdf(SharedSecret, "Control-Salt", "Control-Read-Encryption-Key", 32);
    ControllerToAccessoryKey = Ciphers
        .hkdf(SharedSecret, "Control-Salt", "Control-Write-Encryption-Key", 32);
  }

  @Test
  public void writingCharacteristicsTest() throws IOException, GeneralSecurityException {
    AttributeKey<byte[]> k1 = AttributeKey.valueOf("AccessoryToControllerKey");
    AttributeKey<byte[]> k2 = AttributeKey.valueOf("ControllerToAccessoryKey");
    AttributeKey<Long> k3 = AttributeKey.valueOf("outboundFrameCount");
    AttributeKey<Long> k4 = AttributeKey.valueOf("inboundFrameCount");
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
    Channel ch = mock(Channel.class);
    @SuppressWarnings("unchecked")
    Attribute<byte[]> attr1 = mock(Attribute.class);
    @SuppressWarnings("unchecked")
    Attribute<Long> attr2 = mock(Attribute.class);
    when(ctx.channel()).thenReturn(ch);
    when(ch.attr(k1)).thenReturn(attr1);
    when(ch.attr(k2)).thenReturn(attr1);
    when(ch.attr(k3)).thenReturn(attr2);
    when(ch.attr(k4)).thenReturn(attr2);
    when(attr1.get()).thenReturn(ControllerToAccessoryKey);
    when(attr2.get()).thenReturn(null);

    List<CharacteristicWriteRequestTarget> list = new ArrayList<>();
    list.add(new CharacteristicWriteRequestTarget(2L, 8L, true));
    list.add(new CharacteristicWriteRequestTarget(3L, 8L, true));
    CharacteristicsWriteRequest request = new CharacteristicsWriteRequest(list);

    FrameCodec codec = new FrameCodec(1, 1);

    byte[] jsonEncode = Jacksons.getWriter().writeValueAsBytes(request);
    ByteBuf msg = Unpooled.buffer(jsonEncode.length);
    msg.writeBytes(jsonEncode);
    ByteBuf b = Unpooled.buffer(0);
    codec.encode(ctx, msg, b);

    assert b.readableBytes() == 2 + jsonEncode.length + 16;

    List<Object> l = new ArrayList<>();
    codec.decode(ctx, b, l);
    byte[] jsonDecode = new byte[jsonEncode.length];
    ((ByteBuf) l.get(0)).readBytes(jsonDecode);

    assert l.size() == 1;
    assert Objects.deepEquals(jsonEncode, jsonDecode);
  }
}
