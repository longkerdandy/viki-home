package com.github.longkerdandy.viki.home.mi.storage.mapper;

import com.github.longkerdandy.viki.home.mi.model.Gateway;
import com.github.longkerdandy.viki.home.util.ResultSets;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

/**
 * {@link RowMapper} for the {@link Gateway}
 */
public class GatewayMapper implements RowMapper<Gateway> {

  @Override
  public Gateway map(ResultSet rs, StatementContext ctx) throws SQLException {
    if (rs == null) {
      return null;
    }
    try {
      String gid = ResultSets.getString(rs, "gid");
      String model = ResultSets.getString(rs, "model");
      String protocolVersion = ResultSets.getString(rs, "protocol_version");
      String password = ResultSets.getString(rs, "gid");
      String token = ResultSets.getString(rs, "token");
      String address = ResultSets.getString(rs, "address");
      Integer port = ResultSets.getInt(rs, "port");
      return new Gateway(gid, model, protocolVersion, password, token,
          address != null ? InetAddress.getByName(address) : null, port);
    } catch (UnknownHostException e) {
      throw new IllegalStateException("Illegal gateway ip address", e);
    }
  }
}
