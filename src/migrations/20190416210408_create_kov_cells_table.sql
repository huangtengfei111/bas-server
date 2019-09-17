CREATE TABLE kov_cells (
  id              BIGINT NOT NULL AUTO_INCREMENT,
  data_source     VARCHAR(10), 
  radio_type      VARCHAR(50), 
  mcc             VARCHAR(10), 
  mnc             VARCHAR(10), 
  lac             INT, 
  ci              INT, 
  lat             DECIMAL(11, 8),
  lon             DECIMAL(11, 8),
  rag             INT,   -- range

  created_at      INT, 
  updated_at      INT,

  PRIMARY KEY (id),
  INDEX mcc_index (mcc),
  INDEX mnc_index (mnc),
  INDEX lac_index (lac),
  INDEX ci_index  (ci)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;