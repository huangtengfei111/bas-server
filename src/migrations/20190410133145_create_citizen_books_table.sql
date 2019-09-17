CREATE TABLE citizen_books (
  id              BIGINT NOT NULL AUTO_INCREMENT,
  name            VARCHAR(200),  -- 名称      [必填]
  area_code       VARCHAR(100),  -- 区号 
  version         VARCHAR(100),  -- 年份版本   [必填]
  category        VARCHAR(10),   -- 类型 g: 国家公务员 b: 企业人员 j: 国企人员 z: 一般人员
  filename        VARCHAR(200),  -- 文件名称

  created_at      DATETIME,

  PRIMARY KEY (id),
  INDEX filename_index (filename)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;