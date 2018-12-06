package com.github.longkerdandy.viki.home.hap.http.response;

import java.util.List;

/**
 * Write response for /characteristics
 */
public class CharacteristicsWriteResponse {

  private List<CharacteristicWriteResponseTarget> characteristics;

  private CharacteristicsWriteResponse() {
  }

  public CharacteristicsWriteResponse(List<CharacteristicWriteResponseTarget> characteristics) {
    this.characteristics = characteristics;
  }

  public List<CharacteristicWriteResponseTarget> getCharacteristics() {
    return characteristics;
  }

  public void setCharacteristics(List<CharacteristicWriteResponseTarget> characteristics) {
    this.characteristics = characteristics;
  }
}
