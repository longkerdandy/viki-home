package com.github.longkerdandy.viki.home.hap.storage.mapper;

import com.github.longkerdandy.viki.home.hap.model.Bridge;
import com.github.longkerdandy.viki.home.util.ResultSets;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

/**
 * Mapper for {@link Bridge}
 */
public class BridgeMapper implements RowMapper<Bridge> {

  @Override
  public Bridge map(ResultSet rs, StatementContext ctx) throws SQLException {
    Long instanceId = ResultSets.getLong(rs, "aid");
    Integer configNum = ResultSets.getInt(rs, "config_num");
    Integer stateNum = ResultSets.getInt(rs, "state_num");
    String protocolVersion = ResultSets.getString(rs, "protocol_version");
    Integer statusFlag = ResultSets.getInt(rs, "status_flag");
    Integer categoryId = ResultSets.getInt(rs, "category_id");
    byte[] privateKey = ResultSets.getBytes(rs, "private_key");
    byte[] publicKey = ResultSets.getBytes(rs, "public_key");
    return new Bridge(instanceId, configNum, stateNum, protocolVersion, statusFlag, categoryId,
        privateKey, publicKey);
  }
}
