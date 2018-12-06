package com.github.longkerdandy.viki.home.hap.http.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.longkerdandy.viki.home.hap.model.property.Format;
import com.github.longkerdandy.viki.home.hap.model.property.Permission;
import com.github.longkerdandy.viki.home.hap.model.property.Type;
import com.github.longkerdandy.viki.home.hap.model.property.Unit;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

public class CharacteristicReadResponseTarget {

  @JsonProperty("aid")
  private Long accessoryId;
  private UUID type;
  @JsonProperty("iid")
  private Long instanceId;
  private Object value;
  @JsonProperty("perms")
  private List<Permission> permissions;
  @JsonProperty("ev")
  private Boolean enableEvent;
  private Format format;
  private Unit unit;
  private Object minValue;
  private Object maxValue;
  private Object minStep;
  @JsonProperty("maxLen")
  private Integer maxLength;
  @JsonProperty("maxDataLen")
  private Integer maxDataLength;
  private Status status;

  private CharacteristicReadResponseTarget() {
  }

  public CharacteristicReadResponseTarget(Long accessoryId, Long instanceId, Object value) {
    this.accessoryId = accessoryId;
    this.instanceId = instanceId;
    this.value = value;
    this.status = Status.SUCCESS;
  }

  public CharacteristicReadResponseTarget(Long accessoryId, Long instanceId, Status status) {
    this.accessoryId = accessoryId;
    this.instanceId = instanceId;
    this.status = status;
  }

  public Long getAccessoryId() {
    return accessoryId;
  }

  public void setAccessoryId(Long accessoryId) {
    this.accessoryId = accessoryId;
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

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public List<Permission> getPermissions() {
    return permissions;
  }

  public void setPermissions(
      List<Permission> permissions) {
    this.permissions = permissions;
  }

  public Boolean getEnableEvent() {
    return enableEvent;
  }

  public void setEnableEvent(Boolean enableEvent) {
    this.enableEvent = enableEvent;
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

  public Object getMinValue() {
    return minValue;
  }

  public void setMinValue(Object minValue) {
    this.minValue = minValue;
  }

  public Object getMaxValue() {
    return maxValue;
  }

  public void setMaxValue(Object maxValue) {
    this.maxValue = maxValue;
  }

  public Object getMinStep() {
    return minStep;
  }

  public void setMinStep(Object minStep) {
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

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "CharacteristicReadResponseTarget{" +
        "accessoryId=" + accessoryId +
        ", type=" + type +
        ", instanceId=" + instanceId +
        ", value=" + value +
        ", permissions=" + permissions +
        ", enableEvent=" + enableEvent +
        ", format=" + format +
        ", unit=" + unit +
        ", minValue=" + minValue +
        ", maxValue=" + maxValue +
        ", minStep=" + minStep +
        ", maxLength=" + maxLength +
        ", maxDataLength=" + maxDataLength +
        ", status=" + status +
        '}';
  }
}
