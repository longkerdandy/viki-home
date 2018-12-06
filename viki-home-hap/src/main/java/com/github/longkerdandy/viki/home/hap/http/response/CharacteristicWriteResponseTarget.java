package com.github.longkerdandy.viki.home.hap.http.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Write response target for /characteristics
 */
public class CharacteristicWriteResponseTarget {

  @JsonProperty("aid")
  private Long accessoryId;
  @JsonProperty("iid")
  private Long instanceId;
  private Status status;

  private CharacteristicWriteResponseTarget() {
  }

  public CharacteristicWriteResponseTarget(Long accessoryId, Long instanceId,
      Status status) {
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

  public Long getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(Long instanceId) {
    this.instanceId = instanceId;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "CharacteristicWriteResponseTarget{" +
        "accessoryId=" + accessoryId +
        ", instanceId=" + instanceId +
        ", status=" + status +
        '}';
  }
}
