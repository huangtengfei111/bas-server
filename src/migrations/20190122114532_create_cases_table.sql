CREATE TABLE cases (
  id                      INT(11) NOT NULL AUTO_INCREMENT,
  name                    VARCHAR(128),   -- 案件名称     [必填]
  num                     VARCHAR(128),   -- 案件编号     [必填]
  started_at              DATETIME,       -- 案件开始时间
  ended_at                DATETIME,       -- 案件结束时间
  operator                VARCHAR(100),   -- 负责人/经办人
  pb_started_at           DATETIME,       -- 话单开始时间
  pb_ended_at             DATETIME,       -- 话单结束时间
  pb_alyz_day_start       DATE,           -- 话单开始名义日期
  pb_alyz_day_end         DATE,           -- 话单结束名义日期
  owner_num_count         BIGINT DEFAULT 0,         -- 本方号码数量
  peer_num_count          BIGINT DEFAULT 0,         -- 对方号码数量
  pb_rec_count            BIGINT DEFAULT 0,         -- 话单记录条数
  pb_city                 VARCHAR(200),             -- 话单主要城市
  status                  INT DEFAULT 1,            -- 状态: 0: archived, 1: active     
  memo                    VARCHAR(256),   -- 备注   
  
  created_by              INT,            -- 创建人        
  created_at              DATETIME,
  updated_at              DATETIME,
  
  PRIMARY KEY (id),
  INDEX name_index (name),
  UNIQUE INDEX num_index (num)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;