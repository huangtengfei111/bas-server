CREATE TABLE ct_labels (
  id              BIGINT NOT NULL AUTO_INCREMENT,
  case_id         INT,
  ct_code         VARCHAR(20),   -- 基站编码 (LAC:CI:MNC) [必填] 可重复

  cp_lat          DECIMAL(14, 8),  -- 中心点 lat
  cp_lng          DECIMAL(14, 8),  -- 中心点 lng
  cp_name         VARCHAR(255),    -- 中心点名称
  label           VARCHAR(100),  -- 标注信息   [必填]
  marker_color    VARCHAR(100) DEFAULT "#607d8b",  -- 图钉颜色
  color_order     INT,
  
  memo            VARCHAR(200),  -- 备注/描述

  created_at      DATETIME,
  updated_at      DATETIME,

  PRIMARY KEY (id),
  INDEX ct_code_index (ct_code),
  UNIQUE INDEX case_code_index (case_id, ct_code)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;