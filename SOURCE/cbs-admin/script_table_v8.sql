-- Start of DDL Script for Table ADM_OWNER.AM_ACCESS_TIME
-- Generated 26-Jul-2014 10:20:38 from ADM_OWNER@ORCL

CREATE TABLE am_access_time
    (at_id                          NUMBER(10,0) NOT NULL,
    name                           VARCHAR2(50 BYTE) NOT NULL,
    status                         NUMBER(1,0),
    ord                            NUMBER(10,0))
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Constraints for AM_ACCESS_TIME

ALTER TABLE am_access_time
ADD CONSTRAINT am_schedule_uk UNIQUE (name)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
/

ALTER TABLE am_access_time
ADD CONSTRAINT am_schedule_pk PRIMARY KEY (at_id)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
/


-- Comments for AM_ACCESS_TIME

COMMENT ON COLUMN am_access_time.at_id IS 'Ma lich truy nhap'
/
COMMENT ON COLUMN am_access_time.name IS 'Ten lich truy nhap'
/
COMMENT ON COLUMN am_access_time.status IS '0: Disabled, 1: Enabled, 2: System'
/

-- End of DDL Script for Table ADM_OWNER.AM_ACCESS_TIME

-- Start of DDL Script for Table ADM_OWNER.AM_ACCESS_TIME_DTL
-- Generated 26-Jul-2014 10:20:38 from ADM_OWNER@ORCL

CREATE TABLE am_access_time_dtl
    (at_id                          NUMBER(10,0) NOT NULL,
    day_id                         NUMBER(1,0) NOT NULL,
    start_time                     VARCHAR2(8 BYTE) NOT NULL,
    end_time                       VARCHAR2(8 BYTE) NOT NULL)
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Constraints for AM_ACCESS_TIME_DTL

ALTER TABLE am_access_time_dtl
ADD CONSTRAINT am_schedule_detail_pk PRIMARY KEY (at_id, day_id, start_time)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
/



-- Comments for AM_ACCESS_TIME_DTL

COMMENT ON TABLE am_access_time_dtl IS 'Bang du lieu lich truy cap chi tiet'
/
COMMENT ON COLUMN am_access_time_dtl.at_id IS 'Ma lich truy nhap'
/
COMMENT ON COLUMN am_access_time_dtl.day_id IS 'Ma ngay (bat dau tu 0)'
/
COMMENT ON COLUMN am_access_time_dtl.end_time IS 'Gio ket thuc hieu luc'
/
COMMENT ON COLUMN am_access_time_dtl.start_time IS 'Gio bat dau duoc phep truy cap'
/

-- End of DDL Script for Table ADM_OWNER.AM_ACCESS_TIME_DTL

-- Start of DDL Script for Table ADM_OWNER.AM_ADDR_DTL
-- Generated 26-Jul-2014 10:20:38 from ADM_OWNER@ORCL

CREATE TABLE am_addr_dtl
    (addr_id                        NUMBER(5,0) NOT NULL,
    address                        VARCHAR2(50 BYTE) NOT NULL,
    subnet                         VARCHAR2(50 BYTE) NOT NULL)
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Constraints for AM_ADDR_DTL

ALTER TABLE am_addr_dtl
ADD CONSTRAINT am_ip_detail_pk PRIMARY KEY (addr_id, address)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
/



-- Comments for AM_ADDR_DTL

COMMENT ON COLUMN am_addr_dtl.addr_id IS 'Ma dai dia chi IP'
/
COMMENT ON COLUMN am_addr_dtl.address IS 'Dia chi IP'
/

-- End of DDL Script for Table ADM_OWNER.AM_ADDR_DTL

-- Start of DDL Script for Table ADM_OWNER.AM_ADDR_GROUP
-- Generated 26-Jul-2014 10:20:38 from ADM_OWNER@ORCL

CREATE TABLE am_addr_group
    (addr_id                        NUMBER(5,0) NOT NULL,
    group_id                       NUMBER(10,0) NOT NULL,
    grant_type                     NUMBER(1,0) NOT NULL)
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Constraints for AM_ADDR_GROUP

ALTER TABLE am_addr_group
ADD CONSTRAINT am_ip_group_pk PRIMARY KEY (addr_id, group_id)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
/




-- Comments for AM_ADDR_GROUP

COMMENT ON COLUMN am_addr_group.addr_id IS 'Ma dai dia chi IP'
/
COMMENT ON COLUMN am_addr_group.grant_type IS 'Quyen su dung, 0: Deny, 1: Allow'
/
COMMENT ON COLUMN am_addr_group.group_id IS 'Ma nhom NSD'
/

-- End of DDL Script for Table ADM_OWNER.AM_ADDR_GROUP

-- Start of DDL Script for Table ADM_OWNER.AM_ADDR_USER
-- Generated 26-Jul-2014 10:20:38 from ADM_OWNER@ORCL

CREATE TABLE am_addr_user
    (addr_id                        NUMBER(5,0) NOT NULL,
    user_id                        NUMBER(10,0) NOT NULL,
    grant_type                     NUMBER(1,0) NOT NULL)
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Constraints for AM_ADDR_USER

ALTER TABLE am_addr_user
ADD CONSTRAINT am_ip_user_pk PRIMARY KEY (addr_id, user_id)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
/




-- Comments for AM_ADDR_USER

COMMENT ON COLUMN am_addr_user.addr_id IS 'Ma dai dia chi IP'
/
COMMENT ON COLUMN am_addr_user.grant_type IS 'Quyen su dung, 0: Deny, 1: Allow'
/
COMMENT ON COLUMN am_addr_user.user_id IS 'Ma NSD'
/

-- End of DDL Script for Table ADM_OWNER.AM_ADDR_USER

-- Start of DDL Script for Table ADM_OWNER.AM_APP
-- Generated 26-Jul-2014 10:20:38 from ADM_OWNER@ORCL

CREATE TABLE am_app
    (app_id                         NUMBER(5,0) NOT NULL,
    code                           VARCHAR2(50 BYTE) NOT NULL,
    name                           VARCHAR2(100 BYTE) NOT NULL,
    status                         NUMBER(1,0) NOT NULL,
    ord                            NUMBER(5,0))
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Indexes for AM_APP

