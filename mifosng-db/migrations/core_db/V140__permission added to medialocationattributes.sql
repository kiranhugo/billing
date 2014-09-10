INSERT IGNORE INTO `m_permission`(grouping,code,entity_name,action_name,can_maker_checker) 
VALUES ('BILLMASTER','CREATE_MEDIAASSETLOCATIONATTRIBUTES','MEDIAASSETLOCATIONATTRIBUTES','CREATE',1);
INSERT IGNORE INTO `m_code`(id,code_name,is_system_defined,code_description) VALUES(52,'Event Category',0,'category for events');
INSERT IGNORE INTO `m_code_value`(code_id,code_value,order_position) VALUES(52,'Live Event',0);
INSERT IGNORE INTO `m_code_value`(code_id,code_value,order_position) VALUES(52,'VOD',0);



