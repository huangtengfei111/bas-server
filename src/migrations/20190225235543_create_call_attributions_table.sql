CREATE TABLE call_attributions (
  id              BIGINT NOT NULL AUTO_INCREMENT,
  num             VARCHAR(20),  -- 手机号码
  isp             VARCHAR(20),  -- 运营商
                                -- CMCC:   "中国移动",
                                -- CUCC:   "中国联通",
                                -- CTCC:   "中国电信",
                                -- CTCC_v: "中国电信虚拟运营商",
                                -- CUCC_v: "中国联通虚拟运营商",
                                -- CMCC_v: "中国移动虚拟运营商",
  zone            VARCHAR(10),  -- 地区编码
  city            VARCHAR(100), -- 城市
  province        VARCHAR(100), -- 省

  created_at      DATETIME,
  updated_at      DATETIME,

  PRIMARY KEY (id),
  INDEX num_index (num)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;