CREATE UNIQUE INDEX uk_apps ON am_app
  (
    code                            ASC
  )
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  indx
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
NOPARALLEL
LOGGING
/



-- Constraints for AM_APP

ALTER TABLE am_app
ADD CONSTRAINT uk_am_app_code UNIQUE (code)
/

ALTER TABLE am_app
ADD CONSTRAINT uk_am_app_name UNIQUE (name)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  indx
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/

ALTER TABLE am_app
ADD CONSTRAINT pk_apps PRIMARY KEY (app_id)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  indx
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/


-- End of DDL Script for Table ADM_OWNER.AM_APP

-- Start of DDL Script for Table ADM_OWNER.AM_APP_OBJ
-- Generated 26-Jul-2014 10:20:38 from ADM_OWNER@ORCL

CREATE TABLE am_app_obj
    (app_id                         NUMBER(5,0) NOT NULL,
    obj_id                         NUMBER(5,0) NOT NULL)
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Indexes for AM_APP_OBJ

CREATE UNIQUE INDEX uk_apps_obj ON am_app_obj
  (
    app_id                          ASC,
    obj_id                          ASC
  )
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  indx
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
NOPARALLEL
LOGGING
/



-- Constraints for AM_APP_OBJ



ALTER TABLE am_app_obj
ADD CONSTRAINT am_app_obj_uk UNIQUE (obj_id)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/


-- End of DDL Script for Table ADM_OWNER.AM_APP_OBJ

-- Start of DDL Script for Table ADM_OWNER.AM_CLIENT_ADDR
-- Generated 26-Jul-2014 10:20:38 from ADM_OWNER@ORCL

CREATE TABLE am_client_addr
    (addr_id                        NUMBER(5,0) NOT NULL,
    name                           VARCHAR2(50 BYTE) NOT NULL,
    description                    VARCHAR2(512 BYTE),
    grant_type                     NUMBER(1,0) NOT NULL,
    status                         NUMBER(1,0) NOT NULL,
    ord                            NUMBER(5,0) NOT NULL)
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Constraints for AM_CLIENT_ADDR

ALTER TABLE am_client_addr
ADD CONSTRAINT am_ip_uk UNIQUE (name)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
/

ALTER TABLE am_client_addr
ADD CONSTRAINT am_ip_pk PRIMARY KEY (addr_id)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
/


-- Comments for AM_CLIENT_ADDR

COMMENT ON COLUMN am_client_addr.addr_id IS 'Ma dai dia chi IP'
/
COMMENT ON COLUMN am_client_addr.description IS 'Mo ta dia chi may'
/
COMMENT ON COLUMN am_client_addr.grant_type IS 'Quyen su dung, 0: Deny, 1: Allow'
/
COMMENT ON COLUMN am_client_addr.name IS 'Ten dai dia chi IP'
/
COMMENT ON COLUMN am_client_addr.ord IS 'So thu tu'
/
COMMENT ON COLUMN am_client_addr.status IS '0: Disabled, 1: Enabled'
/

-- End of DDL Script for Table ADM_OWNER.AM_CLIENT_ADDR

-- Start of DDL Script for Table ADM_OWNER.AM_DATA_HISTORY
-- Generated 26-Jul-2014 10:20:38 from ADM_OWNER@ORCL

CREATE TABLE am_data_history
    (change_id                      NUMBER(10,0) NOT NULL,
    log_id                         NUMBER(10,0) NOT NULL,
    name                           VARCHAR2(50 BYTE) NOT NULL,
    row_id                         VARCHAR2(50 BYTE) NOT NULL,
    action_type                    VARCHAR2(10 BYTE) NOT NULL)
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Indexes for AM_DATA_HISTORY

CREATE INDEX stl_idx_action_type ON am_data_history
  (
    action_type                     ASC
  )
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  indx
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
NOPARALLEL
LOGGING
/

CREATE INDEX stl_idx_log_id ON am_data_history
  (
    log_id                          ASC
  )
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  indx
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
NOPARALLEL
LOGGING
/

CREATE INDEX stl_idx_table_name ON am_data_history
  (
    name                            ASC
  )
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  indx
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
NOPARALLEL
LOGGING
/



-- Constraints for AM_DATA_HISTORY

ALTER TABLE am_data_history
ADD CONSTRAINT am_table_log_pk PRIMARY KEY (change_id)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/



-- Comments for AM_DATA_HISTORY

COMMENT ON TABLE am_data_history IS 'Bang lich su thay doi thong tin cac bang trong CSDL'
/
COMMENT ON COLUMN am_data_history.action_type IS 'Loai tac dong'
/
COMMENT ON COLUMN am_data_history.change_id IS 'Ma thay doi'
/
COMMENT ON COLUMN am_data_history.log_id IS 'Ma ghi nhan'
/
COMMENT ON COLUMN am_data_history.name IS 'Ten bang'
/
COMMENT ON COLUMN am_data_history.row_id IS 'Ma dong'
/

-- End of DDL Script for Table ADM_OWNER.AM_DATA_HISTORY

-- Start of DDL Script for Table ADM_OWNER.AM_GLOBAL_PROPERTY
-- Generated 26-Jul-2014 10:20:39 from ADM_OWNER@ORCL

CREATE TABLE am_global_property
    (name                           VARCHAR2(50 BYTE) NOT NULL,
    value                          VARCHAR2(50 BYTE))
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Constraints for AM_GLOBAL_PROPERTY

ALTER TABLE am_global_property
ADD CONSTRAINT am_system_policy_pk PRIMARY KEY (name)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/


-- Comments for AM_GLOBAL_PROPERTY

COMMENT ON COLUMN am_global_property.name IS 'Loai chinh sach'
/
COMMENT ON COLUMN am_global_property.value IS 'Gia tri ap dung'
/

-- End of DDL Script for Table ADM_OWNER.AM_GLOBAL_PROPERTY

