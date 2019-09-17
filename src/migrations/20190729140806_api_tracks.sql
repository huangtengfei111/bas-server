CREATE TABLE api_tracks (
  id               INT NOT NULL AUTO_INCREMENT,
  system_id        VARCHAR(255),
  host_id          VARCHAR(255),            
  controller       VARCHAR(255),
  action           VARCHAR(255),
  
  created_at       DATETIME,
  updated_at       DATETIME,
  
  PRIMARY KEY (id),
  INDEX system_id_index (system_id),
  INDEX host_id_index (host_id),
  INDEX controller_index (controller),
  INDEX created_at_index (created_at)
) 
ENGINE=MYISAM
DEFAULT CHARSET=utf8;