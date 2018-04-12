package com.github.longkerdandy.viki.home.util;

import java.util.Random;
import org.junit.Test;

public class IdsTest {

  @Test
  public void idTest() {
    Ids generator = new Ids(new Random());
    String id = generator.nextId();
    assert id != null;
    assert id.length() == 20;
  }
}
