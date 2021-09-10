-- GAMS
create schema if not exists arrowhead;

use arrowhead;

CREATE TABLE IF NOT EXISTS `gams_instance`
(
    `id`              bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `created_at`      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      TIMESTAMP   NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    `uid`             BINARY(16)  NOT NULL,
    `name`            varchar(32) NOT NULL,
    `delay`           bigint(20)  NOT NULL DEFAULT 0,
    `delay_time_unit` varchar(16) NOT NULL DEFAULT 'SECONDS',
    `owner`           varchar(32),
    `email`           varchar(32)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_sensor`
(
    `id`             bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `created_at`     TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     TIMESTAMP   NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    `uid`            BINARY(16)  NOT NULL,
    `instance_id`    bigint(20)  NOT NULL,
    `name`           varchar(40) NOT NULL,
    `address`        varchar(16),
    `type`           varchar(32) NOT NULL,
    `retention_time` bigint(20)  NOT NULL DEFAULT 24,
    `time_unit`      varchar(16) NOT NULL DEFAULT 'HOURS',
    CONSTRAINT `fk_sensor_instance` FOREIGN KEY (`instance_id`) REFERENCES `gams_instance` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `u_sensor_name` (`instance_id`, `name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_sensor_data`
(
    `id`         bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `created_at` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `valid_till` TIMESTAMP   NOT NULL,
    `state`      varchar(16) NOT NULL,
    `sensor_id`  bigint(20)  NOT NULL,
    `address`    varchar(16),
    CONSTRAINT `fk_sensor_data_sensor` FOREIGN KEY (`sensor_id`) REFERENCES `gams_sensor` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_sensor_data_long`
(
    `id`   bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `data` bigint(20) NOT NULL,
    CONSTRAINT `fk_long_data_parent` FOREIGN KEY (`id`) REFERENCES `gams_sensor_data` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_sensor_data_string`
(
    `id`   bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `data` varchar(64) NOT NULL,
    CONSTRAINT `fk_string_data_parent` FOREIGN KEY (`id`) REFERENCES `gams_sensor_data` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_sensor_data_double`
(
    `id`   bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `data` DOUBLE PRECISION NOT NULL,
    CONSTRAINT `fk_double_data_parent` FOREIGN KEY (`id`) REFERENCES `gams_sensor_data` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_event`
(
    `id`          bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `created_at`  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `valid_till`  TIMESTAMP   NOT NULL,
    `state`       varchar(16) NOT NULL,
    `valid_from`  TIMESTAMP   NOT NULL,
    `sensor_id`   bigint(20)  NOT NULL,
    `phase`       varchar(16) NOT NULL,
    `type`        varchar(16) NOT NULL,
    `source`      varchar(64),
    `data`        varchar(64),
    CONSTRAINT `fk_event_sensor` FOREIGN KEY (`sensor_id`) REFERENCES `gams_sensor` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_aggregation`
(
    `id`          bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `created_at`  TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  TIMESTAMP  NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    `uid`         BINARY(16) NOT NULL,
    `sensor_id`   bigint(20) NOT NULL,
    `type`        varchar(8) NOT NULL,
    `quantity`    bigint(8),
    `validity`    bigint(20),
    `validity_unit` varchar(16),
    CONSTRAINT `fk_aggregation_sensor` FOREIGN KEY (`sensor_id`) REFERENCES `gams_sensor` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_counting_aggregation`
(
    `id`   bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `timescale`    bigint(20),
    `timescale_unit` varchar(16),
    CONSTRAINT `fk_counting_aggregation_parent` FOREIGN KEY (`id`) REFERENCES `gams_aggregation` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


CREATE TABLE IF NOT EXISTS `gams_timeout_guard`
(
    `id`         bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `created_at` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP   NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    `uid`        BINARY(16)  NOT NULL,
    `sensor_id`  bigint(20)  NOT NULL,
    `time_value` bigint(20)  NOT NULL DEFAULT 24,
    `time_unit`  varchar(16) NOT NULL DEFAULT 'HOURS',
    CONSTRAINT `fk_timeout_sensor` FOREIGN KEY (`sensor_id`) REFERENCES `gams_sensor` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_analysis`
(
    `id`   bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    `uid`  BINARY(16) NOT NULL,
    `sensor_id`  bigint(20)  NOT NULL,
    `target_knowledge`  varchar(32)  NOT NULL,
    `type`  varchar(12)  NOT NULL,
    CONSTRAINT `fk_analysis_sensor` FOREIGN KEY (`sensor_id`) REFERENCES `gams_sensor` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_counting_analysis`
(
    `id`   bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `count` int(4)  NOT NULL,
    `time_value` bigint(20)  NOT NULL DEFAULT 24,
    `time_unit`  varchar(16) NOT NULL DEFAULT 'HOURS',
    CONSTRAINT `fk_counting_analysis_parent` FOREIGN KEY (`id`) REFERENCES `gams_analysis` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_setpoint_analysis`
(
    `id`   bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `lower_set_point`  varchar(16) NOT NULL,
    `upper_set_point`  varchar(16),
    `inverse`  bool NOT NULL,
    CONSTRAINT `fk_setpoint_analysis_parent` FOREIGN KEY (`id`) REFERENCES `gams_analysis` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_action`
(
    `id`          bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `created_at`  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  TIMESTAMP   NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    `uid`         BINARY(16)  NOT NULL,
    `instance_id` bigint(20)  NOT NULL,
    `name`        varchar(32) NOT NULL,
    `action_type`        varchar(16) NOT NULL,
    CONSTRAINT `fk_action_instance` FOREIGN KEY (`instance_id`) REFERENCES `gams_instance` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `u_action_name` (`instance_id`, `name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_processable_action`
(
    `id`             bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `knowledge_keys` varchar(128),
    `processors`     varchar(512),
    CONSTRAINT `fk_processable_action_parent` FOREIGN KEY (`id`) REFERENCES `gams_action` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_composite_action`
(
    `id`             bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `composite_type` varchar(16) NOT NULL,
    CONSTRAINT `fk_composite_action_parent` FOREIGN KEY (`id`) REFERENCES `gams_action` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_event_action`
(
    `id`                bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `target_sensor_id`  bigint(20)  NOT NULL,
    `phase`             varchar(16) NOT NULL,
    `event_type`        varchar(16) NOT NULL,
    CONSTRAINT `fk_event_action_parent` FOREIGN KEY (`id`) REFERENCES `gams_action` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_event_action_sensor` FOREIGN KEY (`target_sensor_id`) REFERENCES `gams_sensor` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_logging_action`
(
    `id`                bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `marker`             varchar(16),
    CONSTRAINT `fk_logging_action_parent` FOREIGN KEY (`id`) REFERENCES `gams_action` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_composite_action_actions`
(
    `composite_action_id` bigint(20) NOT NULL,
    `actions_id`      bigint(20) NOT NULL,
    CONSTRAINT `fk_action_plan_id` FOREIGN KEY (`composite_action_id`) REFERENCES `gams_composite_action` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_action_id` FOREIGN KEY (`actions_id`) REFERENCES `gams_action` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_http_url_api_call`
(
    `id`                   bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `service_uri`          varchar(64) NOT NULL,
    `method`               varchar(16) NOT NULL,
    `basic_authentication` varchar(64),
    CONSTRAINT `fk_http_url_api_parent` FOREIGN KEY (`id`) REFERENCES `gams_processable_action` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_http_body_api_call`
(
    `id`   bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `body` varchar(512) NOT NULL,
    CONSTRAINT `fk_http_body_api_parent` FOREIGN KEY (`id`) REFERENCES `gams_http_url_api_call` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_action_plan`
(
    `id`          bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `created_at`  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  TIMESTAMP   NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    `uid`         BINARY(16)  NOT NULL,
    `instance_id` bigint(20)  NOT NULL,
    `name`        varchar(32) NOT NULL,
    `action_id`   bigint(20)  NOT NULL,
    CONSTRAINT `fk_action_plan_instance` FOREIGN KEY (`instance_id`) REFERENCES `gams_instance` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_policy`
(
    `id`   bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    `uid`  BINARY(16) NOT NULL,
    `source_knowledge`  varchar(32)  NOT NULL,
    `target_knowledge`  varchar(32)  NOT NULL,
    `sensor_id`  bigint(20)  NOT NULL,
    `policy_type`        varchar(16) NOT NULL,
    CONSTRAINT `fk_policy_sensor` FOREIGN KEY (`sensor_id`) REFERENCES `gams_sensor` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


CREATE TABLE IF NOT EXISTS `gams_match_policy`
(
    `id`   bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `match_type`        varchar(16) NOT NULL,
    `number`        bigint(20) NOT NULL,
    CONSTRAINT `fk_match_policy_parent` FOREIGN KEY (`id`) REFERENCES `gams_policy` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_transform_policy`
(
    `id`   bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `expression`        varchar(64) NOT NULL,
    `variable`        varchar(16) NOT NULL,
    CONSTRAINT `fk_transform_policy_parent` FOREIGN KEY (`id`) REFERENCES `gams_policy` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_api_policy`
(
    `id`   bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `api_call_id`        bigint(20) NOT NULL,
    CONSTRAINT `fk_api_policy_parent` FOREIGN KEY (`id`) REFERENCES `gams_policy` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_api_policy_api` FOREIGN KEY (`api_call_id`) REFERENCES  `gams_processable_action` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `gams_knowledge`
(
    `id`   bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `instance_id`    bigint(20)  NOT NULL,
    `key_`        varchar(64) NOT NULL,
    `value_`        varchar(64) NOT NULL,
    CONSTRAINT `fk_knowledge_instance` FOREIGN KEY (`instance_id`) REFERENCES `gams_instance` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `u_knowledge_key` (`instance_id`, `key_`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


