CREATE TABLE uploads (
  id                INT NOT NULL AUTO_INCREMENT,
  case_id           INT,
  client_uuid       INT NOT NULL,
  filename          INT NOT NULL,

  created_at        DATETIME,
  updated_at        DATETIME,

  PRIMARY KEY (id),
  INDEX case_id_index (case_id),
  INDEX client_uuid_index (client_uuid),
  INDEX created_at_index (created_at)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;