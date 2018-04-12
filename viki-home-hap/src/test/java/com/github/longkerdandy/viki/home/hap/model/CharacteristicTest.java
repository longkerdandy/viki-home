package com.github.longkerdandy.viki.home.hap.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.longkerdandy.viki.home.hap.model.property.Format;
import com.github.longkerdandy.viki.home.hap.model.property.Permission;
import com.github.longkerdandy.viki.home.util.Jacksons;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class CharacteristicTest {

  @Test
  public void serializationTest() throws IOException {
    Characteristic<String> c =
        new Characteristic.Builder<>("00000023-0000-1000-8000-0026BB765291", 2L,
            "Acme Light Bridge",
            List.of(Permission.PAIRED_READ), Format.STRING).build();
    String json = Jacksons.getWriter().writeValueAsString(c);
    assert json != null;
    assert json.contains("type") && json.contains("\"23\"");
    assert json.contains("iid");
    assert json.contains("value");
    assert json.contains("perms") && json.contains("pr");
    assert json.contains("format") && json.contains("string");
    Characteristic<String> r = Jacksons.getReader(new TypeReference<Characteristic<String>>() {
    }).readValue(json);
    assert r != null;
    assert r.getType().toString().equalsIgnoreCase("00000023-0000-1000-8000-0026BB765291");
    assert r.getShortType().equalsIgnoreCase("23");
    assert r.getInstanceId() == 2L;
    assert r.getValue().equals("Acme Light Bridge");
    assert r.getPermissions().size() == 1 && r.getPermissions().contains(Permission.PAIRED_READ);
    assert r.getFormat() == Format.STRING;
  }

  @Test
  public void serializationWithNullTest() throws IOException {
    Characteristic<String> c =
        new Characteristic.Builder<String>("79fce53f-22c8-489b-9b3d-b17f909957f6", 2L, null,
            List.of(Permission.PAIRED_WRITE), Format.INT).validValues(new ArrayList<>()).build();
    String json = Jacksons.getWriter().writeValueAsString(c);
    assert json != null;
    assert json.contains("type") && json
        .contains("79fce53f-22c8-489b-9b3d-b17f909957f6".toUpperCase());
    assert json.contains("iid");
    assert json.contains("value") && json.contains("null");
    assert json.contains("perms") && json.contains("pw");
    assert json.contains("format") && json.contains("int");
    assert !json.contains("valid-values");
    Characteristic<String> r = Jacksons.getReader(new TypeReference<Characteristic<String>>() {
    }).readValue(json);
    assert r != null;
    assert r.getType().toString().equalsIgnoreCase("79fce53f-22c8-489b-9b3d-b17f909957f6");
    assert r.getShortType().equalsIgnoreCase("79fce53f-22c8-489b-9b3d-b17f909957f6");
    assert r.getInstanceId() == 2L;
    assert r.getValue() == null;
    assert r.getPermissions().size() == 1 && r.getPermissions().contains(Permission.PAIRED_WRITE);
    assert r.getFormat() == Format.INT;
    assert r.getValidValues() == null;
  }
}
