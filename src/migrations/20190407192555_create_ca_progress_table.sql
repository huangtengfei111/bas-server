CREATE TABLE ca_progress (
	id bigint auto_increment primary key, 
	head varchar(3), 
	isp varchar(8), 
	progress integer default 0
);
