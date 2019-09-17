CREATE TABLE citizens (
  id           BIGINT NOT NULL AUTO_INCREMENT,
  social_no    VARCHAR(128),           -- 身份证 
  name         VARCHAR(10),            -- 姓名   [必填]
  address      VARCHAR(128),           -- 住址
  phone        VARCHAR(100),           -- 固定电话/办电
  mobile       VARCHAR(100),           -- 手机
  ven_num      VARCHAR(100),           -- 虚拟网短号
  ven_name     VARCHAR(100),           -- 虚拟网名称
  company      VARCHAR(128),           -- 公司/单位
  position     VARCHAR(500),           -- 职务
  
  category     VARCHAR(10),            -- 人员类型: g: 国家公务员 b: 企业人员 j: 国企人员 z: 一般人员
  version      VARCHAR(100),           -- 年份版本 

  citizen_book_id INT,                 -- 通讯录Id. id = -1 表示人工录入

  created_at   DATETIME,
  updated_at   DATETIME,

  PRIMARY KEY (id),
  -- UNIQUE INDEX social_no_index (social_no),
  INDEX social_no_index (social_no),
  INDEX citizen_book_id_index (citizen_book_id),
  INDEX name_index (name)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;