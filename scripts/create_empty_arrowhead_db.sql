DROP DATABASE IF EXISTS `arrowhead_shared`;
CREATE DATABASE IF NOT EXISTS `arrowhead_shared`;
USE `arrowhead_shared`;

-- Service Registry

DROP TABLE IF EXISTS `service_registry_system`;
CREATE TABLE `service_registry_system` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `system_name` varchar(255) NOT NULL,
  `address` varchar(255) NOT NULL,
  `port` int(11) NOT NULL,
  `authentication_info` varchar(2047) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pair` (`system_name`,`address`,`port`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `service_registry_service_definition`;
CREATE TABLE `service_registry_service_definition` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `service_definition` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `service_definition` (`service_definition`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `service_registry_service_interface`;
CREATE TABLE `service_registry_service_interface` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `interface_name` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `interface` (`interface_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `service_registry`;
CREATE TABLE `service_registry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `service_id` bigint(20) NOT NULL,
  `system_id` bigint(20) NOT NULL,
  `service_uri` varchar(255) DEFAULT NULL,
  `end_of_validity` timestamp NULL DEFAULT NULL,
  `secure` varchar(255) NOT NULL DEFAULT 'NOT_SECURE',
  `metadata` text,
  `version` int(11) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pair` (`service_id`,`system_id`),
  KEY `system` (`system_id`),
  CONSTRAINT `service` FOREIGN KEY (`service_id`) REFERENCES `service_registry_service_definition` (`id`) ON DELETE CASCADE,
  CONSTRAINT `system` FOREIGN KEY (`system_id`) REFERENCES `service_registry_system` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `service_registry_interface_connection`;
