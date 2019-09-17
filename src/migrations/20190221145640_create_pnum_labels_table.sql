CREATE TABLE pnum_labels (
  id              BIGINT NOT NULL AUTO_INCREMENT,
  case_id         INT,
  num             VARCHAR(20),   -- 长号   
  short_num       VARCHAR(20),   -- 短号

  label           VARCHAR(200),  -- 标注文字  [必填]
  label_txt_color VARCHAR(10) DEFAULT "#ffffff",  -- 标注文字颜色
  label_bg_color  VARCHAR(10) DEFAULT "#607d8b",  -- 标注背景颜色
  color_order     INT DEFAULT 16,
  ptags           VARCHAR(255),  -- 个性标签
  
  memo            VARCHAR(200),  -- 备注
  source          INT DEFAULT 1, -- 数据来源 1. 手工单条添加；2. 批量导入；3. 综合人员信息库中导入。
  created_at      DATETIME,
  updated_at      DATETIME,

  PRIMARY KEY (id),
  INDEX case_id_index (case_id),
  INDEX num_index (num),
  INDEX short_num_index (short_num),
  UNIQUE INDEX case_num_index (case_id, num)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;