INSERT IGNORE INTO `m_permission`(grouping,code,entity_name,action_name,can_maker_checker) 
VALUES ('BILLMASTER','CREATE_MEDIAASSETLOCATIONATTRIBUTES','MEDIAASSETLOCATIONATTRIBUTES','CREATE',1);
INSERT IGNORE INTO `m_code`(id,code_name,is_system_defined,code_description) VALUES(null,'Event Category',0,'category for events');
select @a_lid:=last_insert_id();
INSERT IGNORE INTO `m_code_value`(code_id,code_value,order_position) VALUES(null,@a_lid,,'Live Event',0);
INSERT IGNORE INTO `m_code_value`(code_id,code_value,order_position) VALUES(null,@a_lid,,'VOD',0);



