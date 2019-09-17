CREATE TABLE label_groups (
  id              BIGINT NOT NULL AUTO_INCREMENT,
  case_id         int,
  name            VARCHAR(200),  -- 名称 [必填]
  topic           int,           -- 类别: 1: 号码，2: 时间  3: 基站

  updated_at      DATETIME,
  created_at      DATETIME,

  PRIMARY KEY (id),
  INDEX case_id_index (case_id),
  INDEX name_index (name)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;