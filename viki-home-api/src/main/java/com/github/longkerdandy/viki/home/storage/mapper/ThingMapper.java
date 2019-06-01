package com.github.longkerdandy.viki.home.storage.mapper;

import com.github.longkerdandy.viki.home.model.Thing;
import com.github.longkerdandy.viki.home.util.ResultSets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

/**
 * {@link RowMapper} for the {@link Thing}
 */
public class ThingMapper implements RowMapper<Thing> {

  @Override
  public Thing map(ResultSet rs, StatementContext ctx) throws SQLException {
    if (rs == null) {
      return null;
    }
    String id = ResultSets.getString(rs, "id");
    String schema = ResultSets.getString(rs, "_schema");
    Long d = ResultSets.getLong(rs, "heartbeat");
    LocalDateTime heartbeat = d == null ? null :
        LocalDateTime.ofInstant(Instant.ofEpochSecond(d), ZoneId.systemDefault());
    return new Thing(id, schema, null, heartbeat);
  }
}
