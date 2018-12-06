package com.github.longkerdandy.viki.home.hap.storage.mapper;

import static com.github.longkerdandy.viki.home.hap.util.Mappers.toArray;
import static com.github.longkerdandy.viki.home.hap.util.Mappers.toBoolean;
import static com.github.longkerdandy.viki.home.hap.util.Mappers.toDouble;
import static com.github.longkerdandy.viki.home.hap.util.Mappers.toDoubleList;
import static com.github.longkerdandy.viki.home.hap.util.Mappers.toInteger;
import static com.github.longkerdandy.viki.home.hap.util.Mappers.toIntegerList;
import static com.github.longkerdandy.viki.home.hap.util.Mappers.toLong;
import static com.github.longkerdandy.viki.home.hap.util.Mappers.toLongList;

import com.github.longkerdandy.viki.home.hap.model.Characteristic;
import com.github.longkerdandy.viki.home.hap.model.Characteristic.Builder;
import com.github.longkerdandy.viki.home.hap.model.property.Format;
import com.github.longkerdandy.viki.home.hap.model.property.Permission;
import com.github.longkerdandy.viki.home.hap.model.property.Unit;
import com.github.longkerdandy.viki.home.util.ResultSets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

/**
 * Mapper for {@link Characteristic}
 */
public class CharacteristicMapper implements RowMapper<Characteristic> {

  @Override
  public Characteristic map(ResultSet rs, StatementContext ctx) throws SQLException {
    Long instanceId = ResultSets.getLong(rs, "cid");
    String type = ResultSets.getString(rs, "type");
    String value = ResultSets.getString(rs, "_value");
    List<Permission> permissions = toPermissions(ResultSets.getString(rs, "permissions"));
    Boolean enableEvent = toBoolean(ResultSets.getInt(rs, "enable_event"));
    String description = ResultSets.getString(rs, "description");
    Format format = toFormat(ResultSets.getString(rs, "format"));
    Unit unit = toUnit(ResultSets.getString(rs, "unit"));

    Builder builder;
    if (format == Format.BOOL) {
      builder = new Builder<>(type, instanceId, toBoolean(value), permissions, format);
    } else if (format == Format.UINT8 || format == Format.UINT16 || format == Format.INT) {
      builder = new Builder<>(type, instanceId, toInteger(value), permissions, format)
          .minValue(ResultSets.getInt(rs, "min_value"))
          .maxValue(ResultSets.getInt(rs, "max_value"))
          .minStep(ResultSets.getInt(rs, "min_step"))
          .validValues(toIntegerList(ResultSets.getString(rs, "valid_values")))
          .validValuesRange(toIntegerList(ResultSets.getString(rs, "valid_values_range")));
    } else if (format == Format.UINT32 || format == Format.UINT64) {
      builder = new Builder<>(type, instanceId, toLong(value), permissions, format)
          .minValue(ResultSets.getLong(rs, "min_value"))
          .maxValue(ResultSets.getLong(rs, "max_value"))
          .minStep(ResultSets.getLong(rs, "min_step"))
          .validValues(toLongList(ResultSets.getString(rs, "valid_values")))
          .validValuesRange(toLongList(ResultSets.getString(rs, "valid_values_range")));
    } else if (format == Format.FLOAT) {
      builder = new Builder<>(type, instanceId, toDouble(value), permissions, format)
          .minValue(ResultSets.getDouble(rs, "min_value"))
          .maxValue(ResultSets.getDouble(rs, "max_value"))
          .minStep(ResultSets.getDouble(rs, "min_step"))
          .validValues(toDoubleList(ResultSets.getString(rs, "valid_values")))
          .validValuesRange(toDoubleList(ResultSets.getString(rs, "valid_values_range")));
    } else if (format == Format.STRING) {
      builder = new Builder<>(type, instanceId, value, permissions, format)
          .maxLength(ResultSets.getInt(rs, "max_length"));
    } else if (format == Format.DATA) {
      builder = new Builder<>(type, instanceId, value, permissions, format)
          .maxDataLength(ResultSets.getInt(rs, "max_data_length"));
    } else if (format == Format.TLV8) {
      builder = new Builder<>(type, instanceId, value, permissions, format);
    } else {
      throw new AssertionError("Unknown format " + format);
    }
    return builder
        .enableEvent(enableEvent)
        .description(description)
        .unit(unit)
        .build();
  }

  protected List<Permission> toPermissions(String value) {
    if (value != null) {
      return Arrays.stream(toArray(value)).map(Permission::fromValue).collect(Collectors.toList());
    } else {
      return null;
    }
  }

  protected Format toFormat(String value) {
    if (value != null) {
      return Format.fromValue(value);
    } else {
      return null;
    }
  }

  protected Unit toUnit(String value) {
    if (value != null) {
      return Unit.fromValue(value);
    } else {
      return null;
    }
  }
}
