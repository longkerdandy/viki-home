package com.github.longkerdandy.viki.home.hap.storage;

import com.github.longkerdandy.viki.home.hap.model.Characteristic;
import com.github.longkerdandy.viki.home.hap.model.property.Format;
import com.github.longkerdandy.viki.home.hap.model.property.Permission;
import com.github.longkerdandy.viki.home.hap.model.property.Unit;
import com.github.longkerdandy.viki.home.storage.sqlite.SQLiteStorage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.configuration2.MapConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

public class HAPStorageTest {

  private static HAPStorage hapStorage;

  @BeforeClass
  public static void init() throws IOException {
    String path = File.createTempFile("viki_home_", "db").getAbsolutePath();
    SQLiteStorage storage = new SQLiteStorage(new MapConfiguration(
        Map.of("storage.jdbc.url", "jdbc:sqlite:" + path,
            "storage.sqlite.pragma.foreign_keys", "true")));
    hapStorage = new HAPStorage(storage.getJdbi());

    storage.getJdbi().useHandle(handle -> {
      handle.execute("DROP TABLE IF EXISTS ext_hap_bridge");
      handle.execute("CREATE TABLE ext_hap_bridge(\n"
          + "  aid INTEGER NOT NULL PRIMARY KEY ASC,\n"
          + "  aid_counter INTEGER NOT NULL,\n"
          + "  config_num INTEGER NOT NULL,\n"
          + "  protocol_version TEXT NOT NULL,\n"
          + "  state_num INTEGER NOT NULL,\n"
          + "  status_flag INTEGER NOT NULL,\n"
          + "  category_id INTEGER NOT NULL\n"
          + ")");
      handle.execute(
          "INSERT INTO ext_hap_bridge (aid, aid_counter, config_num, protocol_version, state_num, status_flag, category_id)\n"
              + "VALUES (1, 1, 1, '1.0', 1, 1, 2)");
    });

    storage.getJdbi().useHandle(handle -> {
      handle.execute("DROP TABLE IF EXISTS ext_hap_service");
      handle.execute("DROP INDEX IF EXISTS idx_ext_hap_service_type");
      handle.execute("CREATE TABLE ext_hap_service(\n"
          + "  aid INTEGER NOT NULL,\n"
          + "  sid INTEGER NOT NULL,\n"
          + "  type TEXT NOT NULL,\n"
          + "  is_hidden INTEGER,\n"
          + "  is_primary INTEGER,\n"
          + "  linked_services TEXT,\n"
          + "  PRIMARY KEY(aid, sid)\n"
          + ")");
      handle.execute("CREATE UNIQUE INDEX idx_ext_hap_service_type\n"
          + "ON ext_hap_service (aid, type)");
      handle.execute("INSERT INTO ext_hap_service (aid, sid, type)\n"
          + "VALUES (1, 1, '0000003E-0000-1000-8000-0026BB765291')");
    });

    storage.getJdbi().useHandle(handle -> {
      handle.execute("DROP TABLE IF EXISTS ext_hap_characteristic");
      handle.execute("DROP INDEX IF EXISTS idx_ext_hap_characteristic_type");
      handle.execute("CREATE TABLE ext_hap_characteristic(\n"
          + "  aid INTEGER NOT NULL,\n"
          + "  cid INTEGER NOT NULL,\n"
          + "  sid INTEGER NOT NULL,\n"
          + "  type TEXT NOT NULL,\n"
          + "  _value TEXT,\n"
          + "  permissions TEXT NOT NULL,\n"
          + "  enable_event INTEGER,\n"
          + "  description TEXT,\n"
          + "  format TEXT NOT NULL,\n"
          + "  unit TEXT,\n"
          + "  min_value REAL,\n"
          + "  max_value REAL,\n"
          + "  min_step REAL,\n"
          + "  max_length INTEGER,\n"
          + "  max_data_length INTEGER,\n"
          + "  valid_values TEXT,\n"
          + "  valid_values_range TEXT,\n"
          + "  PRIMARY KEY(aid, cid),\n"
          + "  FOREIGN KEY(aid, sid) REFERENCES ext_hap_service(aid, sid)\n"
          + ")");
      handle.execute("CREATE UNIQUE INDEX idx_ext_hap_characteristic_type\n"
          + "ON ext_hap_characteristic (aid, type)");
      handle.execute(
          "INSERT INTO ext_hap_characteristic (aid, cid, sid, type, _value, permissions, format)\n"
              + "VALUES (1, 2, 1, '00000014-0000-1000-8000-0026BB765291', null, 'pw', 'bool')");
      handle.execute(
          "INSERT INTO ext_hap_characteristic (aid, cid, sid, type, _value, permissions, format, max_length)\n"
              + "VALUES (1, 3, 1, '00000021-0000-1000-8000-0026BB765291', 'V.I.K.I Home Open Source Project', 'pr', 'string', 64)");
      handle.execute(
          "INSERT INTO ext_hap_characteristic (aid, cid, sid, type, _value, permissions, format, unit, min_value, max_value, min_step)\n"
              + "VALUES (1, 4, 1, '00000035-0000-1000-8000-0026BB765291', '23.6', 'pr,pw,ev', 'float', 'celsius', 10.0, 38.0, 0.1)");
    });
  }

