CREATE TABLE pbill_records (
  id                BIGINT NOT NULL AUTO_INCREMENT,
  pbill_id          INT NOT NULL,
  owner_num         VARCHAR(50),              -- `self_number(1)` 本方号码  -> (1) 从0开始 
  owner_short_num   VARCHAR(50),              -- 本方短号
  owner_num_status  INT,                      -- `status(24)` 本方号码状态 0-其他 1-本地 2-漫游 
  owner_comm_loc    VARCHAR(100),             -- `self_area(25)` 本方通话地   
  peer_num          VARCHAR(50),              -- `term_number(2)` 对方号码  
  peer_short_num    VARCHAR(50),              -- `term_short_number(4)`对方短号 
  peer_comm_loc     VARCHAR(100),             -- `term_area(26)` 对方通话地   
  peer_num_attr     VARCHAR(100),             -- `number_area(6)` 对方号码归属地 
  peer_num_isp      VARCHAR(20),              -- `number_company(7)` 对方号码运营商 
  peer_num_type     INT,                      -- `term_number_type(5)` 对方号码类型: 0-其他 11-移动手机 21-电信手机 31-联通手机 61-固话
  started_at        DATETIME,                 -- `begin_int(10)` 开始时间  
  ended_at          DATETIME,                 -- `end_int(11)` 结束时间    
  weekday           INT,                      -- `weekday(12)`, 周几 1-7分别周一至周日
  started_day       DATE,                     -- `begin_on(13)` 开始日期"yyyyMMdd"
  started_time      VARCHAR(10),              -- `begin_at(16)` 开始时间"HHmm"
  started_time_l1_class INT,                  -- `begin_at_type(17)` 时间分类 0-4:30~7:30 1-7:31~11:15 2-11:16~13:30 3-13:31~17:15 4- 17:16~19:00 5-19:01~20:50 6-20:51~23:59 7-0点~5:30
                                                                        --   0-早晨      1-上午  2- 中午 3-下午 4- 傍晚 5- 晚上 6 深夜 7 凌晨 
  started_time_l2_class INT,                  -- `begin_at_type_more(18)`时间分类: 0-4:30~6:20 1-6:21~7:10 2-7:11~7:50 3- 7:51~8:25 4-8:26~11:00 5-11:01~11:30 6-11:31~12:30 7-12:31~13:20 8- 13:21~14:00 9-14:01~16:50 10-16:51~17:40 11-17:41~18:50 12-18:51~20:00 13- 20:01~21:50 14-21:51~23:59 15-0点~4:29
  started_hour_class    INT,                  -- `begin_at_type_hour(19)`开始时间(小时)分类: 0-4时 1-5时 2-6时 3-7时 4-8时 5-9时 6-10 时 7-11时 8-12时 9-13时 10-14时 11-15时 12-16时 13-17时 14-18时 15-19 时 16-20时 17-21时 18-22时 19-23时 20-0时 21-1时 22-2时 23-3时 
  alyz_day          DATE,                     -- `begin_should_on(14)` 名义日期(案件分析日期)
  alyz_day_type     INT,                      -- `begin_on_type(15)` 名义日期性质
  duration          INT,                      -- `duration(21)`时长
  duration_class    INT,                      -- `duration_type(22)` 时长类型: 0: 其他 1: 1~15秒 2: 16-90秒 3: 1.5~3分 4: 3~5分, 5: 5~10分 6: > 10分
  time_class        INT,                      -- `time_type(20)`时间性质: 0: 私人时间 1: 工作时间 (私人时间是周末和工作日7:00-18:30之外的时间。暂时没考虑节假日)
  bill_type         INT,                      -- `billing_type(9)` 计费类型: 1: 通话 2: 短信 3: 彩信 
  comm_direction    INT,                      -- `connect_type(23)` 联系类型: 0: 未知 11: 主叫 12: <--- 13: 呼转 21 主短 22: 被短 31: 主彩 32: 被彩
  long_dist         INT,                      -- `long_call(27)`是否长途: 0: 否，1: 是
  ven               INT,                      -- `vpn(8)`是否虚拟网: 0: 否 1: 是
          
  owner_ct_code     VARCHAR(100),             -- `lac_cid(30)` 基站编码(lac:ci:mnc) 本方基站
  owner_lac         INT,                      -- `lac_hex(28)` 基站LAC    (-1)表示不存在
  owner_ci          BIGINT,                   -- `cid_hex(29)` 基站(CId)  (-1)表示不存在
  owner_mnc         BIGINT,          
  owner_ct_id       BIGINT,          
  owner_ct_lat      DECIMAL(14, 8),           -- 本方号码基站经纬度(国际标准坐标)
  owner_ct_lng      DECIMAL(14, 8),           --
  owner_geohash     CHAR(12),
  owner_ct_city     VARCHAR(50),              -- 城市
  owner_ct_dist     VARCHAR(50),              -- 地区
  owner_ct_town     VARCHAR(200),             -- 乡镇
            
  peer_ct_code      VARCHAR(100),          
  peer_lac          INT,          
  peer_ci           BIGINT,          
  peer_mnc          BIGINT,          
  peer_ct_id        BIGINT,          
  peer_ct_lat       DECIMAL(14, 8),           -- 对方号码基站经纬度(国际标准坐标)
  peer_ct_lng       DECIMAL(14, 8),
  peer_geohash      CHAR(12),
  peer_ct_city      VARCHAR(50),              -- 城市
  peer_ct_dist      VARCHAR(50),              -- 地区
  peer_ct_town      VARCHAR(200),             -- 乡镇
  
  owner_citizen_id  BIGINT,                   -- 本方人员库信息
  owner_cname       VARCHAR(20),              -- 本方号码人员库姓名
  peer_citizen_id   BIGINT,                   -- 对方人员库信息
  peer_cname        VARCHAR(20),              -- 对方号码人员库姓名
      
  merged_pbr_id     BIGINT,                   -- 话单合并对应的记录

  memo              VARCHAR(255),             -- 记录小变动日志信息
  
  created_at   DATETIME,
  updated_at   DATETIME,

  PRIMARY KEY (id),
  INDEX pbill_id_index (pbill_id),
  INDEX owner_num_index (owner_num),
  INDEX owner_short_num_index (owner_short_num),
  INDEX peer_num_index (peer_num),
  INDEX peer_short_num_index (peer_short_num),
  INDEX started_at_index (started_at),
  INDEX started_day_index (started_day),
  INDEX alyz_day_index (alyz_day),

  INDEX owner_ct_code_index (owner_ct_code),
  INDEX owner_mnc_index (owner_mnc),
  INDEX owner_ci_index (owner_ci),
  INDEX owner_lac_index (owner_lac),
  INDEX owner_ct_id_index (owner_ct_id),
  INDEX owner_ct_lat_index (owner_ct_lat),
  INDEX owner_ct_lng_index (owner_ct_lng),

  INDEX peer_ct_code_index (peer_ct_code),
  INDEX peer_mnc_index (peer_mnc),
  INDEX peer_lac_index (peer_lac),
  INDEX peer_ci_index (peer_ci),
  INDEX peer_ct_id_index (peer_ct_id),
  INDEX peer_ct_lat_index (peer_ct_lat),
  INDEX peer_ct_lng_index (peer_ct_lng),

  INDEX merged_pbr_id_index (merged_pbr_id),
  
  UNIQUE INDEX owner_num_time_index(owner_num, started_at)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;