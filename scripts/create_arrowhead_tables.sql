CREATE DATABASE IF NOT EXISTS `arrowhead`;
USE `arrowhead`;

-- Common

CREATE TABLE IF NOT EXISTS `cloud` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `operator` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `secure` int(1) NOT NULL DEFAULT 0 COMMENT 'Is secure?',
  `neighbor` int(1) NOT NULL DEFAULT 0 COMMENT 'Is neighbor cloud?',
  `own_cloud` int(1) NOT NULL DEFAULT 0 COMMENT 'Is own cloud?',
  `authentication_info` varchar(2047) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `cloud` (`operator`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `relay` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) NOT NULL,
  `port` int(11) NOT NULL,
  `secure` int(1) NOT NULL DEFAULT 0,
  `authentication_info` varchar(2047) DEFAULT NULL,
  `exclusive` int(1) NOT NULL DEFAULT 0,
  `type` varchar(255) NOT NULL DEFAULT 'GENERAL_RELAY',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pair` (`address`, `port`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `cloud_gatekeeper_relay` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cloud_id` bigint(20) NOT NULL,
  `relay_id` bigint(20) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pair` (`cloud_id`,`relay_id`),
  CONSTRAINT `gk_cloud_constr` FOREIGN KEY (`cloud_id`) REFERENCES `cloud` (`id`) ON DELETE CASCADE,
  CONSTRAINT `gk_relay_constr` FOREIGN KEY (`relay_id`) REFERENCES `relay` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `cloud_gateway_relay` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cloud_id` bigint(20) NOT NULL,
  `relay_id` bigint(20) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pair` (`cloud_id`,`relay_id`),
  CONSTRAINT `gw_cloud_constr` FOREIGN KEY (`cloud_id`) REFERENCES `cloud` (`id`) ON DELETE CASCADE,
  CONSTRAINT `gw_relay_constr` FOREIGN KEY (`relay_id`) REFERENCES `relay` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `system_` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `system_name` varchar(255) NOT NULL,
  `address` varchar(255) NOT NULL,
  `address_type` varchar(255) NULL,
  `port` int(11) NOT NULL,
  `authentication_info` varchar(2047) DEFAULT NULL,
  `metadata` mediumtext NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `triple` (`system_name`,`address`,`port`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `device` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `device_name` varchar(255) NOT NULL,
  `address` varchar(255) NOT NULL,
  `mac_address` varchar(255) NOT NULL,
  `authentication_info` varchar(2047) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `double` (`device_name`,`mac_address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `service_definition` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `service_definition` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `service_definition` (`service_definition`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `service_interface` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `interface_name` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `interface` (`interface_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT IGNORE INTO `service_interface` (interface_name) VALUES ('HTTP-SECURE-JSON');
INSERT IGNORE INTO `service_interface` (interface_name) VALUES ('HTTP-INSECURE-JSON');

-- Device Registry

CREATE TABLE IF NOT EXISTS `device_registry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `device_id` bigint(20) NOT NULL,
  `end_of_validity` timestamp NULL DEFAULT NULL,
  `metadata` text,
  `version` int(11) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `device_registry_device` (`device_id`),
  CONSTRAINT `device_registry_device` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- System Registry

CREATE TABLE IF NOT EXISTS `system_registry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `system_id` bigint(20) NOT NULL,
  `device_id` bigint(20) NOT NULL,
  `end_of_validity` timestamp NULL DEFAULT NULL,
  `metadata` text,
  `version` int(11) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `system_registry_pair` (`system_id`,`device_id`),
  KEY `system_registry_device` (`device_id`),
  CONSTRAINT `system_registry_system` FOREIGN KEY (`system_id`) REFERENCES `system_` (`id`) ON DELETE CASCADE,
  CONSTRAINT `system_registry_device` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Service Registry

CREATE TABLE IF NOT EXISTS `service_registry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `service_id` bigint(20) NOT NULL,
  `system_id` bigint(20) NOT NULL,
  `service_uri` varchar(255) NOT NULL DEFAULT '',
  `end_of_validity` timestamp NULL DEFAULT NULL,
  `secure` varchar(255) NOT NULL DEFAULT 'NOT_SECURE',
  `metadata` mediumtext,
  `version` int(11) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `service_registry_triplet` (`service_id`,`system_id`, `service_uri`),
  KEY `service_registry_system` (`system_id`),
  CONSTRAINT `service_registry_service` FOREIGN KEY (`service_id`) REFERENCES `service_definition` (`id`) ON DELETE CASCADE,
  CONSTRAINT `service_registry_system` FOREIGN KEY (`system_id`) REFERENCES `system_` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `service_registry_interface_connection` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `service_registry_id` bigint(20) NOT NULL,
  `interface_id` bigint(20) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pair` (`service_registry_id`,`interface_id`),
  KEY `interface_sr` (`interface_id`),
  CONSTRAINT `interface_sr` FOREIGN KEY (`interface_id`) REFERENCES `service_interface` (`id`) ON DELETE CASCADE,
  CONSTRAINT `service_registry` FOREIGN KEY (`service_registry_id`) REFERENCES `service_registry` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Authorization

CREATE TABLE IF NOT EXISTS `authorization_intra_cloud` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `consumer_system_id` bigint(20) NOT NULL,
  `provider_system_id` bigint(20) NOT NULL,
  `service_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `rule` (`consumer_system_id`,`provider_system_id`,`service_id`),
  KEY `provider` (`provider_system_id`),
  KEY `service_intra_auth` (`service_id`),
  CONSTRAINT `service_intra_auth` FOREIGN KEY (`service_id`) REFERENCES `service_definition` (`id`) ON DELETE CASCADE,
  CONSTRAINT `provider` FOREIGN KEY (`provider_system_id`) REFERENCES `system_` (`id`) ON DELETE CASCADE,
  CONSTRAINT `consumer` FOREIGN KEY (`consumer_system_id`) REFERENCES `system_` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `authorization_inter_cloud` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `consumer_cloud_id` bigint(20) NOT NULL,
  `provider_system_id` bigint(20) NOT NULL,
  `service_id` bigint(20) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `rule` (`consumer_cloud_id`, `provider_system_id`, `service_id`),
  KEY `service_inter_auth` (`service_id`),
  CONSTRAINT `cloud` FOREIGN KEY (`consumer_cloud_id`) REFERENCES `cloud` (`id`) ON DELETE CASCADE,
  CONSTRAINT `service_inter_auth` FOREIGN KEY (`service_id`) REFERENCES `service_definition` (`id`) ON DELETE CASCADE,
  CONSTRAINT `provider_inter_auth` FOREIGN KEY (`provider_system_id`) REFERENCES `system_` (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `authorization_inter_cloud_interface_connection` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `authorization_inter_cloud_id` bigint(20) NOT NULL,
  `interface_id` bigint(20) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pair` (`authorization_inter_cloud_id`,`interface_id`),
  KEY `interface_inter` (`interface_id`),
  CONSTRAINT `auth_inter_interface` FOREIGN KEY (`interface_id`) REFERENCES `service_interface` (`id`) ON DELETE CASCADE,
  CONSTRAINT `auth_inter_cloud` FOREIGN KEY (`authorization_inter_cloud_id`) REFERENCES `authorization_inter_cloud` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `authorization_intra_cloud_interface_connection` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `authorization_intra_cloud_id` bigint(20) NOT NULL,
  `interface_id` bigint(20) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pair` (`authorization_intra_cloud_id`,`interface_id`),
  KEY `interface_intra` (`interface_id`),
  CONSTRAINT `auth_intra_interface` FOREIGN KEY (`interface_id`) REFERENCES `service_interface` (`id`) ON DELETE CASCADE,
  CONSTRAINT `auth_intra_cloud` FOREIGN KEY (`authorization_intra_cloud_id`) REFERENCES `authorization_intra_cloud` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Orchestrator

CREATE TABLE IF NOT EXISTS `orchestrator_store` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `consumer_system_id` bigint(20) NOT NULL,
  `provider_system_id` bigint(20) NOT NULL,
  `foreign_` int(1) NOT NULL DEFAULT 0,
  `service_id` bigint(20) NOT NULL,
  `service_interface_id` bigint(20) NOT NULL,
  `priority` int(11) NOT NULL,
  `attribute` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `priority_rule` (`service_id`, `service_interface_id`, `consumer_system_id`,`priority`),
  UNIQUE KEY `duplication_rule` (`service_id`, `service_interface_id`, `consumer_system_id`,`provider_system_id`, `foreign_`),
  CONSTRAINT `consumer_orch` FOREIGN KEY (`consumer_system_id`) REFERENCES `system_` (`id`) ON DELETE CASCADE,
  CONSTRAINT `service_orch` FOREIGN KEY (`service_id`) REFERENCES `service_definition` (`id`) ON DELETE CASCADE,
  CONSTRAINT `service_intf_orch` FOREIGN KEY (`service_interface_id`) REFERENCES `service_interface` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `foreign_system` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `provider_cloud_id` bigint(20) NOT NULL,
  `system_name` varchar(255) NOT NULL,
  `address` varchar(255) NOT NULL,
  `port` int(11) NOT NULL,
  `authentication_info` varchar(2047) DEFAULT NULL,
  `metadata` mediumtext,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `triple` (`system_name`,`address`,`port`),
  CONSTRAINT `foreign_cloud` FOREIGN KEY (`provider_cloud_id`) REFERENCES `cloud` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `orchestrator_store_flexible` (
`id` bigint(20) NOT NULL AUTO_INCREMENT,
`consumer_system_name` varchar(255),
`provider_system_name` varchar(255),
`consumer_system_metadata` mediumtext,
`provider_system_metadata` mediumtext,
`service_metadata` mediumtext,
`service_interface_name` varchar(255),
`service_definition_name` varchar(255) NOT NULL,
`priority` int(11) NOT NULL DEFAULT 2147483647,
`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Logs

CREATE TABLE IF NOT EXISTS `logs` (
  `log_id` varchar(100) NOT NULL,
  `entry_date` timestamp(3) NULL DEFAULT NULL,
  `logger` varchar(100) DEFAULT NULL,
  `log_level` varchar(100) DEFAULT NULL,
  `system_name` varchar(255) DEFAULT NULL,
  `message` mediumtext,
  `exception` mediumtext,
  PRIMARY KEY (`log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Event Handler

CREATE TABLE IF NOT EXISTS `event_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `event_type_name` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `eventtype` (`event_type_name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `subscription` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `system_id` bigint(20) NOT NULL,
  `event_type_id` bigint(20) NOT NULL,
  `filter_meta_data` mediumtext,
  `match_meta_data` int(1) NOT NULL DEFAULT 0,
  `only_predefined_publishers` int(1) NOT NULL DEFAULT 0,
  `notify_uri` text NOT NULL,
  `start_date` timestamp NULL DEFAULT NULL,
  `end_date` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pair` (`event_type_id`,`system_id`),
  CONSTRAINT `subscriber_system` FOREIGN KEY (`system_id`) REFERENCES `system_` (`id`) ON DELETE CASCADE,
  CONSTRAINT `event_type` FOREIGN KEY (`event_type_id`) REFERENCES `event_type` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `subscription_publisher_connection` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `subscription_id` bigint(20) NOT NULL,
  `system_id` bigint(20) NOT NULL,
  `authorized` int(1) NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pair` (`subscription_id`,`system_id`),
  CONSTRAINT `subscription_constraint` FOREIGN KEY (`subscription_id`) REFERENCES `subscription` (`id`) ON DELETE CASCADE,
  CONSTRAINT `system_constraint` FOREIGN KEY (`system_id`) REFERENCES `system_` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- DataManager

CREATE TABLE IF NOT EXISTS `dmhist_services` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `system_name` varchar(255) NOT NULL,
  `service_name` varchar(255) NOT NULL,
  `service_type` varchar(255),
  last_update timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `dmhist_messages` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sid` bigint(20) NOT NULL,
  `bt` double NOT NULL,
  `mint` double NOT NULL,
  `maxt` double NOT NULL,
  `msg` BLOB NOT NULL,
  `datastored` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `service_id_constr` FOREIGN KEY (`sid`) REFERENCES `dmhist_services` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `dmhist_entries` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sid` bigint(20) NOT NULL,
  `mid` bigint(20) NOT NULL,
  `n` varchar(128) NOT NULL,
  `t` double NOT NULL,
  `u` varchar(64),
  `v`  double,
  `vs` BLOB,
  `vb` BOOLEAN,
  PRIMARY KEY (`id`),
  CONSTRAINT `service_id_fk` FOREIGN KEY(`sid`) REFERENCES `dmhist_services`(`id`) ON DELETE CASCADE,
  CONSTRAINT `message_id_fk` FOREIGN KEY(`mid`) REFERENCES `dmhist_messages`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- TimeManager

-- Choreographer

CREATE TABLE IF NOT EXISTS `choreographer_plan` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `first_action_id` bigint(20),
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `plan_name_unique_key` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `choreographer_action` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `first_action` int(1) NOT NULL DEFAULT 0,
  `plan_id` bigint(20) NOT NULL,
  `next_action_id` bigint(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_plan_id_unique_key` (`name`,`plan_id`),
  CONSTRAINT `next_action` FOREIGN KEY (`next_action_id`) REFERENCES `choreographer_action` (`id`) ON DELETE CASCADE,
  CONSTRAINT `plan` FOREIGN KEY (`plan_id`) REFERENCES `choreographer_plan` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

ALTER TABLE `choreographer_plan` ADD FOREIGN KEY (`first_action_id`) references `choreographer_action`(`id`);

CREATE TABLE IF NOT EXISTS `choreographer_step` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `first_step` int(1) NOT NULL DEFAULT 0,
  `action_id` bigint(20) NOT NULL,
  `service_definition` varchar(255) NOT NULL,
  `min_version` int(11),
  `max_version` int(11),
  `sr_template` mediumtext NOT NULL,
  `static_parameters` mediumtext,
  `quantity` int(20) NOT NULL DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_action_id_unique_key` (`name`, `action_id`),
  CONSTRAINT `action` FOREIGN KEY (`action_id`) REFERENCES `choreographer_action` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `choreographer_step_next_step_connection` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `from_id` bigint(20) NOT NULL,
  `to_id` bigint(20) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `current_step` FOREIGN KEY (`from_id`) REFERENCES choreographer_step (`id`) ON DELETE CASCADE,
  CONSTRAINT `next_step` FOREIGN KEY (`to_id`) REFERENCES choreographer_step (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `choreographer_executor` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `name` varchar(255) NOT NULL,
    `address` varchar(255) NOT NULL,
    `port` int(11) NOT NULL,
    `base_uri` varchar(255) DEFAULT NULL,
	`locked` int(1) NOT NULL DEFAULT 0,
    `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
	UNIQUE KEY `executor_name_unique` (`name`),
	UNIQUE KEY `executor_address_port_uri_unique` (`address`, `port`, `base_uri`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `choreographer_executor_service_definition` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `executor_id` bigint(20) NOT NULL,
    `service_definition` varchar(255) NOT NULL,
	`min_version` int(11) NOT NULL,
	`max_version` int(11) NOT NULL,
    `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY  KEY (`id`),
    CONSTRAINT `fk_executor_id` FOREIGN KEY (`executor_id`) REFERENCES `choreographer_executor` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `unique_executor_service_definition` ( `executor_id`, `service_definition`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `choreographer_session` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `plan_id` bigint(20) NOT NULL,
  `status` varchar(255) NOT NULL,
  `quantity_done` bigint(20) NOT NULL,
  `quantity_goal` bigint(20) NOT NULL,
  `execution_number` bigint(20) NOT NULL,
  `notify_uri` text,
  `started_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `session_plan` FOREIGN KEY (`plan_id`) REFERENCES `choreographer_plan` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `choreographer_session_step` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `session_id` bigint(20) NOT NULL,
  `step_id` bigint(20) NOT NULL,
  `executor_id` bigint(20) NOT NULL,
  `status` varchar(255) NOT NULL,
  `execution_number` bigint(20) NOT NULL,
  `message` mediumtext,
  `started_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `session_step` FOREIGN KEY (`step_id`) REFERENCES `choreographer_step` (`id`) ON DELETE CASCADE,
  CONSTRAINT `session_step_session` FOREIGN KEY (`session_id`) REFERENCES `choreographer_session`(`id`) ON DELETE CASCADE,
  CONSTRAINT `session_step_executor` FOREIGN KEY (`executor_id`) REFERENCES `choreographer_executor` (`id`) ON DELETE CASCADE,
  UNIQUE KEY `session_step_unique` (`session_id`, `step_id`, `execution_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `choreographer_worklog` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `entry_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `plan_name` varchar(255),
  `action_name` varchar(255),
  `step_name` varchar(255),
  `session_id` bigint(20),
  `execution_number` bigint(20),
  `message` mediumtext,
  `exception` mediumtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Configuration
CREATE TABLE IF NOT EXISTS `configuration_data` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `systemName` varchar(255) NOT NULL UNIQUE,
  `fileName` varchar(255) NOT NULL,
  `contentType` varchar(255) NOT NULL,
  `data` blob NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- QoS Monitor
-- Intra

CREATE TABLE IF NOT EXISTS `qos_intra_measurement` (
	`id` bigint(20) PRIMARY KEY AUTO_INCREMENT,
	`system_id` bigint(20) NOT NULL,
	`measurement_type` varchar(255) NOT NULL,
	`last_measurement_at` timestamp NOT NULL,
	`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `fk_system` FOREIGN KEY (`system_id`) REFERENCES `system_` (`id`) ON DELETE CASCADE,
	UNIQUE KEY `unique_system_id_measurement_type` (`system_id`, `measurement_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `qos_intra_ping_measurement` (
	`id` bigint(20) PRIMARY KEY AUTO_INCREMENT,
	`measurement_id` bigint(20) NOT NULL,
	`available` int(1) NOT NULL DEFAULT 0,
	`last_access_at` timestamp NULL DEFAULT NULL,
	`min_response_time` int(11) DEFAULT NULL,
	`max_response_time` int(11) DEFAULT NULL,
	`mean_response_time_with_timeout` int(11) NULL DEFAULT NULL,
	`mean_response_time_without_timeout` int(11) NULL DEFAULT NULL,
	`jitter_with_timeout` int(11) NULL DEFAULT NULL,
	`jitter_without_timeout` int(11) NULL DEFAULT NULL,
	`lost_per_measurement_percent` int(3) NOT NULL DEFAULT 0,
	`sent` bigint(20) NOT NULL DEFAULT 0,
	`received` bigint(20) NOT NULL DEFAULT 0,
	`count_started_at` timestamp NULL,
	`sent_all` bigint(20) NOT NULL DEFAULT 0,
	`received_all` bigint(20) NOT NULL DEFAULT 0,
	`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `fk_intra_measurement` FOREIGN KEY (`measurement_id`) REFERENCES `qos_intra_measurement` (`id`) ON DELETE CASCADE,
	UNIQUE KEY `unique_intra_measurement` (`measurement_id`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `qos_intra_ping_measurement_log` (
	`id` bigint(20) PRIMARY KEY AUTO_INCREMENT,
	`measured_system_address` varchar(255) NOT NULL,
	`available` int(1) NOT NULL DEFAULT 0,
	`min_response_time` int(11) DEFAULT NULL,
	`max_response_time` int(11) DEFAULT NULL,
	`mean_response_time_with_timeout` int(11) NULL DEFAULT NULL,
	`mean_response_time_without_timeout` int(11) NULL DEFAULT NULL,
	`jitter_with_timeout` int(11) NULL DEFAULT NULL,
	`jitter_without_timeout` int(11) NULL DEFAULT NULL,
	`lost_per_measurement_percent` int(3) NOT NULL DEFAULT 0,
	`sent` bigint(20) NOT NULL DEFAULT 0,
	`received` bigint(20) NOT NULL DEFAULT 0,
	`measured_at` timestamp NOT NULL,
	`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `qos_intra_ping_measurement_log_details` (
	`id` bigint(20) PRIMARY KEY AUTO_INCREMENT,
	`measurement_log_id` bigint(20) NOT NULL,
	`measurement_sequenece_number` int(3) NOT NULL,
	`success_flag` int(1) NOT NULL DEFAULT 0,
	`timeout_flag` int(1) NOT NULL DEFAULT 0,
	`error_message` varchar(255) NULL DEFAULT NULL,
	`throwable` varchar(255) NULL DEFAULT NULL,
	`size_` int(11) NULL DEFAULT NULL,
	`rtt` int(11) NULL DEFAULT NULL,
	`ttl` int(3) NULL DEFAULT NULL,
	`duration` int(5) NULL DEFAULT NULL,
	`measured_at` timestamp NOT NULL,
	`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `fk_intra_measurement_log` FOREIGN KEY (`measurement_log_id`) REFERENCES `qos_intra_ping_measurement_log` (`id`) ON DELETE CASCADE

) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- QoS Monitor
-- Inter

CREATE TABLE IF NOT EXISTS `qos_inter_direct_measurement` (
	`id` bigint(20) PRIMARY KEY AUTO_INCREMENT,
	`cloud_id` bigint(20) NOT NULL,
	`address` varchar(255) NOT NULL,
	`measurement_type` varchar(255) NOT NULL,
	`last_measurement_at` timestamp NOT NULL,
	`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `fk_cloud_inter_direct` FOREIGN KEY (`cloud_id`) REFERENCES `cloud` (`id`) ON DELETE CASCADE,
	UNIQUE KEY `unique_cloud_id_address_measurement_type` (`cloud_id`, `address`, `measurement_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `qos_inter_direct_ping_measurement` (
	`id` bigint(20) PRIMARY KEY AUTO_INCREMENT,
	`measurement_id` bigint(20) NOT NULL,
	`available` int(1) NOT NULL DEFAULT 0,
	`last_access_at` timestamp NULL DEFAULT NULL,
	`min_response_time` int(11) DEFAULT NULL,
	`max_response_time` int(11) DEFAULT NULL,
	`mean_response_time_with_timeout` int(11) NULL DEFAULT NULL,
	`mean_response_time_without_timeout` int(11) NULL DEFAULT NULL,
	`jitter_with_timeout` int(11) NULL DEFAULT NULL,
	`jitter_without_timeout` int(11) NULL DEFAULT NULL,
	`lost_per_measurement_percent` int(3) NOT NULL DEFAULT 0,
	`sent` bigint(20) NOT NULL DEFAULT 0,
	`received` bigint(20) NOT NULL DEFAULT 0,
	`count_started_at` timestamp NULL,
	`sent_all` bigint(20) NOT NULL DEFAULT 0,
	`received_all` bigint(20) NOT NULL DEFAULT 0,
	`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `fk_inter_direct_measurement` FOREIGN KEY (`measurement_id`) REFERENCES `qos_inter_direct_measurement` (`id`) ON DELETE CASCADE,
	UNIQUE KEY `unique_measurement` (`measurement_id`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `qos_inter_direct_ping_measurement_log` (
	`id` bigint(20) PRIMARY KEY AUTO_INCREMENT,
	`measured_system_address` varchar(255) NOT NULL,
	`available` int(1) NOT NULL DEFAULT 0,
	`min_response_time` int(11) DEFAULT NULL,
	`max_response_time` int(11) DEFAULT NULL,
	`mean_response_time_with_timeout` int(11) NULL DEFAULT NULL,
	`mean_response_time_without_timeout` int(11) NULL DEFAULT NULL,
	`jitter_with_timeout` int(11) NULL DEFAULT NULL,
	`jitter_without_timeout` int(11) NULL DEFAULT NULL,
	`lost_per_measurement_percent` int(3) NOT NULL DEFAULT 0,
	`sent` bigint(20) NOT NULL DEFAULT 0,
	`received` bigint(20) NOT NULL DEFAULT 0,
	`measured_at` timestamp NOT NULL,
	`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `qos_inter_direct_ping_measurement_log_details` (
	`id` bigint(20) PRIMARY KEY AUTO_INCREMENT,
	`measurement_log_id` bigint(20) NOT NULL,
	`measurement_sequenece_number` int(3) NOT NULL,
	`success_flag` int(1) NOT NULL DEFAULT 0,
	`timeout_flag` int(1) NOT NULL DEFAULT 0,
	`error_message` varchar(255) NULL DEFAULT NULL,
	`throwable` varchar(255) NULL DEFAULT NULL,
	`size_` int(11) NULL DEFAULT NULL,
	`rtt` int(11) NULL DEFAULT NULL,
	`ttl` int(3) NULL DEFAULT NULL,
	`duration` int(5) NULL DEFAULT NULL,
	`measured_at` timestamp NOT NULL,
	`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `fk_inter_direct_ping_measurement_log` FOREIGN KEY (`measurement_log_id`) REFERENCES `qos_inter_direct_ping_measurement_log` (`id`) ON DELETE CASCADE

) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `qos_inter_relay_measurement` (
	`id` bigint(20) PRIMARY KEY AUTO_INCREMENT,
	`cloud_id` bigint(20) NOT NULL,
	`relay_id` bigint(20) NOT NULL,
	`measurement_type` varchar(255) NOT NULL,
	`status` varchar(255) NOT NULL,
	`last_measurement_at` timestamp NOT NULL,
	`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `fk_cloud_inter_relay` FOREIGN KEY (`cloud_id`) REFERENCES `cloud` (`id`) ON DELETE CASCADE,
	CONSTRAINT `fk_relay_inter_relay` FOREIGN KEY (`relay_id`) REFERENCES `relay` (`id`) ON DELETE CASCADE,
	UNIQUE KEY `unique_cloud_relay_measurement_type` (`cloud_id`, `relay_id`, `measurement_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `qos_inter_relay_echo_measurement` (
	`id` bigint(20) PRIMARY KEY AUTO_INCREMENT,
	`measurement_id` bigint(20) NOT NULL,
	`last_access_at` timestamp NULL DEFAULT NULL,
	`min_response_time` int(11) DEFAULT NULL,
	`max_response_time` int(11) DEFAULT NULL,
	`mean_response_time_with_timeout` int(11) NULL DEFAULT NULL,
	`mean_response_time_without_timeout` int(11) NULL DEFAULT NULL,
	`jitter_with_timeout` int(11) NULL DEFAULT NULL,
	`jitter_without_timeout` int(11) NULL DEFAULT NULL,
	`lost_per_measurement_percent` int(3) NOT NULL DEFAULT 0,
	`sent` bigint(20) NOT NULL DEFAULT 0,
	`received` bigint(20) NOT NULL DEFAULT 0,
	`count_started_at` timestamp NULL,
	`sent_all` bigint(20) NOT NULL DEFAULT 0,
	`received_all` bigint(20) NOT NULL DEFAULT 0,
	`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `fk_inter_relay_measurement` FOREIGN KEY (`measurement_id`) REFERENCES `qos_inter_relay_measurement` (`id`) ON DELETE CASCADE,
	UNIQUE KEY `unique_inter_relay_echo_measurement` (`measurement_id`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `qos_inter_relay_echo_measurement_log` (
	`id` bigint(20) PRIMARY KEY AUTO_INCREMENT,
	`measurement_id` bigint(20) NOT NULL,
	`measurement_sequenece_number` int(3) NOT NULL,
	`timeout_flag` int(1) NOT NULL DEFAULT 0,
	`error_message` varchar(255) NULL DEFAULT NULL,
	`throwable` varchar(255) NULL DEFAULT NULL,
	`size_` int(11) NULL DEFAULT NULL,
	`duration` int(5) NULL DEFAULT NULL,
	`measured_at` timestamp NOT NULL,
	`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `fk_inter_relay_echo_measurement_log` FOREIGN KEY (`measurement_id`) REFERENCES `qos_inter_relay_measurement` (`id`) ON DELETE CASCADE

) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- QoS Manager

CREATE TABLE IF NOT EXISTS `qos_reservation` (
	`id` bigint(20) PRIMARY KEY AUTO_INCREMENT,
	`reserved_provider_id` bigint(20) NOT NULL,
	`reserved_service_id` bigint(20) NOT NULL,
	`consumer_system_name` varchar(255) NOT NULL,
	`consumer_address` varchar(255) NOT NULL,
	`consumer_port` int(11) NOT NULL,
	`reserved_to` timestamp NOT NULL,
	`temporary_lock` int(1) NOT NULL DEFAULT 0,
	`created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	CONSTRAINT `fk_reserved_provider` FOREIGN KEY (`reserved_provider_id`) REFERENCES `system_` (`id`) ON DELETE CASCADE,
	CONSTRAINT `fk_reserved_service` FOREIGN KEY (`reserved_service_id`) REFERENCES `service_definition` (`id`) ON DELETE CASCADE,
	UNIQUE KEY `unique_reserved_provider_and_service` (`reserved_provider_id`, `reserved_service_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Certificate Authority

CREATE TABLE IF NOT EXISTS `ca_certificate` (
  `id` bigint(20) PRIMARY KEY AUTO_INCREMENT,
  `common_name` varchar(255) NOT NULL,
  `serial` bigint(20) NOT NULL,
  `created_by` varchar(255) NOT NULL,
  `valid_after` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `valid_before` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `revoked_at` timestamp NULL,
  UNIQUE KEY `unique_certificate_serial` (`serial`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `ca_trusted_key` (
  `id` bigint(20) PRIMARY KEY AUTO_INCREMENT,
  `public_key` text NOT NULL,
  `hash` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL,
  `valid_after` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `valid_before` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `unique_hash` (`hash`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

-- MSVC
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
    `details`               varchar(255),
    `verification_entry_id` bigint      NOT NULL,
    `execution_id`          bigint      NOT NULL,
    `script_id`             bigint      NOT NULL,
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

-- Plant Description Engine

CREATE TABLE IF NOT EXISTS `pde_rule` (
  `id` bigint(20) PRIMARY KEY,
  `plant_description_id` bigint(20) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `plant_description` (
  `id` bigint(20) PRIMARY KEY,
  `plant_description` mediumtext NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8;


-- Generic Autonomic Management System

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
