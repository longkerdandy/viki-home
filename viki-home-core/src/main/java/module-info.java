module com.github.longkerdandy.viki.home.core {
  // project api
  requires com.github.longkerdandy.viki.home.api;

  // database migration
  requires org.flywaydb.core;

  // services
  uses com.github.longkerdandy.viki.home.storage.StorageFactory;
  uses com.github.longkerdandy.viki.home.addon.ProtocolAddOnFactory;
}