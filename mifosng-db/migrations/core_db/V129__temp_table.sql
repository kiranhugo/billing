Drop procedure IF EXISTS temp; 
DELIMITER //
create procedure temp() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'payment_status'
     and TABLE_NAME = 'temp'
     and TABLE_SCHEMA = DATABASE())THEN
alter  table temp add column payment_status varchar(20) DEFAULT 'INACTIVE';

END IF;

IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'payment_data'
     and TABLE_NAME = 'temp'
     and TABLE_SCHEMA = DATABASE())THEN
alter  table temp add column payment_data varchar(500) DEFAULT NULL;
END IF;

IF EXISTS (SELECT INDEX_NAME FROM INFORMATION_SCHEMA.STATISTICS WHERE
`TABLE_CATALOG` = 'def' AND `TABLE_SCHEMA` = DATABASE() AND
`TABLE_NAME` = 'b_clientuser' AND `INDEX_NAME` = 'username')THEN
alter table b_clientuser drop index username;
END IF;

END //
DELIMITER ;
call temp();

Drop procedure IF EXISTS temp;


