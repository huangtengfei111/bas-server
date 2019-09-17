CREATE TABLE licenses (
  id                 BIGINT NOT NULL AUTO_INCREMENT,         
  host_id            VARCHAR(100),  -- 机器编码 
  holder             VARCHAR(100),  -- 持有人 
  
  system_sn          VARCHAR(255),  -- 系统硬件信息 
  baseboard_info     VARCHAR(255), 
  processor_info     VARCHAR(255), 
  mac_address        VARCHAR(255), 
  ip_address         VARCHAR(255), 
  
  plan               VARCHAR(100),  -- 0: 试用版 1: 个人版 2: 企业版 
  acct_limit         INT,           -- 账号限制数目 
  expired_at         DATETIME,      -- 失效时间 
  issued_at          DATETIME,      -- 开始时间 
  
  public_alias       VARCHAR(255),  
  store_pass         VARCHAR(255),  
  private_alias      VARCHAR(255),   
  key_pass           VARCHAR(255),  
  
  salt               VARCHAR(255),  
  path               VARCHAR(100),  -- license.bin磁盘路径
   
  issued_by          INT,           -- 签发人
  created_at         DATETIME,
  updated_at         DATETIME,
  deleted_at         DATETIME,

  PRIMARY KEY (id),
  UNIQUE INDEX host_id_index(host_id),
  INDEX expired_at_index (expired_at),
  INDEX created_at_index (created_at),
  INDEX deleted_at_index (deleted_at)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;