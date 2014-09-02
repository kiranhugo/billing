insert ignore into c_configuration VALUES (null,'CHANGE_PLAN_ALIGN_DATES',1,null);
insert ignore  into `b_charge_codes` values (null,'NONE','NONE','NRC',0,'NONE',1,'NONE');
insert ignore into m_permission  VALUES (null,'billing','UPDATEIPSTATUS_IPPOOLMANAGEMENT','IPPOOLMANAGEMENT','UPDATEIPSTATUS',0);               
insert ignore  into m_permission VALUES (null,'billing','ACTIVATE_ACTIVATIONPROCESS','ACTIVATIONPROCESS','ACTIVATE',0);
alter TABLE b_charge modify  `discount_code` varchar(20) NOT NULL;