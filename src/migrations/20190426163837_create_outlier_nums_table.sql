CREATE TABLE outlier_nums (
  id              BIGINT NOT NULL AUTO_INCREMENT,
  pbill_id        INT,
  num             VARCHAR(20),   -- [必填]
  flaw_type       INT,           -- [必填]

  created_at      DATETIME,
 
  PRIMARY KEY (id),
  INDEX pbill_id_index (pbill_id),
  INDEX num_index (num),
  UNIQUE INDEX pbill_id_num_index (pbill_id, num, flaw_type)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;