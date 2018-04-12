package com.github.longkerdandy.viki.home.hap.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.longkerdandy.viki.home.hap.model.property.Type;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

/**
 * HAP Service Object
 */
public class Service {

  private UUID type;
  @JsonProperty("iid")
  private Long instanceId;
  private List<Characteristic> characteristics;
  @JsonProperty("hidden")
  private Boolean isHidden;
  @JsonProperty("primary")
  private Boolean isPrimary;
  @JsonProperty("linked")
  private List<Long> linkedServiceIds;

  private Service() {
  }

  private Service(Builder builder) {
    this.type = builder.type;
    this.instanceId = builder.instanceId;
    this.characteristics = builder.characteristics;
    this.isHidden = builder.isHidden;
    this.isPrimary = builder.isPrimary;
    this.linkedServiceIds = builder.linkedServiceIds;
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

  public List<Characteristic> getCharacteristics() {
    return characteristics;
  }

  public void setCharacteristics(
      List<Characteristic> characteristics) {
    this.characteristics = characteristics;
  }

  public Boolean getHidden() {
    return isHidden;
  }

  public void setHidden(Boolean hidden) {
    isHidden = hidden;
  }

  public Boolean getPrimary() {
    return isPrimary;
  }

  public void setPrimary(Boolean primary) {
    isPrimary = primary;
  }

  public List<Long> getLinkedServiceIds() {
    return linkedServiceIds;
  }

  public void setLinkedServiceIds(List<Long> linkedServiceIds) {
    this.linkedServiceIds = linkedServiceIds;
  }

  public static class Builder {

    private UUID type;
    private Long instanceId;
    private List<Characteristic> characteristics;
    private Boolean isHidden;
    private Boolean isPrimary;
    private List<Long> linkedServiceIds;

    public Builder(UUID type, Long instanceId, List<Characteristic> characteristics) {
      this.type = type;
      this.instanceId = instanceId;
      this.characteristics = characteristics;
    }

    public Builder isHidden(Boolean isHidden) {
      this.isHidden = isHidden;
      return this;
    }

    public Builder isPrimary(Boolean isHidden) {
      this.isPrimary = isPrimary;
      return this;
    }

    public Builder linkedServiceIds(List<Long> linkedServiceIds) {
      this.linkedServiceIds = linkedServiceIds;
      return this;
    }

    public Service build() {
      // validation
      if (this.type == null || this.instanceId == null
          || this.characteristics == null || this.characteristics.isEmpty()) {
        throw new IllegalArgumentException(
            "Service's Type InstanceId Characteristics are required");
      }

      return new Service(this);
    }
  }
}
