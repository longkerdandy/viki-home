package com.github.longkerdandy.viki.home.addon;

/**
 * Add-on for the specific protocol
 */
public interface ProtocolAddOn {

  /**
   * Get the Add-on name
   *
   * @return Add-on Name
   */
  String getAddOnName();

  /**
   * Initialize
   */
  void init();

  /**
   * Destroy
   */
  void destroy();
}
