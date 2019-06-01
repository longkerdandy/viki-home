package com.github.longkerdandy.viki.home.mi.model;

import java.net.InetAddress;

/**
 * Gateway
 *
 * Gateway is a control center for XiaoMi smart home. It connection other XiaoMi {@link ZigbeeThing}
 * via Zigbee protocol.
 *
 * Including:
 * <pre>
 *   MiJia Gateway                            Protocol v1.1.2
 *   AQARA Gateway                            TODO: Unsupported yet
 *   AQARA Air Conditioner Controller         TODO: Protocol v2.0.x ?
 * </pre>
 */
public class Gateway {

  private final String gid;                   // gateway id
  private final String model;                 // model
  private final String protocolVersion;       // protocol version
  private final String password;              // password from MiHome App
  private final String token;                 // token
  private final InetAddress address;          // ip address
  private final Integer port;                 // port

  /**
   * Constructor
   */
  public Gateway(String gid, String model, String protocolVersion, String password, String token,
      InetAddress address, Integer port) {
    this.gid = gid;
    this.model = model;
    this.protocolVersion = protocolVersion;
    this.password = password;
    this.token = token;
    this.address = address;
    this.port = port;
  }

  public String getGid() {
    return gid;
  }

  public String getModel() {
    return model;
  }

  public String getProtocolVersion() {
    return protocolVersion;
  }

  public String getPassword() {
    return password;
  }

  public String getToken() {
    return token;
  }

  public InetAddress getAddress() {
    return address;
  }

  public Integer getPort() {
    return port;
  }

  /**
   * Is protocol version 1.x?
   */
  public boolean isProtocolV1() {
    return protocolVersion.startsWith("1");
  }

  /**
   * Is protocol version 2.x?
   */
  public boolean isProtocolV2() {
    return protocolVersion.startsWith("2");
  }

  @Override
  public String toString() {
    return "Gateway{" +
        "gid='" + gid + '\'' +
        ", model='" + model + '\'' +
        ", protocolVersion='" + protocolVersion + '\'' +
        ", password='" + password + '\'' +
        ", token='" + token + '\'' +
        ", address=" + address +
        ", port=" + port +
        '}';
  }
}
