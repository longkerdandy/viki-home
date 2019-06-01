/** XiaoMi Smart Home Protocol Extension **/

/** Gateway Information **/
CREATE TABLE ext_mi_gateway(
  gid TEXT NOT NULL,
  model TEXT NOT NULL,
  protocol_version TEXT NOT NULL,
  password TEXT,
  token TEXT NOT NULL,
  address TEXT NOT NULL,
  port INTEGER NOT NULL,
  PRIMARY KEY (gid ASC)
);

/** Zigbee Thing Information **/
CREATE TABLE ext_mi_zigbee_thing(
  sid TEXT NOT NULL,
  gid TEXT NOT NULL,
  tid TEXT NOT NULL UNIQUE,
  model TEXT NOT NULL,
  short_id TEXT,
  PRIMARY KEY (sid ASC),
  FOREIGN KEY (gid) REFERENCES ext_mi_gateway(gid) ON DELETE CASCADE ON UPDATE NO ACTION
);