-- Start of DDL Script for Table ADM_OWNER.AM_GROUP
-- Generated 26-Jul-2014 10:20:39 from ADM_OWNER@ORCL

CREATE TABLE am_group
    (group_id                       NUMBER(10,0) NOT NULL,
    name                           VARCHAR2(50 BYTE) NOT NULL,
    description                    VARCHAR2(512 BYTE),
    status                         NUMBER(1,0),
    parent_id                      NUMBER(10,0))
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Constraints for AM_GROUP

ALTER TABLE am_group
ADD CONSTRAINT am_group_uk UNIQUE (name)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/

ALTER TABLE am_group
ADD CONSTRAINT am_group_pk PRIMARY KEY (group_id)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/



-- Comments for AM_GROUP

COMMENT ON COLUMN am_group.description IS 'Mo ta nhom'
/
COMMENT ON COLUMN am_group.group_id IS 'Ma nhom NSD'
/
COMMENT ON COLUMN am_group.name IS 'Ten nhom NSD'
/
COMMENT ON COLUMN am_group.parent_id IS 'Ma nhom cha'
/
COMMENT ON COLUMN am_group.status IS '0: Disabled, 1: Enabled, 2: System'
/

-- End of DDL Script for Table ADM_OWNER.AM_GROUP

-- Start of DDL Script for Table ADM_OWNER.AM_GROUP_ACCESS_TIME
-- Generated 26-Jul-2014 10:20:39 from ADM_OWNER@ORCL

CREATE TABLE am_group_access_time
    (group_id                       NUMBER(10,0) NOT NULL,
    at_id                          NUMBER(10,0) NOT NULL)
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Constraints for AM_GROUP_ACCESS_TIME

ALTER TABLE am_group_access_time
ADD CONSTRAINT am_group_schedule_pk PRIMARY KEY (group_id, at_id)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
/




-- Comments for AM_GROUP_ACCESS_TIME

COMMENT ON TABLE am_group_access_time IS 'Bang map nhom NSD - lich truy nhap, luu du lieu phan quyen truy nhap cho nhom NSD'
/
COMMENT ON COLUMN am_group_access_time.at_id IS 'Ma lich truy nhap'
/
COMMENT ON COLUMN am_group_access_time.group_id IS 'Ma nhom NSD'
/

-- End of DDL Script for Table ADM_OWNER.AM_GROUP_ACCESS_TIME

-- Start of DDL Script for Table ADM_OWNER.AM_GROUP_OBJECT
-- Generated 26-Jul-2014 10:20:39 from ADM_OWNER@ORCL

CREATE TABLE am_group_object
    (group_id                       NUMBER(10,0) NOT NULL,
    object_id                      NUMBER(5,0) NOT NULL,
    right_code                     VARCHAR2(5 BYTE) NOT NULL,
    access_type                    NUMBER(1,0) NOT NULL)
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Constraints for AM_GROUP_OBJECT

ALTER TABLE am_group_object
ADD CONSTRAINT am_group_module_pk PRIMARY KEY (group_id, object_id, right_code)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/





-- Comments for AM_GROUP_OBJECT

COMMENT ON TABLE am_group_object IS 'Bang map nhom NSD - chuc nang, luu du lieu phan quyen cua nhom NSD tren chuc nang'
/
COMMENT ON COLUMN am_group_object.access_type IS 'Quyen truy cap, 0: Deny, 1: Allow'
/
COMMENT ON COLUMN am_group_object.group_id IS 'Ma nhom NSD'
/
COMMENT ON COLUMN am_group_object.object_id IS 'Ma chuc nang'
/
COMMENT ON COLUMN am_group_object.right_code IS 'Ma quyen truy cap (S, I, U, D)'
/

-- End of DDL Script for Table ADM_OWNER.AM_GROUP_OBJECT

-- Start of DDL Script for Table ADM_OWNER.AM_GROUP_USER
-- Generated 26-Jul-2014 10:20:39 from ADM_OWNER@ORCL

CREATE TABLE am_group_user
    (group_id                       NUMBER(10,0) NOT NULL,
    user_id                        NUMBER(10,0) NOT NULL)
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Constraints for AM_GROUP_USER

ALTER TABLE am_group_user
ADD CONSTRAINT am_group_user_pk PRIMARY KEY (group_id, user_id)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/




-- Comments for AM_GROUP_USER

COMMENT ON TABLE am_group_user IS 'Bang map nhom NSD - NSD, luu thong tin phan nhom NSD'
/
COMMENT ON COLUMN am_group_user.group_id IS 'Ma nhom NSD'
/
COMMENT ON COLUMN am_group_user.user_id IS 'Ma NSD'
/

-- End of DDL Script for Table ADM_OWNER.AM_GROUP_USER

-- Start of DDL Script for Table ADM_OWNER.AM_LOG_ACCESS
-- Generated 26-Jul-2014 10:20:39 from ADM_OWNER@ORCL

CREATE TABLE am_log_access
    (user_id                        NUMBER(10,0) NOT NULL,
    object_id                      NUMBER(5,0) NOT NULL,
    client_addr                    VARCHAR2(50 BYTE),
    access_date                    DATE NOT NULL)
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Indexes for AM_LOG_ACCESS

CREATE INDEX sal_idx_access_date ON am_log_access
  (
    access_date                     ASC,
    user_id                         ASC,
    object_id                       ASC
  )
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  indx
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
NOPARALLEL
LOGGING
/

CREATE INDEX sal_idx_module_id ON am_log_access
  (
    object_id                       ASC,
    access_date                     ASC,
    user_id                         ASC
  )
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  indx
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
NOPARALLEL
LOGGING
/



-- Constraints for AM_LOG_ACCESS

ALTER TABLE am_log_access
ADD CONSTRAINT am_access_log_pk PRIMARY KEY (user_id, object_id, access_date)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/




-- Comments for AM_LOG_ACCESS

COMMENT ON COLUMN am_log_access.access_date IS 'Ngay truy nhap'
/
COMMENT ON COLUMN am_log_access.client_addr IS 'Dia chi IP'
/
COMMENT ON COLUMN am_log_access.object_id IS 'Ma chuc nang'
/
COMMENT ON COLUMN am_log_access.user_id IS 'Ma NSD'
/

