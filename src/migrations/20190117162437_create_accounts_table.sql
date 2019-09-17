CREATE TABLE accounts (
  id           INT(11) NOT NULL AUTO_INCREMENT,
  username     VARCHAR(128),   -- 登录账号     [必填]
  password     VARCHAR(128),   -- 密码(已加密) 
  salt         VARCHAR(128),   -- 随机数       
  built_in      INT  DEFAULT 0, -- 是否为内置账号(内置账号不可删除)
  
  user_id      INT,            -- 用户
  role_id      INT,            -- 角色
  
  created_at   DATETIME,
  updated_at   DATETIME,
  deleted_at   DATETIME,

  PRIMARY KEY (id),
  UNIQUE INDEX username_index (username),
  INDEX user_id_index (user_id),
  INDEX role_id_index (role_id),
  INDEX deleted_at_index (deleted_at)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;