  @Test
  public void getBridgeInformationTest() {
    Map<String, ?> r = hapStorage.getBridgeInformation();
    assert r != null;
    assert (Integer) r.get("aid") == 1;
    assert (Integer) r.get("config_num") == 1;
    assert r.get("protocol_version").equals("1.0");
    assert (Integer) r.get("state_num") == 1;
    assert (Integer) r.get("status_flag") == 1;
    assert (Integer) r.get("category_id") == 2;
  }

  @Test
  public void nextAccessoryIdTest() {
    assert hapStorage.nextAccessoryId() == 2;
    assert hapStorage.nextAccessoryId() == 3;
    assert hapStorage.nextAccessoryId() == 4;
    assert hapStorage.nextAccessoryId() == 5;
  }

  @Test
  public void getCharacteristicByTypeTest() {
    Characteristic c = hapStorage.getCharacteristicByType(1, "00000014-0000-1000-8000-0026BB765291");
    assert c != null;
    assert c.getInstanceId() == 2;
    assert c.getType().toString().equalsIgnoreCase("00000014-0000-1000-8000-0026BB765291");
    assert c.getValue() == null;
    assert c.getPermissions().size() == 1;
    assert c.getPermissions().contains(Permission.PAIRED_WRITE);
    assert c.getEnableEvent() == null;
    assert c.getDescription() == null;
    assert c.getFormat() == Format.BOOL;
    assert c.getUnit() == null;
    assert c.getMinValue() == null;
    assert c.getMaxValue() == null;
    assert c.getMinStep() == null;
    assert c.getMaxLength() == null;
    assert c.getMaxDataLength() == null;
    assert c.getValidValues() == null;
    assert c.getValidValuesRange() == null;

    c = hapStorage.getCharacteristicByType(1, "00000021-0000-1000-8000-0026BB765291");
    assert c != null;
    assert c.getInstanceId() == 3;
    assert c.getType().toString().equalsIgnoreCase("00000021-0000-1000-8000-0026BB765291");
    assert c.getValue().equals("V.I.K.I Home Open Source Project");
    assert c.getPermissions().size() == 1;
    assert c.getPermissions().contains(Permission.PAIRED_READ);
    assert c.getEnableEvent() == null;
    assert c.getDescription() == null;
    assert c.getFormat() == Format.STRING;
    assert c.getUnit() == null;
    assert c.getMinValue() == null;
    assert c.getMaxValue() == null;
    assert c.getMinStep() == null;
    assert c.getMaxLength() == 64;
    assert c.getMaxDataLength() == null;
    assert c.getValidValues() == null;
    assert c.getValidValuesRange() == null;

    c = hapStorage.getCharacteristicByType(1, "00000035-0000-1000-8000-0026BB765291");
    assert c != null;
    assert c.getInstanceId() == 4;
    assert c.getType().toString().equalsIgnoreCase("00000035-0000-1000-8000-0026BB765291");
    assert (Double) c.getValue() == 23.6;
    assert c.getPermissions().size() == 3;
    assert c.getPermissions().contains(Permission.PAIRED_READ);
    assert c.getPermissions().contains(Permission.PAIRED_WRITE);
    assert c.getPermissions().contains(Permission.NOTIFY);
    assert c.getEnableEvent() == null;
    assert c.getDescription() == null;
    assert c.getFormat() == Format.FLOAT;
    assert c.getUnit() == Unit.CELSIUS;
    assert (Double) c.getMinValue() == 10.0;
    assert (Double) c.getMaxValue() == 38.0;
    assert (Double) c.getMinStep() == 0.1;
    assert c.getMaxLength() == null;
    assert c.getMaxDataLength() == null;
    assert c.getValidValues() == null;
    assert c.getValidValuesRange() == null;
  }
}
