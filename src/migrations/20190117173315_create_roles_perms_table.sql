CREATE TABLE roles_perms (
  id           INT(11) NOT NULL AUTO_INCREMENT,
  role_id      INT,
  perm_id      INT,

  created_at   DATETIME,
  updated_at   DATETIME,

  PRIMARY KEY (id),
  INDEX role_id_index (role_id),
  INDEX perm_id_index (perm_id)
) 
ENGINE=InnoDB
DEFAULT CHARSET=utf8;