CREATE TABLE `service_registry_interface_connection` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `service_registry_id` bigint(20) NOT NULL,
  `interface_id` bigint(20) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pair` (`service_registry_id`,`interface_id`),
  KEY `interface_sr` (`interface_id`),
  CONSTRAINT `interface_sr` FOREIGN KEY (`interface_id`) REFERENCES `service_registry_service_interface` (`id`) ON DELETE CASCADE,
  CONSTRAINT `service_registry` FOREIGN KEY (`service_registry_id`) REFERENCES `service_registry` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Authorization

DROP TABLE IF EXISTS `authorization_system`;
CREATE TABLE `authorization_system` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `system_name` varchar(255) NOT NULL,
  `address` varchar(255) NOT NULL,
  `port` int(11) NOT NULL,
  `authentication_info` varchar(2047) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pair` (`system_name`,`address`,`port`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `authorization_service_definition`;
CREATE TABLE `authorization_service_definition` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `service_definition` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `service_definition` (`service_definition`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `authorization_service_interface`;
CREATE TABLE `authorization_service_interface` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `interface_name` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `interface` (`interface_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `authorization_cloud`;
CREATE TABLE `authorization_cloud` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `operator` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `address` varchar(255) NOT NULL,
  `port` int(11) NOT NULL,
  `gatekeeper_service_uri` varchar(255) NOT NULL,
  `authentication_info` varchar(2047) DEFAULT NULL,
  `secure` int(1) NOT NULL DEFAULT 0 COMMENT 'Is secure?',
  `neighbor` int(1) NOT NULL DEFAULT 0 COMMENT 'Is neighbor cloud?',
  `own_cloud` int(1) NOT NULL DEFAULT 0 COMMENT 'Is own cloud?',
  `created_at` timestamp NOT NULL DEFAULT NOW(),
  `updated_at` timestamp NOT NULL DEFAULT NOW() ON UPDATE NOW(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `cloud` (`operator`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `authorization_intra_cloud`;
CREATE TABLE `authorization_intra_cloud` (
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
  CONSTRAINT `service_intra_auth` FOREIGN KEY (`service_id`) REFERENCES `authorization_service_definition` (`id`) ON DELETE CASCADE,
  CONSTRAINT `provider` FOREIGN KEY (`provider_system_id`) REFERENCES `authorization_system` (`id`) ON DELETE CASCADE,
  CONSTRAINT `consumer` FOREIGN KEY (`consumer_system_id`) REFERENCES `authorization_system` (`id`) ON DELETE CASCADE
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `authorization_inter_cloud`;
CREATE TABLE `authorization_inter_cloud` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `consumer_cloud_id` bigint(20) NOT NULL,
  `provider_system_id` bigint(20) NOT NULL,
  `service_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `rule` (`consumer_cloud_id`,`service_id`),
  KEY `service_inter_auth` (`service_id`),
  CONSTRAINT `cloud` FOREIGN KEY (`consumer_cloud_id`) REFERENCES `authorization_cloud` (`id`) ON DELETE CASCADE,
  CONSTRAINT `service_inter_auth` FOREIGN KEY (`service_id`) REFERENCES `authorization_service_definition` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `authorization_inter_cloud_interface_connection`;
CREATE TABLE `authorization_inter_cloud_interface_connection` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `authorization_inter_cloud_id` bigint(20) NOT NULL,
  `interface_id` bigint(20) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pair` (`authorization_inter_cloud_id`,`interface_id`),
  KEY `interface_inter` (`interface_id`),
  CONSTRAINT `auth_inter_interface` FOREIGN KEY (`interface_id`) REFERENCES `authorization_service_interface` (`id`) ON DELETE CASCADE,
  CONSTRAINT `auth_inter_cloud` FOREIGN KEY (`authorization_inter_cloud_id`) REFERENCES `authorization_inter_cloud` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `authorization_intra_cloud_interface_connection`;
CREATE TABLE `authorization_intra_cloud_interface_connection` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `authorization_intra_cloud_id` bigint(20) NOT NULL,
  `interface_id` bigint(20) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pair` (`authorization_intra_cloud_id`,`interface_id`),
  KEY `interface_intra` (`interface_id`),
  CONSTRAINT `auth_intra_interface` FOREIGN KEY (`interface_id`) REFERENCES `authorization_service_interface` (`id`) ON DELETE CASCADE,
  CONSTRAINT `auth_intra_cloud` FOREIGN KEY (`authorization_intra_cloud_id`) REFERENCES `authorization_intra_cloud` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Relay

DROP TABLE IF EXISTS `relay`;
CREATE TABLE `relay` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) NOT NULL,
  `port` int(11) NOT NULL,
  `secure` int(1) NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT NOW(),
  `updated_at` timestamp NOT NULL DEFAULT NOW() ON UPDATE NOW(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `pair` (`address`, `port`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Orchestrator

DROP TABLE IF EXISTS `orchestrator_system`;
CREATE TABLE `orchestrator_system` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `system_name` varchar(255) NOT NULL,
  `address` varchar(255) NOT NULL,
  `port` int(11) NOT NULL,
  `authentication_info` varchar(2047) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pair` (`system_name`,`address`,`port`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `orchestrator_service_definition`;
CREATE TABLE `orchestrator_service_definition` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `service_definition` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `service_definition` (`service_definition`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `orchestrator_service_interface`;
CREATE TABLE `orchestrator_service_interface` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `interface_name` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `interface` (`interface_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `orchestrator_cloud`;
CREATE TABLE `orchestrator_cloud` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `operator` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `address` varchar(255) NOT NULL,
  `port` int(11) NOT NULL,
  `gatekeeper_service_uri` varchar(255) NOT NULL,
  `authentication_info` varchar(2047) DEFAULT NULL,
  `secure` int(1) NOT NULL DEFAULT 0 COMMENT 'Is secure?',
  `neighbor` int(1) NOT NULL DEFAULT 0 COMMENT 'Is neighbor cloud?',
  `own_cloud` int(1) NOT NULL DEFAULT 0 COMMENT 'Is own cloud?',
  `created_at` timestamp NOT NULL DEFAULT NOW(),
  `updated_at` timestamp NOT NULL DEFAULT NOW() ON UPDATE NOW(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `cloud` (`operator`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `orchestrator_store`;
CREATE TABLE `orchestrator_store` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `consumer_system_id` bigint(20) NOT NULL,
  `provider_cloud_id` bigint(20) DEFAULT NULL,
  `provider_system_id` bigint(20) NOT NULL,
  `service_id` bigint(20) NOT NULL,
  `priority` int(11) DEFAULT NULL,
  `attribute` text,
  `created_at` timestamp NOT NULL DEFAULT NOW(),
  `updated_at` timestamp NOT NULL DEFAULT NOW() ON UPDATE NOW(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique` (`service_id`,`consumer_system_id`,`priority`),
  CONSTRAINT `provider_orch` FOREIGN KEY (`provider_system_id`) REFERENCES `orchestrator_system` (`id`),
  CONSTRAINT `cloud_orch` FOREIGN KEY (`provider_cloud_id`) REFERENCES `orchestrator_cloud` (`id`),
  CONSTRAINT `consumer_orch` FOREIGN KEY (`consumer_system_id`) REFERENCES `orchestrator_system` (`id`),
  CONSTRAINT `service_orch` FOREIGN KEY (`service_id`) REFERENCES `orchestrator_service_definition` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Logs

DROP TABLE IF EXISTS `logs`;
CREATE TABLE `logs` (
  `log_id` varchar(100) NOT NULL,
  `entry_date` timestamp NULL DEFAULT NULL,
  `logger` varchar(100) DEFAULT NULL,
  `log_level` varchar(100) DEFAULT NULL,
  `message` text,
  `exception` text,
  PRIMARY KEY (`log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Event Handler

DROP TABLE IF EXISTS `event_handler_system`;
CREATE TABLE `event_handler_system` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `system_name` varchar(255) NOT NULL,
  `address` varchar(255) NOT NULL,
  `port` int(11) NOT NULL,
  `authentication_info` varchar(2047) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pair` (`system_name`,`address`,`port`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `event_handler_event`;
CREATE TABLE `event_handler_event` (
  `id` bigint(20) AUTO_INCREMENT PRIMARY KEY,
  `type` varchar(255) NOT NULL,
  `provider_system_id` bigint(20) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT NOW(),
  `updated_at` timestamp NOT NULL DEFAULT NOW() ON UPDATE NOW(),
  UNIQUE KEY `pair` (`type`, `provider_system_id`),
  CONSTRAINT `event_provider` FOREIGN KEY (`provider_system_id`) REFERENCES `event_handler_system` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `event_handler_event_subscriber`;
CREATE TABLE `event_handler_event_subscriber` (
  `id` bigint(20) AUTO_INCREMENT PRIMARY KEY,
  `event_id` bigint(20) NOT NULL,
  `consumer_system_id` bigint(20) NOT NULL,
  `notify_uri` varchar(255) DEFAULT NULL,
  `filter_metadata` text,
  `start_date` timestamp NULL DEFAULT NULL,
  `end_date` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT NOW(),
  `updated_at` timestamp NOT NULL DEFAULT NOW() ON UPDATE NOW(),
  CONSTRAINT `event_consumer` FOREIGN KEY (`consumer_system_id`) REFERENCES `event_handler_system` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Choreographer

DROP TABLE IF EXISTS `choreographer_service_definition`;
CREATE TABLE `choreographer_service_definition` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `service_definition` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `service_definition` (`service_definition`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `choreographer_plan`;
CREATE TABLE `choreographer_plan` (
  `id` bigint(20) PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT NOW(),
  `updated_at` timestamp NOT NULL DEFAULT NOW() ON UPDATE NOW()
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `choreographer_plan_step`;
CREATE TABLE `choreographer_plan_step` (
  `id` bigint(20) PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `plan_id` bigint(20) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT NOW(),
  `updated_at` timestamp NOT NULL DEFAULT NOW() ON UPDATE NOW(),
  CONSTRAINT `FKaxgcwgyga50fsi9tyupu19xp` FOREIGN KEY (`plan_id`) REFERENCES `choreographer_plan` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `choreographer_plan_step_service`;
CREATE TABLE `choreographer_plan_step_service` (
  `id` bigint(20) PRIMARY KEY AUTO_INCREMENT,
  `plan_step_id` bigint(20) NOT NULL,
  `service_id` bigint(20) NOT NULL,
  KEY `FK62jllkni532a5e757u1x7jjnw` (`service_id`),
  CONSTRAINT `FK62jllkni532a5e757u1x7jjnw` FOREIGN KEY (`service_id`) REFERENCES `choreographer_service_definition` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FKfvq70kdu6sibc1i3acutbnqp2` FOREIGN KEY (`plan_step_id`) REFERENCES `choreographer_plan_step` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `choreographer_next_step`;
CREATE TABLE `choreographer_next_steps` (
  `id` bigint(20) PRIMARY KEY AUTO_INCREMENT,
  `plan_step_id` bigint(20) NOT NULL,
  `next_step_id` bigint(20) NOT NULL,
  KEY `FKkc9vycv0onpa957jdey7f7rdm` (`next_step_id`),
  CONSTRAINT `FK5x0k6luexx1mmtci9iqmangig` FOREIGN KEY (`plan_step_id`) REFERENCES `choreographer_plan_step` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FKkc9vycv0onpa957jdey7f7rdm` FOREIGN KEY (`next_step_id`) REFERENCES `choreographer_plan_step` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Set up privileges

-- Service Registry
DROP USER IF EXISTS 'service_registry'@'localhost';
CREATE USER 'service_registry'@'localhost' IDENTIFIED BY 'arrowhead';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`service_registry` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`service_registry_interface_connection` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`service_registry_service_definition` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`service_registry_service_interface` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`service_registry_system` TO 'service_registry'@'localhost';

-- Authorizazion
DROP USER IF EXISTS 'authorization'@'localhost';
CREATE USER 'authorization'@'localhost' IDENTIFIED BY 'arrowhead';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`authorization_cloud` TO 'authorization'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`authorization_inter_cloud` TO 'authorization'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`authorization_intra_cloud` TO 'authorization'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`authorization_service_definition` TO 'authorization'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`authorization_service_interface` TO 'authorization'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`authorization_system` TO 'authorization'@'localhost';

-- Orchestrator
DROP USER IF EXISTS 'orchestrator'@'localhost';
CREATE USER 'orchestrator'@'localhost' IDENTIFIED BY 'arrowhead';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`orchestrator_cloud` TO 'orchestrator'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`orchestrator_service_definition` TO 'orchestrator'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`orchestrator_service_interface` TO 'orchestrator'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`orchestrator_store` TO 'orchestrator'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`orchestrator_system` TO 'orchestrator'@'localhost';

-- Event Handler
DROP USER IF EXISTS 'event_handler'@'localhost';
CREATE USER 'event_handler'@'localhost' IDENTIFIED BY 'arrowhead';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`event_handler_event` TO 'event_handler'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`event_handler_event_subscriber` TO 'event_handler'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`event_handler_system` TO 'event_handler'@'localhost';

-- Choreographer
DROP USER IF EXISTS 'choreographer'@'localhost';
CREATE USER 'choreographer'@'localhost' IDENTIFIED BY 'arrowhead';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`choreographer_next_steps` TO 'choreographer'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`choreographer_plan` TO 'choreographer'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`choreographer_plan_step` TO 'choreographer'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`choreographer_plan_step_service` TO 'choreographer'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`choreographer_service_definition` TO 'choreographer'@'localhost';

-- Gatekeeper
DROP USER IF EXISTS 'gatekeeper'@'localhost';
CREATE USER 'gatekeeper'@'localhost' IDENTIFIED BY 'arrowhead';
GRANT ALL PRIVILEGES ON `arrowhead_shared`.`relay` TO 'gatekeeper'@'localhost';
FLUSH PRIVILEGES;