-- End of DDL Script for Table ADM_OWNER.AM_LOG_ACCESS

-- Start of DDL Script for Table ADM_OWNER.AM_LOG_OBJECT
-- Generated 26-Jul-2014 10:20:39 from ADM_OWNER@ORCL

CREATE TABLE am_log_object
    (log_id                         NUMBER(10,0) NOT NULL,
    object_name                    VARCHAR2(128 BYTE) NOT NULL,
    user_id                        NUMBER(10,0) NOT NULL,
    log_date                       DATE NOT NULL,
    action_type                    VARCHAR2(5 BYTE) NOT NULL,
    client_addr                    VARCHAR2(50 BYTE),
    object_desc                    VARCHAR2(512 BYTE))
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Indexes for AM_LOG_OBJECT

CREATE INDEX sml_idx_action_type ON am_log_object
  (
    action_type                     ASC,
    object_name                     ASC,
    user_id                         ASC,
    log_date                        ASC
  )
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  indx
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
NOPARALLEL
LOGGING
/

CREATE INDEX sml_idx_log_date ON am_log_object
  (
    log_date                        ASC,
    action_type                     ASC,
    object_name                     ASC,
    user_id                         ASC
  )
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  indx
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
NOPARALLEL
LOGGING
/

CREATE INDEX sml_idx_module ON am_log_object
  (
    object_name                     ASC,
    user_id                         ASC,
    log_date                        ASC,
    action_type                     ASC
  )
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  indx
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
NOPARALLEL
LOGGING
/

CREATE INDEX sml_idx_user ON am_log_object
  (
    user_id                         ASC,
    log_date                        ASC,
    action_type                     ASC,
    object_name                     ASC
  )
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  indx
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
NOPARALLEL
LOGGING
/



-- Constraints for AM_LOG_OBJECT

ALTER TABLE am_log_object
ADD CONSTRAINT am_module_log_pk PRIMARY KEY (log_id)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/




-- Comments for AM_LOG_OBJECT

COMMENT ON COLUMN am_log_object.action_type IS 'Loai tac dong'
/
COMMENT ON COLUMN am_log_object.client_addr IS 'Dia chi IP'
/
COMMENT ON COLUMN am_log_object.log_date IS 'Ngay ghi nhan'
/
COMMENT ON COLUMN am_log_object.log_id IS 'Ma ghi nhan'
/
COMMENT ON COLUMN am_log_object.object_name IS 'Ma chuc nang'
/
COMMENT ON COLUMN am_log_object.user_id IS 'Ma NSD'
/

-- End of DDL Script for Table ADM_OWNER.AM_LOG_OBJECT

-- Start of DDL Script for Table ADM_OWNER.AM_OBJECT
-- Generated 26-Jul-2014 10:20:39 from ADM_OWNER@ORCL

CREATE TABLE am_object
    (object_id                      NUMBER(5,0) NOT NULL,
    parent_id                      NUMBER(5,0),
    obj_type                       VARCHAR2(5 BYTE) NOT NULL,
    name                           VARCHAR2(128 BYTE) NOT NULL,
    description                    VARCHAR2(512 BYTE),
    status                         NUMBER(1,0),
    ord                            NUMBER(5,0),
    path                           VARCHAR2(512 BYTE) NOT NULL)
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Constraints for AM_OBJECT

ALTER TABLE am_object
ADD CONSTRAINT uk_object UNIQUE (path)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/

ALTER TABLE am_object
ADD CONSTRAINT sec_module_pk PRIMARY KEY (object_id)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/

ALTER TABLE am_object
ADD CONSTRAINT sec_module_uk UNIQUE (name)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/



-- Comments for AM_OBJECT

COMMENT ON COLUMN am_object.description IS 'Mo ta chuc nang'
/
COMMENT ON COLUMN am_object.name IS 'Ten chuc nang'
/
COMMENT ON COLUMN am_object.obj_type IS 'Loai chuc nang (G: Nhom chuc nang, M: chuc nang)'
/
COMMENT ON COLUMN am_object.object_id IS 'Ma chuc nang'
/
COMMENT ON COLUMN am_object.parent_id IS 'Ma chuc nang cha'
/
COMMENT ON COLUMN am_object.status IS 'Trang thai chuc nang, 0: Disabled, 1: Enabled, 2: System'
/

-- End of DDL Script for Table ADM_OWNER.AM_OBJECT

-- Start of DDL Script for Table ADM_OWNER.AM_MOBILE_OBJ
-- Generated 3/12/2015 3:41:01 PM from ADM_OWNER@ORCL

CREATE TABLE am_mobile_obj
    (object_id                      NUMBER(10,0) NOT NULL,
    ord                            NUMBER(10,0),
    is_only_mobile                 VARCHAR2(1 BYTE))
  SEGMENT CREATION IMMEDIATE
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Constraints for AM_MOBILE_OBJ

ALTER TABLE am_mobile_obj
ADD CONSTRAINT am_mobile_obj_uk UNIQUE (object_id)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/


-- End of DDL Script for Table ADM_OWNER.AM_MOBILE_OBJ


-- Start of DDL Script for Table ADM_OWNER.AM_OBJECT_RIGHT
-- Generated 26-Jul-2014 10:20:39 from ADM_OWNER@ORCL

CREATE TABLE am_object_right
    (object_id                      NUMBER(5,0) NOT NULL,
    right_code                     VARCHAR2(5 BYTE) NOT NULL,
    access_type                    NUMBER(1,0) NOT NULL)
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Constraints for AM_OBJECT_RIGHT

ALTER TABLE am_object_right
ADD CONSTRAINT am_module_right_pk PRIMARY KEY (object_id, right_code)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/




-- Comments for AM_OBJECT_RIGHT

COMMENT ON TABLE am_object_right IS 'Bang map chuc nang - quyen, luu danh sach cac quyen ma chuc nang cung cap'
/
COMMENT ON COLUMN am_object_right.access_type IS 'Quyen truy cap, 0: Deny, 1: Allow'
/
COMMENT ON COLUMN am_object_right.object_id IS 'Ma chuc nang'
/
COMMENT ON COLUMN am_object_right.right_code IS 'Ma quyen truy cap (S, I, U, D)'
/

