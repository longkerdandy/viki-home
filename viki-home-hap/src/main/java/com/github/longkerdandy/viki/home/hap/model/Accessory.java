package com.github.longkerdandy.viki.home.hap.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * HAP Accessory Object
 */
public class Accessory {

  @JsonProperty("iid")
  private Long instanceId;
  private List<Service> services;

  private Accessory() {
  }

  private Accessory(Long instanceId, List<Service> services) {
    this.instanceId = instanceId;
    this.services = services;

    // validation
    if (this.instanceId == null || this.services == null || this.services.isEmpty()) {
      throw new IllegalArgumentException("Accessory's InstanceId Services are required");
    }
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
}
