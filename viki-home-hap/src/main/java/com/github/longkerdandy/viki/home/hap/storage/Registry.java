package com.github.longkerdandy.viki.home.hap.storage;

import io.netty.channel.ChannelHandlerContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session registry for iOS pairing device
 */
public class Registry {

  // Thread safe HashMap as Repository (iOSDevicePairingID : ChannelHandlerContext)
  private final Map<String, ChannelHandlerContext> repo = new ConcurrentHashMap<>();

  /**
   * Save session for the device
   *
   * @param deviceId iOSDevicePairingID
   * @param session ChannelHandlerContext as Session
   */
  public void saveSession(String deviceId, ChannelHandlerContext session) {
    this.repo.put(deviceId, session);
  }

  /**
   * Get session for the device
   *
   * @param deviceId iOSDevicePairingID
   * @return ChannelHandlerContext as Session
   */
  public ChannelHandlerContext getSession(String deviceId) {
    return this.repo.get(deviceId);
  }

  /**
   * Remove session for the device
   *
   * @param deviceId iOSDevicePairingID
   * @return Removed Session
   */
  public ChannelHandlerContext removeSession(String deviceId) {
    return this.repo.remove(deviceId);
  }

  /**
   * Remove session for the device. Only if it is currently mapped to the specified value.
   *
   * @param deviceId iOSDevicePairingID
   * @param session ChannelHandlerContext as Session
   * @return {@code true} if the value was removed
   */
  public boolean removeSession(String deviceId, ChannelHandlerContext session) {
    return this.repo.remove(deviceId, session);
  }
}
