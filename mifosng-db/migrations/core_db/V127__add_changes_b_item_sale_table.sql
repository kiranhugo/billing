
Drop procedure IF EXISTS addchargecode;
DELIMITER //
create procedure addchargecode() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'charge_code'
     and TABLE_NAME = 'b_itemsale'
     and TABLE_SCHEMA = DATABASE())THEN
alter table b_itemsale add column charge_code varchar(20) DEFAULT NULL;
ALTER TABLE b_itemsale ADD CONSTRAINT `charge_code_key` FOREIGN KEY(`charge_code`) REFERENCES b_charge_codes(`charge_code`);
END IF;
END //
DELIMITER ;
call addchargecode();
Drop procedure IF EXISTS addchargecode;
insert ignore into m_permission (id,grouping,code,entity_name,action_name,can_maker_checker) VALUES (null,'billing','CREATE_ITEMSALE','ITEMSALE','CREATE',0);
insert ignore into m_permission values(null,'billing','CREATE_USERCHATMESSAGE','USERCHATMESSAGE','CREATE',0);


CREATE TABLE IF NOT EXISTS `m_invoice` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sale_id` bigint(20) NOT NULL,
  `invoice_date` datetime NOT NULL,
  `charge_amount` double(24,4) NOT NULL,
  `tax_percantage` double NOT NULL,
  `tax_amount` double(24,4) NOT NULL,
  `invoice_amount` double NOT NULL,
  `createdby_id` bigint(20) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `lastmodified_date` datetime DEFAULT NULL,
  `lastmodifiedby_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

Drop procedure IF EXISTS addpurchaseby;
DELIMITER //
create procedure addpurchaseby() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'purchase_by'
     and TABLE_NAME = 'b_itemsale'
     and TABLE_SCHEMA = DATABASE())THEN
alter table b_itemsale add column purchase_by bigint(10) NOT NULL;
END IF;
END //
DELIMITER ;
call addpurchaseby();
Drop procedure IF EXISTS addpurchaseby;

Drop procedure IF EXISTS addpurchaseFrom; 
DELIMITER //
create procedure addpurchaseFrom() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'purchase_from'
     and TABLE_NAME = 'b_itemsale'
     and TABLE_SCHEMA = DATABASE())THEN
alter table b_itemsale change  agent_id purchase_from int(10)  NOT NULL;
END IF;
END //
DELIMITER ;
call addpurchaseFrom();
Drop procedure IF EXISTS addpurchaseFrom;

CREATE TABLE IF NOT EXISTS `b_login_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ip_address` varchar(200) DEFAULT NULL,
  `device_id` varchar(200) DEFAULT NULL,
  `username` varchar(100) NOT NULL,   
  `session_id` varchar(200) DEFAULT NULL,
  `login_time` datetime DEFAULT NULL,
  `logout_time` datetime DEFAULT NULL,
  `status` char(10) NOT NULL DEFAULT 'INACTIVE',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=396 DEFAULT CHARSET=utf8 COMMENT='utf8_general_ci';


Drop procedure IF EXISTS loginhistoryusername;
DELIMITER //
create procedure loginhistoryusername() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'username'
     and TABLE_NAME = 'b_login_history'
     and TABLE_SCHEMA = DATABASE())THEN
alter table b_login_history add column `username` varchar(20) default null;
END IF;
END //
DELIMITER ;
call loginhistoryusername();
Drop procedure IF EXISTS loginhistoryusername;




