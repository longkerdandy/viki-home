package com.github.longkerdandy.viki.home.hap.http.response;

import com.github.longkerdandy.viki.home.hap.model.Accessory;
import java.util.List;

/**
 * Response for /accessories
 */
public class AccessoriesResponse {

  private List<Accessory> accessories;

  private AccessoriesResponse() {
  }

  public AccessoriesResponse(List<Accessory> accessories) {
    this.accessories = accessories;
  }

  public List<Accessory> getAccessories() {
    return accessories;
  }

  public void setAccessories(
      List<Accessory> accessories) {
    this.accessories = accessories;
  }
}
