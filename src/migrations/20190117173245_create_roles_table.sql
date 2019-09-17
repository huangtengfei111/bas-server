CREATE TABLE roles (
  id           INT(11) NOT NULL AUTO_INCREMENT,
  name         VARCHAR(100) NOT NULL,   -- 角色名称 [必填]
  value        VARCHAR(20) NOT NULL,   -- 角色系统值 [必填]
  memo         VARCHAR(100),           -- 备注

  created_at   DATETIME,
  updated_at   DATETIME,

  PRIMARY KEY (id),
  UNIQUE INDEX value_index (value),
  INDEX name_index (name)

) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;