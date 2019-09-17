CREATE TABLE settings (
  id           INT(11) NOT NULL AUTO_INCREMENT,
  account_id   INT NOT NULL DEFAULT 0,  -- 0: 全局设置，其他对应相关账号  
  k            VARCHAR(100) NOT NULL,   -- [必填]
  v            VARCHAR(100),
  memo         VARCHAR(255),
  
  created_at   DATETIME,
  updated_at   DATETIME,

  PRIMARY KEY (id),
  INDEX account_id_index (account_id),
  UNIQUE INDEX account_k_index (account_id, k)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;