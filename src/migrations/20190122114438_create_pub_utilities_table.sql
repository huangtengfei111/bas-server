-- 公共事业
CREATE TABLE pub_utilities (
  id           BIGINT NOT NULL AUTO_INCREMENT,
  social_no    VARCHAR(128) NOT NULL,
  home_addr    VARCHAR(128),
  home_phone   VARCHAR(50),
  mobile       VARCHAR(128),
  company      VARCHAR(128),
  position     VARCHAR(50),
  office_addr  VARCHAR(128),
  office_phone VARCHAR(50),

  version      VARCHAR(50), -- 版本, 目前主要用数据编制时间
  data_source  VARCHAR(10), -- 公共事业: 水电煤 公安
  trust_level  INT,         -- 可信度/权威度 1: 完全可信  - 10: 参考价值
  created_at   DATETIME,
  updated_at   DATETIME,

  PRIMARY KEY (id),
  UNIQUE INDEX social_no_index (social_no),
  INDEX mobile_index (mobile)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;