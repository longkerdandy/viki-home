package com.github.longkerdandy.viki.home.util;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class NetworksTest {

  @Test
  public void testGetLocalInetAddress() throws IOException {
    InetAddress address = Networks.getLocalInetAddress();
    assert address != null;
    assert !address.isAnyLocalAddress();
    assert !address.isLoopbackAddress();
    assert !address.isLinkLocalAddress();
    String ip = address.getHostAddress();
    String hostname = address.getHostName();
    assert StringUtils.isNotBlank(ip);
    assert StringUtils.isNotBlank(hostname);
    if (address instanceof Inet4Address) {
      assert !ip.equals("0.0.0.0");
      assert !ip.startsWith("127.0.0.");
    }
    if (address instanceof Inet6Address) {
      assert !ip.startsWith("::");
      assert !ip.startsWith("fe80::");
    }
  }

  @Test
  public void testGetHostname() throws IOException {
    String hostname = Networks.getHostname(Networks.getLocalInetAddress());
    assert hostname != null;
    assert StringUtils.isNotBlank(hostname);
  }

  @Test
  public void testGetMacAddress() throws IOException {
    String mac = Networks.getMacAddress(Networks.getLocalInetAddress());
    assert mac != null;
    assert StringUtils.isNotBlank(mac);
    assert mac.matches("\\w{2}:\\w{2}:\\w{2}:\\w{2}:\\w{2}:\\w{2}");
  }
}
