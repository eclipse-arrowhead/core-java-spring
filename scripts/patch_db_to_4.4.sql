USE `arrowhead`;

SET SQL_SAFE_UPDATES = 0;
UPDATE `service_registry` SET `service_uri`='' WHERE `service_uri` IS NULL;
ALTER TABLE `service_registry` MODIFY `service_uri` varchar(255) NOT NULL DEFAULT '';
ALTER TABLE `service_registry` DROP FOREIGN KEY `service_registry_service`;
ALTER TABLE `service_registry` DROP FOREIGN KEY `service_registry_system`;
ALTER TABLE `service_registry` DROP INDEX `service_registry_pair`;
ALTER TABLE `service_registry` ADD CONSTRAINT `service_registry_triplet` UNIQUE (`service_id`,`system_id`, `service_uri`);
ALTER TABLE `service_registry` ADD CONSTRAINT `service_registry_service` FOREIGN KEY (`service_id`) REFERENCES `service_definition` (`id`) ON DELETE CASCADE;
ALTER TABLE `service_registry` ADD CONSTRAINT `service_registry_system` FOREIGN KEY (`system_id`) REFERENCES `system_` (`id`) ON DELETE CASCADE;

ALTER TABLE `system_` ADD COLUMN `metadata` mediumtext NULL;
ALTER TABLE `service_registry` MODIFY `metadata` mediumtext NULL;
ALTER TABLE `subscription` MODIFY `filter_meta_data` mediumtext NULL;
ALTER TABLE `foreign_system` ADD COLUMN `metadata` mediumtext NULL;

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
GRANT ALL PRIVILEGES ON `arrowhead`.`orchestrator_store_flexible` TO 'orchestrator'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`orchestrator_store_flexible` TO 'orchestrator'@'%';
FLUSH PRIVILEGES;