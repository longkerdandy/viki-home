package com.github.longkerdandy.viki.home.hap.http.response;

import java.util.List;

/**
 * Read response for /characteristics
 */
public class CharacteristicsReadResponse {

  private List<CharacteristicReadResponseTarget> characteristics;

  private CharacteristicsReadResponse() {
  }

  public CharacteristicsReadResponse(List<CharacteristicReadResponseTarget> characteristics) {
    this.characteristics = characteristics;
  }

  public List<CharacteristicReadResponseTarget> getCharacteristics() {
    return characteristics;
  }

  public void setCharacteristics(
      List<CharacteristicReadResponseTarget> characteristics) {
    this.characteristics = characteristics;
  }
}