-- End of DDL Script for Table ADM_OWNER.AM_OBJECT_RIGHT

-- Start of DDL Script for Table ADM_OWNER.AM_RIGHT
-- Generated 26-Jul-2014 10:20:39 from ADM_OWNER@ORCL

CREATE TABLE am_right
    (code                           VARCHAR2(5 BYTE) NOT NULL,
    name                           VARCHAR2(50 BYTE) NOT NULL)
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Constraints for AM_RIGHT

ALTER TABLE am_right
ADD CONSTRAINT am_right_uk UNIQUE (name)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/

ALTER TABLE am_right
ADD CONSTRAINT am_right_pk PRIMARY KEY (code)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/


-- Comments for AM_RIGHT

COMMENT ON TABLE am_right IS 'Bang danh muc quyen'
/
COMMENT ON COLUMN am_right.code IS 'Ma quyen truy cap (S, I, U, D)'
/
COMMENT ON COLUMN am_right.name IS 'Ten quyen truy cap'
/

-- End of DDL Script for Table ADM_OWNER.AM_RIGHT

-- Start of DDL Script for Table ADM_OWNER.AM_SUB_DATA_HISTORY
-- Generated 26-Jul-2014 10:20:39 from ADM_OWNER@ORCL

CREATE TABLE am_sub_data_history
    (change_id                      NUMBER(10,0) NOT NULL,
    name                           VARCHAR2(50 BYTE) NOT NULL,
    old_value                      VARCHAR2(4000 BYTE),
    new_value                      VARCHAR2(4000 BYTE))
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Indexes for AM_SUB_DATA_HISTORY

CREATE INDEX scl_idx_change_id ON am_sub_data_history
  (
    change_id                       ASC
  )
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  indx
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
NOPARALLEL
LOGGING
/



-- Constraints for AM_SUB_DATA_HISTORY



-- Comments for AM_SUB_DATA_HISTORY

COMMENT ON TABLE am_sub_data_history IS 'Bang lich su thay doi cac truong trong database'
/
COMMENT ON COLUMN am_sub_data_history.change_id IS 'Ma thay doi'
/
COMMENT ON COLUMN am_sub_data_history.name IS 'Ten cot'
/
COMMENT ON COLUMN am_sub_data_history.new_value IS 'Gia tri moi'
/
COMMENT ON COLUMN am_sub_data_history.old_value IS 'Gia tri cu'
/

-- End of DDL Script for Table ADM_OWNER.AM_SUB_DATA_HISTORY

-- Start of DDL Script for Table ADM_OWNER.AM_USER
-- Generated 26-Jul-2014 10:20:39 from ADM_OWNER@ORCL

CREATE TABLE am_user
    (user_id                        NUMBER(10,0) NOT NULL,
    user_name                      VARCHAR2(50 BYTE) NOT NULL,
    password                       VARCHAR2(100 BYTE),
    expire_status                  NUMBER(1,0) NOT NULL,
    full_name                      VARCHAR2(50 BYTE),
    status                         NUMBER(1,0),
    phone                          VARCHAR2(15 BYTE),
    mobile                         VARCHAR2(15 BYTE),
    fax                            VARCHAR2(15 BYTE),
    email                          VARCHAR2(50 BYTE),
    address                        VARCHAR2(512 BYTE),
    modified_password              DATE,
    locked_date                    DATE,
    failure_count                  NUMBER(5,0),
    created_id                     NUMBER(10,0),
    deleted_id                     NUMBER(10,0),
    modified_id                    NUMBER(10,0),
    config                         VARCHAR2(255 BYTE))
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Constraints for AM_USER

ALTER TABLE am_user
ADD CONSTRAINT am_user_uk UNIQUE (user_name)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/

ALTER TABLE am_user
ADD CONSTRAINT am_user_pk PRIMARY KEY (user_id)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/


-- Comments for AM_USER

COMMENT ON COLUMN am_user.address IS 'Dia chi lien he'
/
COMMENT ON COLUMN am_user.email IS 'Dia chi email'
/
COMMENT ON COLUMN am_user.expire_status IS '0: Expired; 1: Not expired yet; 2: Never expire'
/
COMMENT ON COLUMN am_user.failure_count IS 'So lan login loi'
/
COMMENT ON COLUMN am_user.fax IS 'So fax'
/
COMMENT ON COLUMN am_user.full_name IS 'Ten day du'
/
COMMENT ON COLUMN am_user.locked_date IS 'Ngay tam khoa cuoi cung'
/
COMMENT ON COLUMN am_user.mobile IS 'So dien thoai di dong'
/
COMMENT ON COLUMN am_user.modified_password IS 'Ngay doi mat khau cuoi cung'
/
COMMENT ON COLUMN am_user.password IS 'Mat khau'
/
COMMENT ON COLUMN am_user.phone IS 'So dien thoai lien lac'
/
COMMENT ON COLUMN am_user.status IS 'Trang thai cua NSD, 0: Disabled, 1: Enabled, 2: System'
/
COMMENT ON COLUMN am_user.user_id IS 'Ma NSD'
/
COMMENT ON COLUMN am_user.user_name IS 'Ten dang nhap cua NSD'
/

-- End of DDL Script for Table ADM_OWNER.AM_USER

-- Start of DDL Script for Table ADM_OWNER.AM_USER_ACCESS_TIME
-- Generated 26-Jul-2014 10:20:39 from ADM_OWNER@ORCL

CREATE TABLE am_user_access_time
    (user_id                        NUMBER(10,0) NOT NULL,
    at_id                          NUMBER(10,0) NOT NULL)
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Constraints for AM_USER_ACCESS_TIME



ALTER TABLE am_user_access_time
ADD CONSTRAINT am_user_schedule_pk PRIMARY KEY (user_id, at_id)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
/


-- Comments for AM_USER_ACCESS_TIME

