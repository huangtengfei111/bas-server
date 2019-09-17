CREATE TABLE citizen_phones (
  id           BIGINT NOT NULL AUTO_INCREMENT,
  citizen_id   BIGINT NOT NULL,
  num          VARCHAR(20),            -- 号码 [必填]
  memo         VARCHAR(50),            -- 备注，一般是办公，私人等
  ven_name     VARCHAR(100),           -- 虚拟网名称(如果是短号)
  citizen_book_id INT,                 -- 通讯录Id 

  created_at   DATETIME,
  updated_at   DATETIME,

  PRIMARY KEY (id),
  INDEX citizen_id_index (citizen_id),
  INDEX citizen_book_id_index (citizen_book_id),
  INDEX num_index (num)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;