package com.github.longkerdandy.viki.home.mi.storage.mapper;

import com.github.longkerdandy.viki.home.mi.model.ZigbeeThing;
import com.github.longkerdandy.viki.home.util.ResultSets;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

/**
 * {@link RowMapper} for the {@link ZigbeeThing}
 */
public class ZigbeeThingMapper implements RowMapper<ZigbeeThing> {

  @Override
  public ZigbeeThing map(ResultSet rs, StatementContext ctx) throws SQLException {
    if (rs == null) {
      return null;
    }
    String sid = ResultSets.getString(rs, "sid");
    String gid = ResultSets.getString(rs, "gid");
    String tid = ResultSets.getString(rs, "tid");
    String model = ResultSets.getString(rs, "model");
    Integer shortId = ResultSets.getInt(rs, "short_id");
    return new ZigbeeThing(sid, gid, tid, model, shortId);
  }
}
