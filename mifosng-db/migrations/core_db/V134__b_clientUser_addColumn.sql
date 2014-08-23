Drop procedure IF EXISTS clientuser1; 
DELIMITER //
create procedure clientuser1() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'auth_pin'
     and TABLE_NAME = 'b_clientuser'
     and TABLE_SCHEMA = DATABASE())THEN
alter table b_clientuser Add column auth_pin varchar(10) default null after unique_reference;
END IF;
END //
DELIMITER ;
call clientuser1();

Drop procedure IF EXISTS clientuser1;

