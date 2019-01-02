module com.github.longkerdandy.viki.home.api {
  // exports
  exports com.github.longkerdandy.viki.home.addon;
  exports com.github.longkerdandy.viki.home.storage;
  exports com.github.longkerdandy.viki.home.util;

  // apache commons
  requires transitive org.apache.commons.lang3;
  requires transitive org.apache.commons.configuration2;

  // json
  requires transitive com.fasterxml.jackson.core;
  requires transitive com.fasterxml.jackson.annotation;
  requires transitive com.fasterxml.jackson.databind;
  requires transitive com.fasterxml.jackson.datatype.jsr310;

  // database
  requires transitive java.sql;
  requires transitive jdbi3.core;   // TODO: Automatic Module Name

  // logging
  requires transitive org.slf4j;
}