COMMENT ON TABLE am_user_access_time IS 'Bang map NSD - lich truy nhap, luu du lieu phan quyen truy nhap cho NSD'
/
COMMENT ON COLUMN am_user_access_time.at_id IS 'Ma lich truy nhap'
/
COMMENT ON COLUMN am_user_access_time.user_id IS 'Ma NSD'
/

-- End of DDL Script for Table ADM_OWNER.AM_USER_ACCESS_TIME

-- Start of DDL Script for Table ADM_OWNER.AM_USER_OBJECT
-- Generated 26-Jul-2014 10:20:39 from ADM_OWNER@ORCL

CREATE TABLE am_user_object
    (user_id                        NUMBER(10,0) NOT NULL,
    object_id                      NUMBER(5,0) NOT NULL,
    right_code                     VARCHAR2(5 BYTE) NOT NULL,
    access_type                    NUMBER(1,0) NOT NULL)
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Constraints for AM_USER_OBJECT




ALTER TABLE am_user_object
ADD CONSTRAINT am_user_module_pk PRIMARY KEY (user_id, object_id, right_code)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/


-- Comments for AM_USER_OBJECT

COMMENT ON TABLE am_user_object IS 'Bang map NSD - chuc nang, luu du lieu phan quyen cua NSD tren chuc nang'
/
COMMENT ON COLUMN am_user_object.access_type IS 'Quyen truy cap, 0: Deny, 1: Allow'
/
COMMENT ON COLUMN am_user_object.object_id IS 'Ma chuc nang'
/
COMMENT ON COLUMN am_user_object.right_code IS 'Ma quyen truy cap (S, I, U, D)'
/
COMMENT ON COLUMN am_user_object.user_id IS 'Ma NSD'
/

-- End of DDL Script for Table ADM_OWNER.AM_USER_OBJECT

-- Foreign Key
ALTER TABLE am_access_time_dtl
ADD CONSTRAINT ssd_ss_fk FOREIGN KEY (at_id)
REFERENCES am_access_time (at_id) ON DELETE CASCADE
/
-- Foreign Key
ALTER TABLE am_addr_dtl
ADD CONSTRAINT sid_si_fk FOREIGN KEY (addr_id)
REFERENCES am_client_addr (addr_id) ON DELETE CASCADE
/
-- Foreign Key
ALTER TABLE am_addr_group
ADD CONSTRAINT sig_si_fk FOREIGN KEY (addr_id)
REFERENCES am_client_addr (addr_id) ON DELETE CASCADE
/
ALTER TABLE am_addr_group
ADD CONSTRAINT sig_sg_fk FOREIGN KEY (group_id)
REFERENCES am_group (group_id) ON DELETE CASCADE
/
-- Foreign Key
ALTER TABLE am_addr_user
ADD CONSTRAINT siu_su_fk FOREIGN KEY (user_id)
REFERENCES am_user (user_id) ON DELETE CASCADE
/
ALTER TABLE am_addr_user
ADD CONSTRAINT siu_si_fk FOREIGN KEY (addr_id)
REFERENCES am_client_addr (addr_id) ON DELETE CASCADE
/
-- Foreign Key
ALTER TABLE am_app_obj
ADD CONSTRAINT fk_apps_obj FOREIGN KEY (app_id)
REFERENCES am_app (app_id)
/
ALTER TABLE am_app_obj
ADD CONSTRAINT pk_apps_obj_1 FOREIGN KEY (obj_id)
REFERENCES am_object (object_id)
/
-- Foreign Key
ALTER TABLE am_data_history
ADD CONSTRAINT stl_sml_fk FOREIGN KEY (log_id)
REFERENCES am_log_object (log_id) ON DELETE CASCADE
/
-- Foreign Key
ALTER TABLE am_group
ADD CONSTRAINT sg_sg_fk FOREIGN KEY (parent_id)
REFERENCES am_group (group_id)
/
-- Foreign Key
ALTER TABLE am_group_access_time
ADD CONSTRAINT sgs_ss_fk FOREIGN KEY (at_id)
REFERENCES am_access_time (at_id) ON DELETE CASCADE
/
ALTER TABLE am_group_access_time
ADD CONSTRAINT sgs_sg_fk FOREIGN KEY (group_id)
REFERENCES am_group (group_id) ON DELETE CASCADE
/
-- Foreign Key
ALTER TABLE am_group_object
ADD CONSTRAINT sgm_sr_fk FOREIGN KEY (right_code)
REFERENCES am_right (code) ON DELETE CASCADE
/
ALTER TABLE am_group_object
ADD CONSTRAINT sgm_sm_fk FOREIGN KEY (object_id)
REFERENCES am_object (object_id) ON DELETE CASCADE
/
ALTER TABLE am_group_object
ADD CONSTRAINT sgm_sg_fk FOREIGN KEY (group_id)
REFERENCES am_group (group_id) ON DELETE CASCADE
/
-- Foreign Key
ALTER TABLE am_group_user
ADD CONSTRAINT sgu_su_fk FOREIGN KEY (user_id)
REFERENCES am_user (user_id) ON DELETE CASCADE
/
ALTER TABLE am_group_user
ADD CONSTRAINT sgu_sg_fk FOREIGN KEY (group_id)
REFERENCES am_group (group_id)
/
-- Foreign Key
ALTER TABLE am_log_access
ADD CONSTRAINT sal_su_fk FOREIGN KEY (user_id)
REFERENCES am_user (user_id) ON DELETE CASCADE
/
ALTER TABLE am_log_access
ADD CONSTRAINT sal_sm_fk FOREIGN KEY (object_id)
REFERENCES am_object (object_id) ON DELETE CASCADE
/
-- Foreign Key
ALTER TABLE am_log_object
ADD CONSTRAINT sml_su_fk FOREIGN KEY (user_id)
REFERENCES am_user (user_id) ON DELETE CASCADE
/
ALTER TABLE am_log_object
ADD CONSTRAINT sml_sr_fk FOREIGN KEY (action_type)
REFERENCES am_right (code) ON DELETE CASCADE
/
-- Foreign Key
ALTER TABLE am_object
ADD CONSTRAINT sm_sm_fk FOREIGN KEY (parent_id)
REFERENCES am_object (object_id)
/
-- Foreign Key
ALTER TABLE am_object_right
ADD CONSTRAINT smr_sr_fk FOREIGN KEY (right_code)
REFERENCES am_right (code) ON DELETE CASCADE
/
ALTER TABLE am_object_right
ADD CONSTRAINT smr_sm_fk FOREIGN KEY (object_id)
REFERENCES am_object (object_id) ON DELETE CASCADE
/
-- Foreign Key
ALTER TABLE am_sub_data_history
ADD CONSTRAINT scl_stl_fk FOREIGN KEY (change_id)
REFERENCES am_data_history (change_id) ON DELETE CASCADE
/
-- Foreign Key
ALTER TABLE am_user_access_time
ADD CONSTRAINT sus_su_fk FOREIGN KEY (user_id)
REFERENCES am_user (user_id) ON DELETE CASCADE
/
ALTER TABLE am_user_access_time
ADD CONSTRAINT sus_ss_fk FOREIGN KEY (at_id)
REFERENCES am_access_time (at_id) ON DELETE CASCADE
/
-- Foreign Key
ALTER TABLE am_user_object
ADD CONSTRAINT sum_su_fk FOREIGN KEY (user_id)
REFERENCES am_user (user_id) ON DELETE CASCADE
/
ALTER TABLE am_user_object
ADD CONSTRAINT sum_sr_fk FOREIGN KEY (right_code)
REFERENCES am_right (code) ON DELETE CASCADE
/
ALTER TABLE am_user_object
ADD CONSTRAINT sum_sm_fk FOREIGN KEY (object_id)
REFERENCES am_object (object_id) ON DELETE CASCADE
/
-- End of DDL script for Foreign Key(s)


