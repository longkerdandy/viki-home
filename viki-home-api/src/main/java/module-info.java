module com.github.longkerdandy.viki.home.api {
  // for jackson only
  opens com.github.longkerdandy.viki.home.schema to com.fasterxml.jackson.databind;

  // exports
  exports com.github.longkerdandy.viki.home.ext;
  exports com.github.longkerdandy.viki.home.model;
  exports com.github.longkerdandy.viki.home.schema;
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
  requires transitive sqlite.jdbc;            // TODO: Automatic Module Name
  requires transitive org.jdbi.v3.core;
  requires transitive org.jdbi.v3.sqlite;

  // logging
  requires transitive org.slf4j;
}