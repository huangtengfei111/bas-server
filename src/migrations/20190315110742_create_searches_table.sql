CREATE TABLE searches (
  id                INT NOT NULL AUTO_INCREMENT,
  account_id        INT NOT NULL,
  case_id           INT NOT NULL,
  name              VARCHAR(200),   -- 名称  [必填]
  subject           VARCHAR(50),    -- 搜索对象
  value             TEXT,           -- 搜索内容

  created_at        DATETIME,
  updated_at        DATETIME,

  PRIMARY KEY (id),
  INDEX case_id_index (case_id),
  INDEX account_id_index (account_id),
  INDEX name_index (name)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;