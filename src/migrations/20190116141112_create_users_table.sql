CREATE TABLE users (
  id           INT(11) NOT NULL AUTO_INCREMENT,
  name         VARCHAR(128),   -- 姓名 [必填]
  avatar       VARCHAR(255),   -- 用户头像
  memo         VARCHAR(255),   -- 备注
  
  last_login_at    DATETIME,     -- 最近登录IP地址
  last_remote_host VARCHAR(100), -- 最近登录时间 
  created_at       DATETIME,
  updated_at       DATETIME,

  PRIMARY KEY (id),
  INDEX name_index (name)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;