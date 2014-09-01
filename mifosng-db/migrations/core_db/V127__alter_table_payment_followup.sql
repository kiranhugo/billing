insert ignore into r_enum_value VALUES ('order_status',7,'REACTIVE','REACTIVE');
insert ignore into m_permission VALUES (null,'Ordering','REACTIVE_ORDER','ORDER','REACTIVE',0);



Drop procedure IF EXISTS addreactivedate;
DELIMITER //
create procedure addreactivedate() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'reactive_date'
     and TABLE_NAME = 'b_payment_followup'
     and TABLE_SCHEMA = DATABASE())THEN
ALTER table b_payment_followup add column `reactive_date` datetime DEFAULT NULL;
END IF;
END //
DELIMITER ;
call addreactivedate();

Drop procedure IF EXISTS addreactivedate;
