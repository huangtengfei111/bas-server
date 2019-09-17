CREATE TABLE connections (
  id              BIGINT NOT NULL AUTO_INCREMENT,
  citizen_a       BIGINT NOT NULL,  -- 人员A [必填]
  relationship    INT,              -- 关系: 0: 其他 1: 家人 2: 同事 3:朋友 [必填]
  citizen_b       BIGINT,           -- 人员B [必填]
  memo            VARCHAR(20),      -- 备注

  created_at      DATETIME,
  updated_at      DATETIME,

  PRIMARY KEY (id),
  INDEX citizen_a_index (citizen_a),
  INDEX citizen_b_index (citizen_b),
  INDEX memo_index (memo),
  UNIQUE INDEX rel_index(citizen_a, citizen_b, relationship)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;