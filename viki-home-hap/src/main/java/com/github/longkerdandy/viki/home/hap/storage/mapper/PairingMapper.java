package com.github.longkerdandy.viki.home.hap.storage.mapper;

import com.github.longkerdandy.viki.home.hap.model.Pairing;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

/**
 * Mapper for {@link Pairing}
 */
public class PairingMapper implements RowMapper<Pairing> {

  @Override
  public Pairing map(ResultSet rs, StatementContext ctx) throws SQLException {
    String paringId = rs.getString("pairing_id");
    byte[] publicKey = rs.getBytes("public_key");
    int permissions = rs.getInt("permissions");
    return new Pairing(paringId, publicKey, permissions);
  }
}
