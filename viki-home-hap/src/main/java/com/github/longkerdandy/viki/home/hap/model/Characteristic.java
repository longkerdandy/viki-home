package com.github.longkerdandy.viki.home.hap.model;

import static com.github.longkerdandy.viki.home.hap.model.property.Permission.PAIRED_READ;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.longkerdandy.viki.home.hap.model.property.Format;
import com.github.longkerdandy.viki.home.hap.model.property.Permission;
import com.github.longkerdandy.viki.home.hap.model.property.Type;
import com.github.longkerdandy.viki.home.hap.model.property.Unit;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

/**
 * HomeKit Accessory Protocol Characteristic Object
 *
 * @param <T> Value type according to the Format
 */
public class Characteristic<T> {

  private UUID type;
  @JsonProperty("iid")
  private Long instanceId;
  // Fact: null value must be excluded
  // @JsonInclude()
  private T value;
  @JsonProperty("perms")
  private List<Permission> permissions;
  @JsonProperty("ev")
  private Boolean enableEvent;
  private String description;
  private Format format;
  private Unit unit;
  private T minValue;
  private T maxValue;
  private T minStep;
  @JsonProperty("maxLen")
  private Integer maxLength;
  @JsonProperty("maxDataLen")
  private Integer maxDataLength;
  @JsonInclude(Include.NON_EMPTY)
  @JsonProperty("valid-values")
  private List<T> validValues;
  @JsonInclude(Include.NON_EMPTY)
  @JsonProperty("valid-values-range")
  private List<T> validValuesRange;

  private Characteristic() {
  }

  private Characteristic(Builder<T> builder) {
    this.type = builder.type;
    this.instanceId = builder.instanceId;
    this.value = builder.value;
    this.permissions = builder.permissions;
    this.enableEvent = builder.enableEvent;
    this.description = builder.description;
    this.format = builder.format;
    this.unit = builder.unit;
    this.minValue = builder.minValue;
    this.maxValue = builder.maxValue;
    this.minStep = builder.minStep;
    this.maxLength = builder.maxLength;
    this.maxDataLength = builder.maxDataLength;
    this.validValues = builder.validValues;
    this.validValuesRange = builder.validValuesRange;
  }

  public UUID getType() {
    return type;
  }

  public void setType(UUID type) {
    this.type = type;
  }

  @JsonProperty("type")
  public String getShortType() {
    String s = type.toString().toUpperCase();
    if (s.length() == 36 && s.endsWith(Type.BASE_UUID_SUFFIX)) {
      return StringUtils.stripStart(s.substring(0, 8), "0");
    } else {
      return s;
    }
  }

  @JsonProperty("type")
  public void setShortType(String type) {
    if (type.length() <= 8) {
      type = StringUtils.leftPad(type, 8, "0") + Type.BASE_UUID_SUFFIX;
    }
    this.type = UUID.fromString(type);
  }

