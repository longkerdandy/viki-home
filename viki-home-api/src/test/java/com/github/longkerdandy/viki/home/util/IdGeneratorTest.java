package com.github.longkerdandy.viki.home.util;

import java.util.Random;
import org.junit.Test;

public class IdGeneratorTest {

  @Test
  public void idTest() {
    IdGenerator generator = new IdGenerator(new Random());
    String id = generator.nextId();
    assert id != null;
    assert id.length() == 20;
  }
}
