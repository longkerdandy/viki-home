package com.github.longkerdandy.viki.home.storage.mapper;

import static com.github.longkerdandy.viki.home.util.Jacksons.getReader;
import static java.util.Base64.getDecoder;

import com.github.longkerdandy.viki.home.model.DataType;
import com.github.longkerdandy.viki.home.model.Property;
import com.github.longkerdandy.viki.home.util.ResultSets;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

/**
 * {@link RowMapper} for the {@link Property}
 */
public class PropertyMapper implements RowMapper<Property> {

  @Override
  public Property map(ResultSet rs, StatementContext ctx) throws SQLException {
    if (rs == null) {
      return null;
    }
    String name = ResultSets.getString(rs, "name");
    DataType type = DataType.fromValue(ResultSets.getString(rs, "type"));
    Long d = ResultSets.getLong(rs, "updated_at");
    LocalDateTime updatedAt = d == null ? null :
        LocalDateTime.ofInstant(Instant.ofEpochSecond(d), ZoneId.systemDefault());
    String v = ResultSets.getString(rs, "_value");
    try {
      switch (type) {
        case INTEGER:
          return new Property<>(name, type, getReader(Long.class).readValue(v), updatedAt);
        case NUMBER:
          return new Property<>(name, type, getReader(Double.class).readValue(v), updatedAt);
        case STRING:
          return new Property<>(name, type, getReader(String.class).readValue(v), updatedAt);
        case BOOLEAN:
          return new Property<>(name, type, getReader(Boolean.class).readValue(v), updatedAt);
        case DATETIME:
          return new Property<>(name, type, getReader(LocalDateTime.class).readValue(v), updatedAt);
        case ARRAY_INTEGER:
          return new Property<>(name, type, getReader(long[].class).readValue(v), updatedAt);
        case ARRAY_NUMBER:
          return new Property<>(name, type, getReader(double[].class).readValue(v), updatedAt);
        case ARRAY_STRING:
          return new Property<>(name, type, getReader(String[].class).readValue(v), updatedAt);
        case BLOB:
          return new Property<>(name, type, getDecoder().decode(v), updatedAt);
        default:
          throw new IllegalStateException("Unknown data type " + type);
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
