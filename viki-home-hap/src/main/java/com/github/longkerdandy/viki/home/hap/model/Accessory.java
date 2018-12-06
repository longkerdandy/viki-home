package com.github.longkerdandy.viki.home.hap.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * HomeKit Accessory Protocol Accessory Object
 */
public class Accessory {

  @JsonProperty("aid")
  private Long instanceId;
  private List<Service> services;

  private Accessory() {
  }

  public Accessory(Long instanceId, List<Service> services) {
    // validation
    if (instanceId == null) {
      throw new IllegalArgumentException("Accessory's InstanceId is required");
    }

    this.instanceId = instanceId;
    this.services = services;
  }

  public Long getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(Long instanceId) {
    this.instanceId = instanceId;
  }

  public List<Service> getServices() {
    return services;
  }

  public void setServices(List<Service> services) {
    this.services = services;
  }

  @Override
  public String toString() {
    return "Accessory{" +
        "instanceId=" + instanceId +
        ", services=" + services +
        '}';
  }
}
