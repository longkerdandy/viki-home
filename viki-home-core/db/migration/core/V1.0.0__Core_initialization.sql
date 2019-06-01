/** Core **/

/** Thing **/
CREATE TABLE core_thing(
  id TEXT NOT NULL,
  _schema TEXT NOT NULL,
  heartbeat INTEGER NOT NULL,
  PRIMARY KEY (id ASC)
);

/** Property **/
CREATE TABLE core_property(
  thing TEXT NOT NULL,
  name TEXT NOT NULL,
  type TEXT NOT NULL,
  _value TEXT NOT NULL,
  updated_at INTEGER NOT NULL,
  PRIMARY KEY (thing, name ASC),
  FOREIGN KEY (thing) REFERENCES core_thing(id) ON DELETE CASCADE ON UPDATE NO ACTION
);