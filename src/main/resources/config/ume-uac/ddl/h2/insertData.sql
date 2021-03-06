DELETE FROM UME_ROLE ;
INSERT INTO UME_ROLE(ROLE_ID,ROLE_NAME,ROLE_DESC,CREATE_AUTHOR,CREATE_DATETIME,UPDATE_AUTHOR,UPDATE_DATETIME) VALUES ('Administrators','管理员','权限仅次于超级用户','admin',null,'admin',current_timestamp()) ;
INSERT INTO UME_ROLE(ROLE_ID,ROLE_NAME,ROLE_DESC,CREATE_AUTHOR,CREATE_DATETIME,UPDATE_AUTHOR,UPDATE_DATETIME) VALUES ('Guests','访客','访客','admin',null,'admin',current_timestamp()) ;

DELETE FROM UME_ROLE_ACL ;
INSERT INTO UME_ROLE_ACL(ROLE_ID,ACC_RES_ID,ACC_RES_TYPE,ACC_LEVEL,CREATE_AUTHOR,CREATE_DATETIME,UPDATE_AUTHOR,UPDATE_DATETIME) VALUES ('Administrators','*',0,4,'admin',null,'admin',current_timestamp()) ;
INSERT INTO UME_ROLE_ACL(ROLE_ID,ACC_RES_ID,ACC_RES_TYPE,ACC_LEVEL,CREATE_AUTHOR,CREATE_DATETIME,UPDATE_AUTHOR,UPDATE_DATETIME) VALUES ('Guests','*',0,1,'admin',null,'admin',current_timestamp()) ;

DELETE FROM UME_USER ;
INSERT INTO UME_USER(USER_ID,USER_NAME,USER_PASSWORD,USER_MOBILE,USER_EMAIL,USER_STATUS,CREATE_AUTHOR,CREATE_DATETIME,UPDATE_AUTHOR,UPDATE_DATETIME) VALUES ('admin','系统管理员','8ddcff3a80f4189ca1c9d4d902c3c909','18600000001','sample-admin@gmail.com',1,'admin',current_timestamp(),'admin',current_timestamp()) ;
INSERT INTO UME_USER(USER_ID,USER_NAME,USER_PASSWORD,USER_MOBILE,USER_EMAIL,USER_STATUS,CREATE_AUTHOR,CREATE_DATETIME,UPDATE_AUTHOR,UPDATE_DATETIME) VALUES ('guest','访客','8ddcff3a80f4189ca1c9d4d902c3c909','18600000002','sample-guest@gmail.com',1,'admin',current_timestamp(),'admin',current_timestamp()) ;

DELETE FROM UME_USER_ROLE ;
INSERT INTO UME_USER_ROLE(USER_ID,ROLE_ID,CREATE_AUTHOR,CREATE_DATETIME,UPDATE_AUTHOR,UPDATE_DATETIME) VALUES ('admin','Administrators','admin',null,'admin',current_timestamp()) ;
INSERT INTO UME_USER_ROLE(USER_ID,ROLE_ID,CREATE_AUTHOR,CREATE_DATETIME,UPDATE_AUTHOR,UPDATE_DATETIME) VALUES ('admin','Guests','admin',null,'admin',current_timestamp()) ;
INSERT INTO UME_USER_ROLE(USER_ID,ROLE_ID,CREATE_AUTHOR,CREATE_DATETIME,UPDATE_AUTHOR,UPDATE_DATETIME) VALUES ('guest','Guests','admin',null,'admin',current_timestamp()) ;

COMMIT;


