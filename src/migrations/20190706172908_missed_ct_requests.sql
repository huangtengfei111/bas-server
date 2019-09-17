CREATE TABLE missed_ct_requests (
  id              INT NOT NULL AUTO_INCREMENT,
  app_id          VARCHAR(255),           -- 请求接口的app-id
  code            VARCHAR(255),            -- 请求的基站编码
  err_code        VARCHAR(100),   
  err_msg         VARCHAR(255),
  created_at      DATETIME,

  PRIMARY KEY (id),
  INDEX app_id_index (app_id),
  INDEX code_index (code)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;