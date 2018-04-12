module com.github.longkerdandy.viki.home.storage {
  // export
  exports com.github.longkerdandy.viki.home.storage.sqlite;

  // project api
  requires com.github.longkerdandy.viki.home.api;

  // database
  requires sqlite.jdbc;     // TODO: Automatic Module Name
  requires jdbi3.sqlite;    // TODO: Automatic Module Name

  // services
  provides com.github.longkerdandy.viki.home.storage.StorageFactory
      with com.github.longkerdandy.viki.home.storage.sqlite.SQLiteStorageFactory;
}