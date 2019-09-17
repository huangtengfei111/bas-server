CREATE TABLE favorite_menus (
  id              INT NOT NULL AUTO_INCREMENT,
  user_id         INT,
  mkey            VARCHAR(20),   -- 菜单键 [必填]       
  
  created_at      DATETIME,
  updated_at      DATETIME,


  PRIMARY KEY (id),
  UNIQUE INDEX user_mkey_index (user_id, mkey),
  INDEX user_id_index (user_id)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;