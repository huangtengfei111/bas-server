CREATE TABLE ca_progress_error (
	id bigint auto_increment primary key, 
	num varchar(7), 
	isp varchar(8), 
	code integer, 
	msg varchar(255), 
	created_at datetime
);
