package com.github.longkerdandy.viki.home.ext;

/**
 * Extension for smart thing controller
 */
public interface ControllerExt {

  /**
   * Get the extension name
   *
   * @return Extension Name
   */
  String getExtName();

  /**
   * Initialize
   */
  void init() throws Exception;

  /**
   * Destroy
   */
  void destroy() throws Exception;
}
