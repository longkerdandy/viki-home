package com.github.longkerdandy.viki.home.mi.schema;

import com.github.longkerdandy.viki.home.schema.ThingSchema;
import java.io.IOException;
import java.util.Locale;
import org.junit.BeforeClass;
import org.junit.Test;

public class SchemaRepositoryTest {

  @BeforeClass
  public static void init() {
    System.setProperty("java.util.PropertyResourceBundle.encoding", "UTF-8");
  }

  @Test
  public void loadTest() throws IOException {
    SchemaRepository schemas = new SchemaRepository(Locale.CHINESE, "src/test/schema/",
        "src/test/i18n/");
    schemas.load();
    ThingSchema schema = schemas.getSchemaByName("aqara:zigbee:1_button_wall_switch");
    assert schema != null;
    assert schema.getPropertyByName("model").isPresent();
    assert schema.getPropertyByName("status").isPresent();
    assert schema.getResources().getString("property.status.label").equals("状态");
  }
}
