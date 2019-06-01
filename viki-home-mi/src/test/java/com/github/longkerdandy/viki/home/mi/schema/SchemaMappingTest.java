package com.github.longkerdandy.viki.home.mi.schema;

import java.util.Arrays;
import java.util.Map;
import org.junit.Test;

public class SchemaMappingTest {

  @Test
  public void decodeMiJiaGatewayTest() {
    Map<String, Object> properties = SchemaMapping.decodeMiJiaGateway(
        "gateway", Map.of("rgb", 4278255360L));
    assert properties != null;
    assert Arrays.equals(new long[]{0L, 255L, 0L}, (long[]) properties.get("rgb"));

    properties = SchemaMapping.decodeMiJiaGateway(
        "gateway", Map.of("rgb", 0L));
    assert properties != null;
    assert Arrays.equals(new long[]{0L, 0L, 0L}, (long[]) properties.get("rgb"));
  }

  @Test
  public void encodeMiJiaGatewayTest() {
    Map<String, Object> params = SchemaMapping.encodeMiJiaGateway(
        "gateway", Map.of("rgb", new long[]{0L, 255L, 0L}));
    assert params != null;
    assert (long) params.get("rgb") == 4278255360L;

    params = SchemaMapping.encodeMiJiaGateway(
        "gateway", Map.of("rgb", new long[]{0L, 0L, 0L}));
    assert params != null;
    assert (long) params.get("rgb") == 0L;
  }
}
