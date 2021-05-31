-- MSVC
use arrowhead;

CREATE TABLE IF NOT EXISTS `mscv_target`
(
    `id`   bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `name` varchar(64) UNIQUE NOT NULL,
    `os`   varchar(16)        NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `mscv_ssh_target`
(
    `id`        bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `address`   varchar(255) NOT NULL,
    `port`      integer      NOT NULL,
    `username`   varchar(64),
    `auth_info`  varchar(255),
    CONSTRAINT `fk_parent_target` FOREIGN KEY (`id`) REFERENCES `mscv_target` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `u_address_port` (`address`, `port`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `mscv_mip_category`
(
    `id`           bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `name`         varchar(64) UNIQUE NOT NULL,
    `abbreviation` varchar(5) UNIQUE  NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `mscv_mip_domain`
(
    `id`   bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `name` varchar(64) UNIQUE NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `mscv_standard`
(
    `id`             bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `identification` varchar(16) UNIQUE NOT NULL,
    `name`           varchar(64) UNIQUE NOT NULL,
    `description`    text,
    `reference_uri`  varchar(255)       NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `mscv_mip`
(
    `id`          bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `ext_id`      integer(11)        NOT NULL,
    `name`        varchar(64) UNIQUE NOT NULL,
    `description` text,
    `standard_id` bigint             NOT NULL,
    `category_id` bigint             NOT NULL,
    `domain_id`   bigint             NOT NULL,
    CONSTRAINT `fk_standard` FOREIGN KEY (`standard_id`) REFERENCES `mscv_standard` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_category` FOREIGN KEY (`category_id`) REFERENCES `mscv_mip_category` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_domain` FOREIGN KEY (`domain_id`) REFERENCES `mscv_mip_domain` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `u_mip_category_ext_id` (`ext_id`, `category_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `mscv_mip_verification_list`
(
    `id`                    bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `name`                  varchar(64) NOT NULL,
    `description`           varchar(255),
    `verification_interval` bigint(20)  NOT NULL,
    `layer`                 varchar(16) NOT NULL,
    UNIQUE KEY `u_verification_list_name_layer` (`name`, `layer`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `mscv_mip_verification_entry`
(
    `id`                   bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `mip_id`               bigint   NOT NULL,
    `weight`               smallint NOT NULL,
    `verification_list_id` bigint   NOT NULL,
    CONSTRAINT `fk_entry_mip` FOREIGN KEY (`mip_id`) REFERENCES `mscv_mip` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_entry_verification_list` FOREIGN KEY (`verification_list_id`) REFERENCES `mscv_mip_verification_list` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `u_entry_mip_list` (`mip_id`, `verification_list_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `mscv_script`
(
    `id`     bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `layer`  varchar(16)  NOT NULL,
    `os`     varchar(16)  NOT NULL,
    `path`   varchar(255) NOT NULL,
    `mip_id` bigint       NOT NULL,
    CONSTRAINT `fk_script_mip` FOREIGN KEY (`mip_id`) REFERENCES `mscv_mip` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `u_script_layer_os_mip` (`layer`, `os`, `mip_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `mscv_verification_result`
(
    `id`                     bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `execution_date`         TIMESTAMP   NOT NULL,
    `result`                 VARCHAR(16) NOT NULL,
    `verification_list_id`   bigint      NOT NULL,
    `verification_target_id` bigint      NOT NULL,
    CONSTRAINT `fk_execution_verification_list` FOREIGN KEY (`verification_list_id`) REFERENCES `mscv_mip_verification_list` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_execution_target` FOREIGN KEY (`verification_target_id`) REFERENCES `mscv_target` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `u_verification_set_target_date` (`verification_list_id`, `verification_target_id`, `execution_date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `mscv_verification_detail_result`
(
    `id`                    bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `result`                varchar(16) NOT NULL,
    `details`               varchar(1024),
    `verification_entry_id` bigint      NOT NULL,
    `execution_id`          bigint      NOT NULL,
    `script_id`             bigint,
    CONSTRAINT `fk_details_mip_entry` FOREIGN KEY (`verification_entry_id`) REFERENCES `mscv_mip_verification_entry` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_details_execution` FOREIGN KEY (`execution_id`) REFERENCES mscv_verification_result (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_details_script` FOREIGN KEY (`script_id`) REFERENCES `mscv_script` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `u_details_execution_mip` (`verification_entry_id`, `execution_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `MSCV_QRTZ_JOB_DETAILS`
(
    `SCHED_NAME`        VARCHAR(120) NOT NULL,
    `JOB_NAME`          VARCHAR(200) NOT NULL,
    `JOB_GROUP`         VARCHAR(200) NOT NULL,
    `DESCRIPTION`       VARCHAR(250) NULL,
    `JOB_CLASS_NAME`    VARCHAR(250) NOT NULL,
    `IS_DURABLE`        VARCHAR(1)   NOT NULL,
    `IS_NONCONCURRENT`  VARCHAR(1)   NOT NULL,
    `IS_UPDATE_DATA`    VARCHAR(1)   NOT NULL,
    `REQUESTS_RECOVERY` VARCHAR(1)   NOT NULL,
    `JOB_DATA`          BLOB         NULL,
    PRIMARY KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `MSCV_QRTZ_TRIGGERS`
(
    `SCHED_NAME`     VARCHAR(120) NOT NULL,
    `TRIGGER_NAME`   VARCHAR(200) NOT NULL,
    `TRIGGER_GROUP`  VARCHAR(200) NOT NULL,
    `JOB_NAME`       VARCHAR(200) NOT NULL,
    `JOB_GROUP`      VARCHAR(200) NOT NULL,
    `DESCRIPTION`    VARCHAR(250) NULL,
    `NEXT_FIRE_TIME` BIGINT(13)   NULL,
    `PREV_FIRE_TIME` BIGINT(13)   NULL,
    `PRIORITY`       INTEGER      NULL,
    `TRIGGER_STATE`  VARCHAR(16)  NOT NULL,
    `TRIGGER_TYPE`   VARCHAR(8)   NOT NULL,
    `START_TIME`     BIGINT(13)   NOT NULL,
    `END_TIME`       BIGINT(13)   NULL,
    `CALENDAR_NAME`  VARCHAR(200) NULL,
    `MISFIRE_INSTR`  SMALLINT(2)  NULL,
    `JOB_DATA`       BLOB         NULL,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    FOREIGN KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`)
        REFERENCES `MSCV_QRTZ_JOB_DETAILS` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `MSCV_QRTZ_SIMPLE_TRIGGERS`
(
    `SCHED_NAME`      VARCHAR(120) NOT NULL,
    `TRIGGER_NAME`    VARCHAR(200) NOT NULL,
    `TRIGGER_GROUP`   VARCHAR(200) NOT NULL,
    `REPEAT_COUNT`    BIGINT(7)    NOT NULL,
    `REPEAT_INTERVAL` BIGINT(12)   NOT NULL,
    `TIMES_TRIGGERED` BIGINT(10)   NOT NULL,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
        REFERENCES `MSCV_QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `MSCV_QRTZ_CRON_TRIGGERS`
(
    `SCHED_NAME`      VARCHAR(120) NOT NULL,
    `TRIGGER_NAME`    VARCHAR(200) NOT NULL,
    `TRIGGER_GROUP`   VARCHAR(200) NOT NULL,
    `CRON_EXPRESSION` VARCHAR(200) NOT NULL,
    `TIME_ZONE_ID`    VARCHAR(80),
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
        REFERENCES `MSCV_QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `MSCV_QRTZ_SIMPROP_TRIGGERS`
(
    `SCHED_NAME`    VARCHAR(120)   NOT NULL,
    `TRIGGER_NAME`  VARCHAR(200)   NOT NULL,
    `TRIGGER_GROUP` VARCHAR(200)   NOT NULL,
    `STR_PROP_1`    VARCHAR(512)   NULL,
    `STR_PROP_2`    VARCHAR(512)   NULL,
    `STR_PROP_3`    VARCHAR(512)   NULL,
    `INT_PROP_1`    INT            NULL,
    `INT_PROP_2`    INT            NULL,
    `LONG_PROP_1`   BIGINT         NULL,
    `LONG_PROP_2`   BIGINT         NULL,
    `DEC_PROP_1`    NUMERIC(13, 4) NULL,
    `DEC_PROP_2`    NUMERIC(13, 4) NULL,
    `BOOL_PROP_1`   VARCHAR(1)     NULL,
    `BOOL_PROP_2`   VARCHAR(1)     NULL,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
        REFERENCES `MSCV_QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `MSCV_QRTZ_BLOB_TRIGGERS`
(
    `SCHED_NAME`    VARCHAR(120) NOT NULL,
    `TRIGGER_NAME`  VARCHAR(200) NOT NULL,
    `TRIGGER_GROUP` VARCHAR(200) NOT NULL,
    `BLOB_DATA`     BLOB         NULL,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`),
    FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
        REFERENCES `MSCV_QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `MSCV_QRTZ_CALENDARS`
(
    `SCHED_NAME`    VARCHAR(120) NOT NULL,
    `CALENDAR_NAME` VARCHAR(200) NOT NULL,
    `CALENDAR`      BLOB         NOT NULL,
    PRIMARY KEY (`SCHED_NAME`, `CALENDAR_NAME`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `MSCV_QRTZ_PAUSED_TRIGGER_GRPS`
(
    `SCHED_NAME`    VARCHAR(120) NOT NULL,
    `TRIGGER_GROUP` VARCHAR(200) NOT NULL,
    PRIMARY KEY (`SCHED_NAME`, `TRIGGER_GROUP`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `MSCV_QRTZ_FIRED_TRIGGERS`
(
    `SCHED_NAME`        VARCHAR(120) NOT NULL,
    `ENTRY_ID`          VARCHAR(95)  NOT NULL,
    `TRIGGER_NAME`      VARCHAR(200) NOT NULL,
    `TRIGGER_GROUP`     VARCHAR(200) NOT NULL,
    `INSTANCE_NAME`     VARCHAR(200) NOT NULL,
    `FIRED_TIME`        BIGINT(13)   NOT NULL,
    `SCHED_TIME`        BIGINT(13)   NOT NULL,
    `PRIORITY`          INTEGER      NOT NULL,
    `STATE`             VARCHAR(16)  NOT NULL,
    `JOB_NAME`          VARCHAR(200) NULL,
    `JOB_GROUP`         VARCHAR(200) NULL,
    `IS_NONCONCURRENT`  VARCHAR(1)   NULL,
    `REQUESTS_RECOVERY` VARCHAR(1)   NULL,
    PRIMARY KEY (`SCHED_NAME`, `ENTRY_ID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `MSCV_QRTZ_SCHEDULER_STATE`
(
    `SCHED_NAME`        VARCHAR(120) NOT NULL,
    `INSTANCE_NAME`     VARCHAR(200) NOT NULL,
    `LAST_CHECKIN_TIME` BIGINT(13)   NOT NULL,
    `CHECKIN_INTERVAL`  BIGINT(13)   NOT NULL,
    PRIMARY KEY (`SCHED_NAME`, `INSTANCE_NAME`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `MSCV_QRTZ_LOCKS`
(
    `SCHED_NAME` VARCHAR(120) NOT NULL,
    `LOCK_NAME`  VARCHAR(40)  NOT NULL,
    PRIMARY KEY (`SCHED_NAME`, `LOCK_NAME`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
