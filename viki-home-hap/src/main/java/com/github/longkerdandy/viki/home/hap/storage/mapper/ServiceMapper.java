package com.github.longkerdandy.viki.home.hap.storage.mapper;

import static com.github.longkerdandy.viki.home.hap.util.Mappers.toBoolean;
import static com.github.longkerdandy.viki.home.hap.util.Mappers.toLongList;

import com.github.longkerdandy.viki.home.hap.model.Service;
import com.github.longkerdandy.viki.home.hap.model.Service.Builder;
import com.github.longkerdandy.viki.home.util.ResultSets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

/**
 * Mapper for {@link Service}
 */
public class ServiceMapper implements RowMapper<Service> {

  @Override
  public Service map(ResultSet rs, StatementContext ctx) throws SQLException {
    Long instanceId = ResultSets.getLong(rs, "sid");
    String type = ResultSets.getString(rs, "type");
    Boolean isHidden = toBoolean(ResultSets.getInt(rs, "is_hidden"));
    Boolean isPrimary = toBoolean(ResultSets.getInt(rs, "is_primary"));
    List<Long> linkedServiceIds = toLongList(ResultSets.getString(rs, "linked_services"));

    Builder builder = new Builder(type, instanceId, null);
    return builder
        .isHidden(isHidden)
        .isPrimary(isPrimary)
        .linkedServiceIds(linkedServiceIds)
        .build();
  }
}
