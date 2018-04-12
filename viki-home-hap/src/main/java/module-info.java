module com.github.longkerdandy.viki.home.hap {
  // project api
  requires com.github.longkerdandy.viki.home.api;

  // bonjour
  requires jmdns;   // TODO: Automatic Module Name

  // service
  provides com.github.longkerdandy.viki.home.addon.ProtocolAddOnFactory
      with com.github.longkerdandy.viki.home.hap.HomeKitProtocolAddOnFactory;
}