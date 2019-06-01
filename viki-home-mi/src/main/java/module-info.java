module com.github.longkerdandy.viki.home.mi {
  // project api
  requires com.github.longkerdandy.viki.home.api;

  // database migration
  requires org.flywaydb.core;

  // apache commons
  requires org.apache.commons.collections4;
  requires org.apache.commons.codec;

  // service
  provides com.github.longkerdandy.viki.home.ext.SmartThingExtFactory
      with com.github.longkerdandy.viki.home.mi.MiProtocolExtFactory;
}