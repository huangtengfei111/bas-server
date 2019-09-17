CREATE TABLE cell_towers (
  id              BIGINT NOT NULL AUTO_INCREMENT,
  code            VARCHAR(20), -- 基站编码
  ci              BIGINT,      -- CID 小区号
  mnc             VARCHAR(10), -- 移动网络编码 对应运营商
  mcc             INT,         -- 国家编码
  lac             INT,         -- LAC(位置区号): LAC CI MNC -> 对应CDMA的 NID BID SID
  isp             INT,         -- 运营商 1: 电信CDMA, 2: 电信4G, 3: 移动, 4. 联通
  ci_hex          VARCHAR(20),
  lac_hex         VARCHAR(20),

  name            VARCHAR(200),
  province        VARCHAR(20),  -- 省份
  city            VARCHAR(50),  -- 城市
  district        VARCHAR(50),  -- 地区
  town            VARCHAR(200), -- 乡镇
  branch          VARCHAR(100),
  addr            VARCHAR(400), -- 全地址
  type            INT,          -- 基站类型: 1 宏站 2 微站
  angle           INT,          -- 角度
  radius          INT,          -- 范围

  lat             DECIMAL(14, 8),     -- 纬度(横)
  lng             DECIMAL(14, 8),     -- 经度(竖)
  blat            DECIMAL(14, 8),     -- Baidu地图经纬度
  blng            DECIMAL(14, 8),
  glat            DECIMAL(14, 8),     -- Google maps调整经纬度
  glng            DECIMAL(14, 8),    
  
  xlat            DECIMAL(14, 8),     -- 加密坐标
  xlng            DECIMAL(14, 8),
  xaddr           VARCHAR(800),       
   
  geohash         CHAR(12),         -- 经纬度hash值
  source          VARCHAR(100),     -- 数据来源
  out_id          VARCHAR(20),
  version         VARCHAR(20),
  created_at      DATETIME,
  updated_at      DATETIME,

  PRIMARY KEY (id),
  INDEX ci_index (ci),
  INDEX mnc_index (mnc),
  INDEX lac_index (lac),
  INDEX out_id_index (out_id),
  INDEX lat_index (lat),
  INDEX lng_index (lng),
  INDEX glat_index (glat),
  INDEX glng_index (glng),
  INDEX xlat_index (xlat),
  INDEX xlng_index (xlng)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;