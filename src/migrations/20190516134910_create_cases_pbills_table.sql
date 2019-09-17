CREATE TABLE cases_pbills (
  id              INT NOT NULL AUTO_INCREMENT,
  case_id         INT,
  pbill_id        INT,            
  
  created_at      DATETIME,
  updated_at      DATETIME,

  PRIMARY KEY (id),
  UNIQUE INDEX case_pbill_index (case_id, pbill_id),
  INDEX case_id_index (case_id),
  INDEX pbill_id_index (pbill_id)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;