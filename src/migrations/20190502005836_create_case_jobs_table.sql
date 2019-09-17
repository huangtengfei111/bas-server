CREATE TABLE case_jobs (
  id              BIGINT NOT NULL AUTO_INCREMENT,
  jid             VARCHAR(200),   
  jtype           VARCHAR(50),
  case_id         INT NOT NULL,
  
  executed_at     DATETIME,   -- 执行开始时间
  ended_at        DATETIME,   -- 结束时间
  launched_by     INT,  
  
  created_at      DATETIME,   -- 创建时间
  updated_at      DATETIME,   -- 更新时间


  PRIMARY KEY (id),
  UNIQUE INDEX case_jtype_index (case_id, jtype),
  INDEX case_id_index (case_id),
  INDEX jid_index (jid)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;