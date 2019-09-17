CREATE TABLE ct_labels_label_groups (
  id              INT NOT NULL AUTO_INCREMENT,
  ct_label_id         INT,
  label_group_id        INT,            
  
  created_at      DATETIME,
  updated_at      DATETIME,

  PRIMARY KEY (id),
  INDEX ct_label_id_index (ct_label_id),
  INDEX label_group_id_index (label_group_id)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;