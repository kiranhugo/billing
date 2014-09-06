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
END //
DELIMITER ;
call temp();

Drop procedure IF EXISTS temp;

INSERT IGNORE INTO `c_configuration`(`id`,`name`,`enabled`,`value`) values (null,'Registration_requires_device',1,'');