-- AM_OBJECT
INSERT INTO am_object 
VALUES(0,NULL,'G','root',NULL,1,1,'/');
INSERT INTO am_object 
VALUES(1,0,'G','He thong',NULL,1,1,'/system');
INSERT INTO am_object 
VALUES(2,0,'G','Danh muc',NULL,1,2,'/danhmuc');
INSERT INTO am_object 
VALUES(3,0,'G','Bao cao',NULL,1,3,'/baocao');
INSERT INTO am_object 
VALUES(4,1,'M','Quan ly chuc nang he thong',NULL,1,1,'/admin/mngmodule');
INSERT INTO am_object 
VALUES(5,1,'M','Quan ly chuc nang mobile',NULL,1,1,'/admin/mngmmodule');
INSERT INTO am_object 
VALUES(6,1,'M','Quan ly file properties',NULL,1,7,'/admin/filepp');
INSERT INTO am_object 
VALUES(7,1,'M','Theo doi log he thong',NULL,1,4,'/admin/systemlog');
INSERT INTO am_object 
VALUES(8,1,'M','Quan ly NSD & Nhom NSD',NULL,1,2,'/admin/mnggroup');
INSERT INTO am_object 
VALUES(9,1,'M','Quan ly chinh sach',NULL,1,5,'/admin/policy');
INSERT INTO am_object 
VALUES(10,1,'M','Theo doi thay doi',NULL,1,5,'/admin/logchange');
INSERT INTO am_object 
VALUES(11,1,'M','Theo doi truy cap',NULL,1,6,'/admin/logaccess');
INSERT INTO am_object 
VALUES(12,1,'M','Quan ly ung dung',NULL,1,3,'/admin/mngapp');

-- AM_RIGHT
INSERT INTO am_right 
VALUES('D','Delete');
INSERT INTO am_right 
VALUES('I','Insert');
INSERT INTO am_right 
VALUES('S','Search');
INSERT INTO am_right 
VALUES('U','Update');

