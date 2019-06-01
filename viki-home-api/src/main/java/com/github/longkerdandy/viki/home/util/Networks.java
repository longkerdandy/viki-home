package com.github.longkerdandy.viki.home.util;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;

/**
 * Network Util
 */
public class Networks {

  private Networks() {
  }

  /**
   * Get the {@link InetAddress} instance represents actual local network interface in use
   *
   * @return {@link InetAddress}
   * @throws IOException If operation system has network problem
   */
  public static InetAddress getLocalInetAddress() throws IOException {
    try (final DatagramSocket socket = new DatagramSocket()) {
      socket.connect(InetAddress.getByName("223.5.5.5"), 53);
      return socket.getLocalAddress();
    }
  }

  /**
   * Get the ip address for the given address
   *
   * @param address {@link InetAddress}
   * @return ip address
   */
  public static String getIPAddress(InetAddress address) {
    return address.getHostAddress();
  }

  /**
   * Get the ip address for the given address
   *
   * @param address {@link SocketAddress}
   * @return ip address
   */
  public static String getIPAddress(SocketAddress address) {
    return getIPAddress(((InetSocketAddress) address).getAddress());
  }

  /**
   * Get the Hostname for the given address
   *
   * @param address {@link InetAddress}
   * @return Hostname
   */
  public static String getHostname(InetAddress address) {
    return address.getHostName();
  }

  /**
   * Get the MAC Address for the given address
   *
   * @param address {@link InetAddress}
   * @return MAC Address in format of XX:XX:XX:XX:XX:XX
   * @throws IOException If operation system has network problem
   */
  @SuppressWarnings("StringConcatenationInLoop")
  public static String getMacAddress(InetAddress address) throws IOException {
    NetworkInterface network = NetworkInterface.getByInetAddress(address);
    byte[] raw = network.getHardwareAddress();
    String mac = "";
    for (int i = 0; i < raw.length; i++) {
      mac += String.format("%02X%s", raw[i], (i < raw.length - 1) ? ":" : "");
    }
    return mac;
  }
}
