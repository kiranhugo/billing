INSERT IGNORE INTO `m_code`(id,code_name,is_system_defined,code_description) 
VALUES(null,'Event Category',0,'category for events');
SET @a_lid:=(select id from m_code where code_name='Event Category');
INSERT IGNORE INTO `m_code_value`(id,code_id,code_value,order_position) VALUES(null,@a_lid,'Live Event',0);
INSERT IGNORE INTO `m_code_value`(id,code_id,code_value,order_position) VALUES(null,@a_lid,'VOD',0);

