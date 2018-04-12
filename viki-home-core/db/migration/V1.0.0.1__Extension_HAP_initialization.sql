/** HomeKit Protocol Extension **/

/** Bridge Information **/
CREATE TABLE ext_hap_bridge(
  aid INTEGER NOT NULL PRIMARY KEY ASC,
  aid_counter INTEGER NOT NULL,
  config_num INTEGER NOT NULL,
  state_num INTEGER NOT NULL,
  protocol_version TEXT NOT NULL,
  status_flag INTEGER NOT NULL,
  category_id INTEGER NOT NULL
);
-- Bridge
INSERT INTO ext_hap_bridge (aid, aid_counter, config_num, protocol_version, state_num, status_flag, category_id)
VALUES (1, 1, 1, '1.0', 1, 1, 2);

/** Accessory **/
CREATE TABLE ext_hap_accessory(
  aid INTEGER NOT NULL PRIMARY KEY ASC,
  device_id TEXT NOT NULL,
  iid_counter INTEGER NOT NULL
);
-- Bridge Accessory
INSERT INTO ext_hap_accessory (aid, device_id, iid_counter) VALUES (1, '-L8ov6yMnBE0j3nyWnwm', 7);

/** Service **/
CREATE TABLE ext_hap_service(
  aid INTEGER NOT NULL,
  sid INTEGER NOT NULL,
  type TEXT NOT NULL,
  is_hidden INTEGER,
  is_primary INTEGER,
  linked_services TEXT,
  PRIMARY KEY(aid, sid)
);
CREATE UNIQUE INDEX idx_ext_hap_service_type
ON ext_hap_service (aid, type);
-- Accessory Information Service
INSERT INTO ext_hap_service (aid, sid, type)
VALUES (1, 1, '0000003E-0000-1000-8000-0026BB765291');

/** Characteristic **/
CREATE TABLE ext_hap_characteristic(
  aid INTEGER NOT NULL,
  cid INTEGER NOT NULL,
  sid INTEGER NOT NULL,
  type TEXT NOT NULL,
  _value TEXT,
  permissions TEXT NOT NULL,
  enable_event INTEGER,
  description TEXT,
  format TEXT NOT NULL,
  unit TEXT,
  min_value REAL,
  max_value REAL,
  min_step REAL,
  max_length INTEGER,
  max_data_length INTEGER,
  valid_values TEXT,
  valid_values_range TEXT,
  PRIMARY KEY(aid, cid),
  FOREIGN KEY(aid, sid) REFERENCES ext_hap_service(aid, sid)
);
CREATE UNIQUE INDEX idx_ext_hap_characteristic_type
ON ext_hap_characteristic (aid, type);
-- Identify
INSERT INTO ext_hap_characteristic (aid, cid, sid, type, _value, permissions, format)
VALUES (1, 2, 1, '00000014-0000-1000-8000-0026BB765291', null, 'pw', 'bool');
-- Manufacturer
INSERT INTO ext_hap_characteristic (aid, cid, sid, type, _value, permissions, format)
VALUES (1, 3, 1, '00000020-0000-1000-8000-0026BB765291', 'LongkerDandy', 'pr', 'string');
-- Model
INSERT INTO ext_hap_characteristic (aid, cid, sid, type, _value, permissions, format, max_length)
VALUES (1, 4, 1, '00000021-0000-1000-8000-0026BB765291', 'VIKI Home Open Source Project', 'pr', 'string', 64);
-- Name
INSERT INTO ext_hap_characteristic (aid, cid, sid, type, _value, permissions, format, max_length)
VALUES (1, 5, 1, '00000023-0000-1000-8000-0026BB765291', 'VIKI Home Bridge', 'pr', 'string', 64);
-- Serial Number
INSERT INTO ext_hap_characteristic (aid, cid, sid, type, _value, permissions, format, max_length)
VALUES (1, 6, 1, '00000030-0000-1000-8000-0026BB765291', '-L8ov6yMnBE0j3nyWnwm', 'pr', 'string', 64);
-- Firmware Revision
INSERT INTO ext_hap_characteristic (aid, cid, sid, type, _value, permissions, format)
VALUES (1, 7, 1, '00000052-0000-1000-8000-0026BB765291', '1.0.0', 'pr', 'string');