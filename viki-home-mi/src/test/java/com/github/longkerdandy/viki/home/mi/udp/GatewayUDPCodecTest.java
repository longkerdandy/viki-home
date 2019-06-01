package com.github.longkerdandy.viki.home.mi.udp;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class GatewayUDPCodecTest {

  @Test
  public void encryptKeyTest() {
    assert GatewayUDPCodec.encryptKey("0987654321qwerty", "1234567890abcdef").equals("3EB43E37C20AFF4C5872CC0D04D81314");
  }

  @Test
  public void encodeMessageV1Test() throws IOException {
    Map<String, Object> map = Map.of("cmd", "write", "model", "ctrl_neutral1",
        "sid", "158d0000123456", "short_id", 4343,
        "data", new LinkedHashMap<>(Map.of("channel_0", "on", "channel_1", "off")));
    String json = new String(GatewayUDPCodec.encodeRequest(new LinkedHashMap<>(map)));

    assert json.length() > 0;
    assert json.contains("\"cmd\":\"write\"");
    assert json.contains("\"model\":\"ctrl_neutral1\"");
    assert json.contains("\"sid\":\"158d0000123456\"");
    assert json.contains("\"short_id\":4343");
    assert json.contains("\\\"channel_0\\\":\\\"on\\\"");
    assert json.contains("\\\"channel_1\\\":\\\"off\\\"");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void decodeMessageV1Test1() throws IOException {
    String json = "{\"cmd\":\"get_id_list_ack\",\"sid\":\"1022780\",\"token\":\"passw0rd\",\"data\":\"[\\\"sid1\\\",\\\"sid2\\\",\\\"sid3\\\"]\"}";
    Map<String, Object> map = GatewayUDPCodec.decodeResponse(json.getBytes());

    assert map.get("cmd").equals("get_id_list_ack");
    assert map.get("sid").equals("1022780");
    assert map.get("token").equals("passw0rd");
    assert map.get("data") instanceof List;
    assert ((List<String>) map.get("data")).get(0).equals("sid1");
    assert ((List<String>) map.get("data")).get(1).equals("sid2");
    assert ((List<String>) map.get("data")).get(2).equals("sid3");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void decodeMessageV1Test2() throws IOException {
    String json = "{\"cmd\":\"write_ack\",\"model\":\"ctrl_neutral2\",\"sid\":\"158d0000123456\",\"short_id\":4343,\"data\":\"{\\\"channel_0\\\":\\\"on\\\",\\\"channel_1\\\":\\\"off\\\"}\"}";
    Map<String, Object> map = GatewayUDPCodec.decodeResponse(json.getBytes());

    assert map.get("cmd").equals("write_ack");
    assert map.get("model").equals("ctrl_neutral2");
    assert map.get("sid").equals("158d0000123456");
    assert (int) map.get("short_id") == 4343;
    assert map.get("data") instanceof Map;
    assert ((Map<String, Object>) map.get("data")).get("channel_0").equals("on");
    assert ((Map<String, Object>) map.get("data")).get("channel_1").equals("off");
  }

  @Test
  public void encodeMessageV2Test() throws IOException {
    Map<String, Object> map = Map.of("cmd", "write", "model", "ctrl_neutral1",
        "sid", "xxxxxxxx", "key", "3EB43E37C20AFF4C5872CC0D04D81314",
        "params", new LinkedHashMap<>(Map.of("channel_0", "on", "channel_1", "off")));
    String json = new String(GatewayUDPCodec.encodeRequest(new LinkedHashMap<>(map)));

    assert json.length() > 0;
    assert json.contains("\"cmd\":\"write\"");
    assert json.contains("\"model\":\"ctrl_neutral1\"");
    assert json.contains("\"sid\":\"xxxxxxxx\"");
    assert json.contains("\"key\":\"3EB43E37C20AFF4C5872CC0D04D81314\"");
    assert json.contains("{\"channel_1\":\"off\"}");
    assert json.contains("{\"channel_0\":\"on\"}");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void decodeMessageV2Test1() throws IOException {
    String json = "{\n"
        + "   \"cmd\":\"discovery_rsp\",\n"
        + "   \"sid\":\"158d323123c9d9\",\n"
        + "   \"token\":\"TahkC7dalbIhXG22\",\n"
        + "   \"dev_list\":[{\"sid\":\"xxxxxxxx1\",\"model\":\"plug\"},\n"
        + "               {\"sid\":\"xxxxxxxx2\",\"model\":\"sensor_switch.aq2\"}]\n"
        + "}";
    Map<String, Object> map = GatewayUDPCodec.decodeResponse(json.getBytes());

    assert map.get("cmd").equals("discovery_rsp");
    assert map.get("sid").equals("158d323123c9d9");
    assert map.get("token").equals("TahkC7dalbIhXG22");
    assert map.get("dev_list") instanceof Map;
    assert ((Map<String, Object>) map.get("dev_list")).get("xxxxxxxx1").equals("plug");
    assert ((Map<String, Object>) map.get("dev_list")).get("xxxxxxxx2").equals("sensor_switch.aq2");
  }

  @SuppressWarnings("unchecked")
  @Test
  public void decodeMessageV2Test2() throws IOException {
    String json = "{\n"
        + "   \"cmd\":\"read_rsp\",\n"
        + "   \"model\":\"ctrl_neutral2\",\n"
        + "   \"sid\":\"xxxxxxxx\",\n"
        + "   \"params\":[{\"channel_0\":\"on\"},{\"channel_1\":\"off\"}]  \n"
        + "}";
    Map<String, Object> map = GatewayUDPCodec.decodeResponse(json.getBytes());

    assert map.get("cmd").equals("read_rsp");
    assert map.get("model").equals("ctrl_neutral2");
    assert map.get("sid").equals("xxxxxxxx");
    assert map.get("params") instanceof Map;
    assert ((Map<String, Object>) map.get("params")).get("channel_0").equals("on");
    assert ((Map<String, Object>) map.get("params")).get("channel_1").equals("off");
  }
}
