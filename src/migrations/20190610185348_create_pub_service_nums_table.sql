CREATE TABLE pub_service_nums (
  id              INT NOT NULL AUTO_INCREMENT,
  num             VARCHAR(100),
  memo            VARCHAR(100),            
  grup            INT,
  created_at      DATETIME,
  updated_at      DATETIME,

  PRIMARY KEY (id),
  UNIQUE INDEX num_index (num)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;