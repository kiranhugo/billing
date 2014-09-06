
DROP TABLE IF EXISTS `b_prov_plan_details`;
DROP TABLE IF EXISTS `x_table_cloumn_code_mappings`;


Drop procedure IF EXISTS addcategory;
DELIMITER //
create procedure addcategory() 
Begin
  IF NOT EXISTS (
     SELECT * FROM information_schema.COLUMNS
     WHERE COLUMN_NAME = 'category'
     and TABLE_NAME = 'x_registered_table'
     and TABLE_SCHEMA = DATABASE())THEN
alter table x_registered_table add column category bigint(20) DEFAULT NULL;
END IF;
END //
DELIMITER ;
call addcategory();

Drop procedure IF EXISTS addcategory;


CREATE TABLE IF NOT EXISTS `b_prov_plan_details` (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `plan_id` int(10) NOT NULL,
  `plan_identification` varchar(100) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `is_deleted` char(1) DEFAULT 'n',
  PRIMARY KEY (`id`),
  UNIQUE KEY `plan_id` (`plan_id`,`plan_identification`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

 CREATE TABLE IF NOT EXISTS `x_table_column_code_mappings` (
  `column_alias_name` varchar(50) NOT NULL,
  `code_id` int(10) NOT NULL,
  PRIMARY KEY (`column_alias_name`),
  KEY `FK_x_code_id` (`code_id`),
  CONSTRAINT `FK_x_code_id` FOREIGN KEY (`code_id`) REFERENCES `m_code` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
