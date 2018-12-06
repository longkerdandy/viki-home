package com.github.longkerdandy.viki.home.hap.storage.mapper;

import com.github.longkerdandy.viki.home.hap.model.Accessory;
import com.github.longkerdandy.viki.home.util.ResultSets;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

/**
 * Mapper for {@link Accessory}
 */
public class AccessoryMapper implements RowMapper<Accessory> {

  @Override
  public Accessory map(ResultSet rs, StatementContext ctx) throws SQLException {
    Long instanceId = ResultSets.getLong(rs, "aid");

    return new Accessory(instanceId, null);
  }
}
