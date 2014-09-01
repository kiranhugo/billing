INSERT IGNORE INTO m_permission VALUES (null,'billing', 'CREATE_PROVISIONINGPLANMAPPING', 'PROVISIONINGPLANMAPPING', 'CREATE', 0);
INSERT IGNORE INTO m_permission VALUES (null,'billing', 'UPDATE_PROVISIONINGPLANMAPPING', 'PROVISIONINGPLANMAPPING', 'UPDATE', 0);
INSERT IGNORE INTO m_permission VALUES (null,'billing', 'DELETE_PROVISIONINGPLANMAPPING', 'PROVISIONINGPLANMAPPING', 'DELETE', 0);
INSERT IGNORE INTO  m_permission VALUES(null,'billing', 'GENERATENEWPASSWORD_SELFCARE', 'SELFCARE', 'GENERATENEWPASSWORD', '0');
INSERT IGNORE INTO  m_permission VALUES(null,'billing', 'UPDATE_SELFCARE', 'SELFCARE', 'UPDATE', '0');
alter table b_clientuser modify column auth_pin varchar(10) default null after unique_reference;
INSERT IGNORE INTO b_message_template(template_description,subject,header,body,footer,message_type) values ('SELFCARE REGISTRATION','Streaming Media Selfcare','Dear <param1>','Your Selfcare User Account has been successfully created,Following are the User login Details. \n userName : <param2> , \n password : <param3> .','Thankyou','E'); 