--
INSERT INTO am_user 
VALUES(1,'ADMIN','sNSQNFVuF8PlrIreWn4i8OywyL4=',2,'Admin',1,NULL,NULL,NULL,NULL,NULL,TO_DATE('2012-10-10 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),NULL,NULL,NULL,NULL,1,'sidebar=1;key=value;');

--
INSERT INTO am_global_property 
VALUES('MINIMUM_PASSWORD_LENGTH',NULL);
INSERT INTO am_global_property 
VALUES('PASSWORD_EXPIRE_DURATION',NULL);
INSERT INTO am_global_property 
VALUES('MAX_OPEN_SESSION_USER',NULL);
INSERT INTO am_global_property 
VALUES('MAX_LOGIN_FAILURE',NULL);
INSERT INTO am_global_property 
VALUES('REQUIRE_STRONG_PASSWORD',NULL);

--
INSERT INTO am_group 
VALUES(0,'root','root',1,NULL);
INSERT INTO am_group 
VALUES(11,'User','User',1,0);
INSERT INTO am_group 
VALUES(12,'VNPT',NULL,1,11);
INSERT INTO am_group 
VALUES(13,'VNP',NULL,1,12);
INSERT INTO am_group 
VALUES(14,'VMS',NULL,1,12);

--
INSERT INTO am_group_user 
VALUES(11,1);
INSERT INTO am_group_user 
VALUES(11,2);

--
INSERT INTO am_object_right 
VALUES(1,'S',1);
INSERT INTO am_object_right 
VALUES(2,'S',1);
INSERT INTO am_object_right 
VALUES(3,'S',1);
INSERT INTO am_object_right 
VALUES(4,'D',1);
INSERT INTO am_object_right 
VALUES(4,'I',1);
INSERT INTO am_object_right 
VALUES(4,'S',1);
INSERT INTO am_object_right 
VALUES(4,'U',1);
INSERT INTO am_object_right 
VALUES(5,'D',1);
INSERT INTO am_object_right 
VALUES(5,'I',1);
INSERT INTO am_object_right 
VALUES(5,'S',1);
INSERT INTO am_object_right 
VALUES(5,'U',1);
INSERT INTO am_object_right 
VALUES(6,'I',1);
INSERT INTO am_object_right 
VALUES(6,'S',1);
INSERT INTO am_object_right 
VALUES(6,'U',1);
INSERT INTO am_object_right 
VALUES(6,'D',1);
INSERT INTO am_object_right 
VALUES(7,'D',1);
INSERT INTO am_object_right 
VALUES(7,'I',1);
INSERT INTO am_object_right 
VALUES(7,'S',1);
INSERT INTO am_object_right 
VALUES(7,'U',1);
INSERT INTO am_object_right 
VALUES(8,'D',1);
INSERT INTO am_object_right 
VALUES(8,'I',1);
INSERT INTO am_object_right 
VALUES(8,'S',1);
INSERT INTO am_object_right 
VALUES(8,'U',1);
INSERT INTO am_object_right 
VALUES(9,'D',1);
INSERT INTO am_object_right 
VALUES(9,'I',1);
INSERT INTO am_object_right 
VALUES(9,'S',1);
INSERT INTO am_object_right 
VALUES(9,'U',1);
INSERT INTO am_object_right 
VALUES(10,'D',1);
INSERT INTO am_object_right 
VALUES(10,'I',1);
INSERT INTO am_object_right 
VALUES(10,'S',1);
INSERT INTO am_object_right 
VALUES(10,'U',1);
INSERT INTO am_object_right 
VALUES(11,'D',1);
INSERT INTO am_object_right 
VALUES(11,'I',1);
INSERT INTO am_object_right 
VALUES(11,'S',1);
INSERT INTO am_object_right 
VALUES(11,'U',1);
INSERT INTO am_object_right 
VALUES(12,'D',1);
INSERT INTO am_object_right 
VALUES(12,'I',1);
INSERT INTO am_object_right 
VALUES(12,'S',1);
INSERT INTO am_object_right 
VALUES(12,'U',1);

--
INSERT INTO am_app 
VALUES(1,'SYS','He thong',1,1);
INSERT INTO am_app 
VALUES(2,'APP_1','Ung dung 1',1,2);
INSERT INTO am_app 
VALUES(3,'APP_2','Ung dung 2',1,2);

--
INSERT INTO am_app_obj 
VALUES(1,1);
INSERT INTO am_app_obj 
VALUES(1,2);
INSERT INTO am_app_obj 
VALUES(1,3);
INSERT INTO am_app_obj 
VALUES(1,4);
INSERT INTO am_app_obj 
VALUES(1,5);
INSERT INTO am_app_obj 
VALUES(1,6);
INSERT INTO am_app_obj 
VALUES(1,7);
INSERT INTO am_app_obj 
VALUES(1,8);
INSERT INTO am_app_obj 
VALUES(1,9);
INSERT INTO am_app_obj 
VALUES(1,10);
INSERT INTO am_app_obj 
VALUES(1,11);
INSERT INTO am_app_obj 
VALUES(1,12);

-- Generated 26/03/2020 16:21:27 from ADMINV2@TELSOFT

CREATE TABLE am_report
    (report_id                      NUMBER(10,0) NOT NULL,
    report_code                    VARCHAR2(50 BYTE) NOT NULL,
    object_id                      NUMBER(10,0) NOT NULL,
    create_date                    DATE,
    update_date                    DATE,
    status                         VARCHAR2(1 BYTE) NOT NULL,
    procedure                      VARCHAR2(50 BYTE))
  SEGMENT CREATION IMMEDIATE
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
  NOCACHE
  MONITORING
  NOPARALLEL
  LOGGING
/





-- Constraints for AM_REPORT


ALTER TABLE am_report
ADD CONSTRAINT am_report_uk UNIQUE (report_code)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/

ALTER TABLE am_report
ADD CONSTRAINT am_report_pk PRIMARY KEY (report_id)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/


-- End of DDL Script for Table ADMINV2.AM_REPORT

-- Foreign Key
ALTER TABLE am_report
ADD CONSTRAINT am_report_fk FOREIGN KEY (object_id)
REFERENCES am_object (object_id)
/
-- End of DDL script for Foreign Key(s)


-- Start of DDL Script for Table ADMINV2.AM_REPORT_FILE
-- Generated 26/03/2020 16:21:35 from ADMINV2@TELSOFT

CREATE TABLE am_report_file
    (file_id                        NUMBER(10,0) NOT NULL,
    report_id                      NUMBER(10,0) NOT NULL,
    template_file                  BLOB NOT NULL,
    hash                           VARCHAR2(50 BYTE) NOT NULL,
    log_time                       DATE DEFAULT sysdate NOT NULL,
    version                        NUMBER(2,1) DEFAULT 0 NOT NULL,
    status                         VARCHAR2(1 BYTE) NOT NULL)
  SEGMENT CREATION IMMEDIATE
  PCTFREE     10
  INITRANS    1
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
  NOCACHE
  MONITORING
  LOB ("TEMPLATE_FILE") STORE AS SYS_LOB0000698215C00003$$
  (
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
   NOCACHE LOGGING
   CHUNK 8192
  )
  NOPARALLEL
  LOGGING
/





-- Constraints for AM_REPORT_FILE


ALTER TABLE am_report_file
ADD CONSTRAINT am_report_file_pk PRIMARY KEY (file_id)
USING INDEX
  PCTFREE     10
  INITRANS    2
  MAXTRANS    255
  TABLESPACE  data
  STORAGE   (
    INITIAL     65536
    NEXT        1048576
    MINEXTENTS  1
    MAXEXTENTS  2147483645
  )
/


-- End of DDL Script for Table ADMINV2.AM_REPORT_FILE

-- Foreign Key
ALTER TABLE am_report_file
ADD CONSTRAINT am_report_file_fk FOREIGN KEY (report_id)
REFERENCES am_report (report_id)
/
-- End of DDL script for Foreign Key(s)


/*  Update to v8 */
ALTER TABLE am_object
    ADD is_render VARCHAR2 (1 BYTE) DEFAULT '1'
    ADD icon VARCHAR2 (50 BYTE);

COMMENT ON COLUMN am_object.is_render IS 'Có hiển thị trên menu hay không';
COMMENT ON COLUMN am_object.icon IS 'Class icon cho menu';

UPDATE am_object SET is_render = '1';