CREATE TABLE audit_logs (
  id              BIGINT NOT NULL AUTO_INCREMENT,
  user_id         INT,           -- 
  subject         VARCHAR(100),  -- 用户名
  remote_host     VARCHAR(20),   -- 主机IP
  case_id         BIGINT,        -- 案件
  action          VARCHAR(100),  -- 操作
  params          text,          -- 操作对应的参数
       
  created_at      DATETIME,

  PRIMARY KEY (id),
  INDEX user_id_index (user_id),
  INDEX case_id_index (case_id),
  INDEX user_name_index (subject),
  INDEX created_at_index (created_at)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;