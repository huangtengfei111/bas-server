CREATE TABLE ven_numbers (
  id              BIGINT NOT NULL AUTO_INCREMENT,
  case_id         INT,
  num             VARCHAR(20),  -- 长号        [必填]
  short_num       VARCHAR(20),  -- 短号        [必填]
  network         VARCHAR(200) NOT NULL, -- 虚拟网名称 [必填]
  label           VARCHAR(200), -- 标注
  memo            VARCHAR(200), -- 备注
  source          INT DEFAULT 1,-- 数据来源 1. 手工单条添加；2. 批量导入；3. 综合人员信息库中导入。

  created_at      DATETIME,
  updated_at      DATETIME,

  PRIMARY KEY (id),
  INDEX case_id_index (case_id),
  INDEX num_index (num),
  INDEX short_num_index (short_num),
  INDEX network_index (network),
  UNIQUE INDEX d_key_index (case_id, num)   -- 一个手机只能入一个虚拟网
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;