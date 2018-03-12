module com.github.longkerdandy.viki.home.storage {
  // project api
  requires com.github.longkerdandy.viki.home.api;

  // jdbc
  requires java.sql;
  requires sqlite.jdbc;     // TODO: Automatic Module Name
  requires jdbi3.core;      // TODO: Automatic Module Name
  requires jdbi3.sqlite;    // TODO: Automatic Module Name

  // dependencies
  requires org.apache.commons.lang3;
  requires org.apache.commons.configuration2;
}