  public Long getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(Long instanceId) {
    this.instanceId = instanceId;
  }

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
  }

  public List<Permission> getPermissions() {
    return permissions;
  }

  public void setPermissions(List<Permission> permissions) {
    this.permissions = permissions;
  }

  public Boolean getEnableEvent() {
    return enableEvent;
  }

  public void setEnableEvent(Boolean enableEvent) {
    this.enableEvent = enableEvent;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Format getFormat() {
    return format;
  }

  public void setFormat(Format format) {
    this.format = format;
  }

  public Unit getUnit() {
    return unit;
  }

  public void setUnit(Unit unit) {
    this.unit = unit;
  }

  public T getMinValue() {
    return minValue;
  }

  public void setMinValue(T minValue) {
    this.minValue = minValue;
  }

  public T getMaxValue() {
    return maxValue;
  }

  public void setMaxValue(T maxValue) {
    this.maxValue = maxValue;
  }

  public T getMinStep() {
    return minStep;
  }

  public void setMinStep(T minStep) {
    this.minStep = minStep;
  }

  public Integer getMaxLength() {
    return maxLength;
  }

  public void setMaxLength(Integer maxLength) {
    this.maxLength = maxLength;
  }

  public Integer getMaxDataLength() {
    return maxDataLength;
  }

  public void setMaxDataLength(Integer maxDataLength) {
    this.maxDataLength = maxDataLength;
  }

  public List<T> getValidValues() {
    return validValues;
  }

  public void setValidValues(List<T> validValues) {
    this.validValues = validValues;
  }

  public List<T> getValidValuesRange() {
    return validValuesRange;
  }

  public void setValidValuesRange(List<T> validValuesRange) {
    this.validValuesRange = validValuesRange;
  }

  @Override
  public String toString() {
    return "Characteristic{" +
        "type=" + type +
        ", instanceId=" + instanceId +
        ", value=" + value +
        ", permissions=" + permissions +
        ", enableEvent=" + enableEvent +
        ", description='" + description + '\'' +
        ", format=" + format +
        ", unit=" + unit +
        ", minValue=" + minValue +
        ", maxValue=" + maxValue +
        ", minStep=" + minStep +
        ", maxLength=" + maxLength +
        ", maxDataLength=" + maxDataLength +
        ", validValues=" + validValues +
        ", validValuesRange=" + validValuesRange +
        '}';
  }

  public static class Builder<T> {

    private UUID type;
    private Long instanceId;
    private T value;
    private List<Permission> permissions;
    private Boolean enableEvent;
    private String description;
    private Format format;
    private Unit unit;
    private T minValue;
    private T maxValue;
    private T minStep;
    private Integer maxLength;
    private Integer maxDataLength;
    private List<T> validValues;
    private List<T> validValuesRange;

    public Builder(String type, Long instanceId, T value, List<Permission> permissions,
        Format format) {
      this.type = UUID.fromString(type);
      this.instanceId = instanceId;
      this.value = value;
      this.permissions = permissions;
      this.format = format;
    }

    public Builder(UUID type, Long instanceId, T value, List<Permission> permissions,
        Format format) {
      this.type = type;
      this.instanceId = instanceId;
      this.value = value;
      this.permissions = permissions;
      this.format = format;
    }

    public Builder<T> enableEvent(Boolean enableEvent) {
      this.enableEvent = enableEvent;
      return this;
    }

    public Builder<T> description(String description) {
      this.description = description;
      return this;
    }

    public Builder<T> unit(Unit unit) {
      this.unit = unit;
      return this;
    }

    public Builder<T> minValue(T minValue) {
      this.minValue = minValue;
      return this;
    }

    public Builder<T> maxValue(T maxValue) {
      this.maxValue = maxValue;
      return this;
    }

    public Builder<T> minStep(T minStep) {
      this.minStep = minStep;
      return this;
    }

    public Builder<T> maxLength(Integer maxLength) {
      this.maxLength = maxLength;
      return this;
    }

    public Builder<T> maxDataLength(Integer maxDataLength) {
      this.maxDataLength = maxDataLength;
      return this;
    }

    public Builder<T> validValues(List<T> validValues) {
      this.validValues = validValues;
      return this;
    }

    public Builder<T> validValuesRange(List<T> validValuesRange) {
      this.validValuesRange = validValuesRange;
      return this;
    }

    public Characteristic<T> build() {
      // validation
      if (this.format == Format.BOOL
          && this.value != null && !(this.value instanceof Boolean)) {
        throw new IllegalArgumentException("Characteristic's Value type should be Boolean");
      } else if (
          (this.format == Format.UINT8 || this.format == Format.UINT16 || this.format == Format.INT)
              && this.value != null && !(this.value instanceof Integer)) {
        throw new IllegalArgumentException("Characteristic's Value type should be Integer");
      } else if ((this.format == Format.UINT32 || this.format == Format.UINT64)
          && this.value != null && !(this.value instanceof Long)) {
        throw new IllegalArgumentException("Characteristic's Value type should be Long");
      } else if (this.format == Format.FLOAT
          && this.value != null && !(this.value instanceof Double)) {
        throw new IllegalArgumentException("Characteristic's Value type should be Double");
      } else if (
          (this.format == Format.STRING || this.format == Format.TLV8 || this.format == Format.DATA)
              && this.value != null && !(this.value instanceof String)) {
        throw new IllegalArgumentException("Characteristic's Value type should be String");
      }
      if (this.type == null || this.instanceId == null || this.permissions == null
          || this.permissions.isEmpty() || this.format == null
          || (this.permissions.contains(PAIRED_READ) && this.value == null)) {
        throw new IllegalArgumentException(
            "Characteristic's Type InstanceId Value Permissions Format are required");
      }
      if (!this.permissions.contains(PAIRED_READ) && this.value != null) {
        throw new IllegalArgumentException(
            "Characteristic's Value should be null when Permissions don't contain paired read");
      }
      if (this.format == Format.BOOL || this.format == Format.STRING
          || this.format == Format.TLV8 || this.format == Format.DATA) {
        if (this.minValue != null || this.maxValue != null || this.minStep != null) {
          throw new IllegalArgumentException(
              "Characteristic's minValue maxValue minStep only applies to Number format");
        }
        if (this.validValues != null || this.validValuesRange != null) {
          throw new IllegalArgumentException(
              "Characteristic's validValues validValuesRange only applies to Number format");
        }
      }
      if (this.format != Format.STRING && this.maxLength != null) {
        throw new IllegalArgumentException(
            "Characteristic's maxLength only applies to String format");
      }
      if (this.format != Format.DATA && this.maxDataLength != null) {
        throw new IllegalArgumentException(
            "Characteristic's maxDataLength only applies to Data format");
      }

      return new Characteristic<>(this);
    }
  }
}
