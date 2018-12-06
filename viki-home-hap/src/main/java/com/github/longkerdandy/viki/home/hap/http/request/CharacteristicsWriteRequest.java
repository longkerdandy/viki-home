package com.github.longkerdandy.viki.home.hap.http.request;

import java.util.List;

/**
 * Write request for /characteristics
 */
public class CharacteristicsWriteRequest {

  private List<CharacteristicWriteRequestTarget> characteristics;

  private CharacteristicsWriteRequest() {
  }

  public CharacteristicsWriteRequest(List<CharacteristicWriteRequestTarget> characteristics) {
    this.characteristics = characteristics;
  }

  public List<CharacteristicWriteRequestTarget> getCharacteristics() {
    return characteristics;
  }

  public void setCharacteristics(List<CharacteristicWriteRequestTarget> characteristics) {
    this.characteristics = characteristics;
  }
}
