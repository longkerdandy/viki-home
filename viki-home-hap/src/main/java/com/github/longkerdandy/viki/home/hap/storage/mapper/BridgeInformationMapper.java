package com.github.longkerdandy.viki.home.hap.storage.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

/**
 * Mapper for bridge information
 */
public class BridgeInformationMapper implements RowMapper<Map<String, ?>> {

  @Override
  public Map<String, ?> map(ResultSet rs, StatementContext ctx) throws SQLException {
    Map<String, Object> map = new HashMap<>();
    map.put("aid", rs.getInt("aid"));
    map.put("config_num", rs.getInt("config_num"));
    map.put("protocol_version", rs.getString("protocol_version"));
    map.put("state_num", rs.getInt("state_num"));
    map.put("status_flag", rs.getInt("status_flag"));
    map.put("category_id", rs.getInt("category_id"));
    map.put("private_key", rs.getBytes("private_key"));
    map.put("public_key", rs.getBytes("public_key"));
    return map;
  }
}
