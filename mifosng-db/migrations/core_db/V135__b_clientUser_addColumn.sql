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
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'national_id'
     and TABLE_NAME = 'b_clientuser'
     and TABLE_SCHEMA = DATABASE())THEN  
alter table b_clientuser Add column national_id varchar(100) default null after unique_reference;
END IF;
END //
DELIMITER ;
call clientuser1();

Drop procedure IF EXISTS clientuser1;

delete from b_message_template where template_description = "SELFCARE REGISTRATION";

INSERT IGNORE INTO b_message_template(template_description,subject,header,body,footer,message_type) values ('SELFCARE REGISTRATION','Streaming Media Selfcare','Dear <PARAM1>','Your Selfcare User Account has been successfully created,Following are the User login Details. \n userName : <PARAM2> , \n password : <PARAM3> .','Thankyou','E'); 
insert ignore into c_configuration VALUES (null,'is_selfcareuser',1,null);


