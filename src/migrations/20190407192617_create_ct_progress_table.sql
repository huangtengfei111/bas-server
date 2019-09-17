CREATE TABLE ct_progress (
	id bigint auto_increment primary key, 
	city varchar(255), 
	yys varchar(8), 
	progress integer default 0
);