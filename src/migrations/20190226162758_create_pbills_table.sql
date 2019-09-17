CREATE TABLE pbills (
  id                BIGINT NOT NULL AUTO_INCREMENT,
  owner_name        VARCHAR(50),    -- 话单归属人         
  owner_num         VARCHAR(50),    -- 本方号码           [必填]
  call_attribution  VARCHAR(100),   -- 本方号码归属地
  residence         VARCHAR(255),   -- 本方号码常驻地
  alyz_day_start    DATE,           -- 话单开始名义日期
  alyz_day_end      DATE,           -- 话单结束名义日期
  started_at        DATETIME,       -- 话单时间范围: 开始时间
  ended_at          DATETIME,       -- 话单时间范围: 结束时间
  total             BIGINT DEFAULT 0,         -- 记录总数
  peer_num_count    BIGINT DEFAULT 0,         -- 对方号码数量
  created_at        DATETIME,
  updated_at        DATETIME,

  PRIMARY KEY (id),
  INDEX owner_num_index (owner_num)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;