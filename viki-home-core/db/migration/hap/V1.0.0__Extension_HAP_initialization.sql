/** HomeKit Protocol Extension **/

/** Bridge Information **/
CREATE TABLE ext_hap_bridge(
  aid INTEGER NOT NULL,
  aid_counter INTEGER NOT NULL,
  config_num INTEGER NOT NULL,
  state_num INTEGER NOT NULL,
  protocol_version TEXT NOT NULL,
  status_flag INTEGER NOT NULL,
  category_id INTEGER NOT NULL,
  private_key BLOB,
  public_key BLOB,
  PRIMARY KEY (aid ASC)
);

/** Pairing **/
CREATE TABLE ext_hap_pairing(
  pairing_id TEXT NOT NULL,
  public_key BLOB NOT NULL,
  permissions INTEGER NOT NULL,
  PRIMARY KEY (pairing_id ASC)
);
CREATE INDEX idx_ext_hap_pairing_permissions
ON ext_hap_pairing (permissions);

/** Accessory **/
CREATE TABLE ext_hap_accessory(
  aid INTEGER NOT NULL,
  iid_counter INTEGER NOT NULL,
  PRIMARY KEY (aid ASC)
);

/** Service **/
CREATE TABLE ext_hap_service(
  aid INTEGER NOT NULL,
  sid INTEGER NOT NULL,
  type TEXT NOT NULL,
  is_hidden INTEGER,
  is_primary INTEGER,
  linked_services TEXT,
  PRIMARY KEY(aid, sid ASC)
);
CREATE UNIQUE INDEX idx_ext_hap_service_type
ON ext_hap_service (aid, type);

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
  PRIMARY KEY(aid, cid ASC),
  FOREIGN KEY(aid, sid) REFERENCES ext_hap_service(aid, sid) ON DELETE CASCADE ON UPDATE NO ACTION
);
CREATE INDEX idx_ext_hap_characteristic_service
ON ext_hap_characteristic (aid, sid);
CREATE UNIQUE INDEX idx_ext_hap_characteristic_type
ON ext_hap_characteristic (aid, type);