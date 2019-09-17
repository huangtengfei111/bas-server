CREATE TABLE citizen_addresses (
  id           BIGINT NOT NULL AUTO_INCREMENT,
  citizen_id   BIGINT NOT NULL,
  province     VARCHAR(50),            -- 省 
  city         VARCHAR(50),            -- 市 
  town         VARCHAR(100),           -- 乡镇
  loc          VARCHAR(100),           -- 地址 [必填]
  area_code    VARCHAR(20),            -- 区号
  memo         VARCHAR(50),            -- 备注，一般是办公，私人等
  
  citizen_book_id INT,                 -- 通讯录Id

  created_at   DATETIME,
  updated_at   DATETIME,

  PRIMARY KEY (id),
  INDEX citizen_id_index (citizen_id),
  INDEX loc_index (loc)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;