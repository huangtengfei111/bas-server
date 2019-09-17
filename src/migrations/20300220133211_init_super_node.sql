-- SELECT "Data for init system";


INSERT INTO roles set name = "超级管理员", value = "super", created_at = NOW(), updated_at = NOW();

INSERT INTO users set name = "超级管理员", created_at = NOW(), updated_at = NOW();

INSERT INTO accounts SET username = "super", salt = "QZuPGmJycUsF8cKBYrq6HQ==", password = "DMQHrKajEUWpmAy71QLyGOpZvyH1K0ToHdXcrMHhdDk=", built_in = 1, role_id = 3, user_id = 2, created_at = NOW(), updated_at = NOW();
