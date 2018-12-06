package com.github.longkerdandy.viki.home.util;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;

/**
 * Network Util
 */
public class Networks {

  /**
   * Get the Local Address instance represents actual local network interface in use
   *
   * @return Local Address
   * @throws IOException If operation system has network problem
   */
  public static InetAddress getLocalInetAddress() throws IOException {
    try (final DatagramSocket socket = new DatagramSocket()) {
      socket.connect(InetAddress.getByName("223.5.5.5"), 53);
      return socket.getLocalAddress();
    }
  }

  /**
   * Get the Hostname for the given address
   *
   * @param address Local Address
   * @return Hostname
   */
  public static String getLocalHostname(InetAddress address) {
    return address.getHostName();
  }

  /**
   * Get the MAC Address for the given address
   *
   * @param address Local Address
   * @return MAC Address in format of XX:XX:XX:XX:XX:XX
   * @throws IOException If operation system has network problem
   */
  @SuppressWarnings("StringConcatenationInLoop")
  public static String getLocalMacAddress(InetAddress address) throws IOException {
    NetworkInterface network = NetworkInterface.getByInetAddress(address);
    byte[] raw = network.getHardwareAddress();
    String mac = "";
    for (int i = 0; i < raw.length; i++) {
      mac += String.format("%02X%s", raw[i], (i < raw.length - 1) ? ":" : "");
    }
    return mac;
  }
}
