CREATE TABLE case_events (
  id           INT(11) NOT NULL AUTO_INCREMENT,
  case_id      INT(11) NOT NULL,
  name         VARCHAR(128),   -- 事件名称 [必填]
  started_at   DATETIME,       -- 开始时间 [必填]
  ended_at     DATETIME,       -- 结束时间 [必填]
  memo         VARCHAR(256),   -- 备注
  color        VARCHAR(10),    -- 标注颜色

  created_at   DATETIME,
  updated_at   DATETIME,

  PRIMARY KEY (id),
  INDEX case_id_index (case_id),
  INDEX started_at_index (started_at),
  INDEX ended_at_index (ended_at)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;