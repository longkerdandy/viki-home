package com.github.longkerdandy.viki.home.hap.http.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Write request target for /characteristics
 */
public class CharacteristicWriteRequestTarget {

  @JsonProperty("aid")
  private Long accessoryId;
  @JsonProperty("iid")
  private Long instanceId;
  private Object value;
  @JsonProperty("ev")
  private Boolean enableEvent;
  @JsonProperty("authData")
  private String authorizationData;
  @JsonProperty("remote")
  private Boolean enableRemote;

  private CharacteristicWriteRequestTarget() {
  }

  public CharacteristicWriteRequestTarget(Long accessoryId, Long instanceId, Object value) {
    this.accessoryId = accessoryId;
    this.instanceId = instanceId;
    this.value = value;
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

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public Boolean getEnableEvent() {
    return enableEvent;
  }

  public void setEnableEvent(Boolean enableEvent) {
    this.enableEvent = enableEvent;
  }

  public String getAuthorizationData() {
    return authorizationData;
  }

  public void setAuthorizationData(String authorizationData) {
    this.authorizationData = authorizationData;
  }

  public Boolean getEnableRemote() {
    return enableRemote;
  }

  public void setEnableRemote(Boolean enableRemote) {
    this.enableRemote = enableRemote;
  }

  @Override
  public String toString() {
    return "CharacteristicWriteRequestTarget{" +
        "accessoryId=" + accessoryId +
        ", instanceId=" + instanceId +
        ", value=" + value +
        ", enableEvent=" + enableEvent +
        ", authorizationData='" + authorizationData + '\'' +
        ", enableRemote=" + enableRemote +
        '}';
  }
}
