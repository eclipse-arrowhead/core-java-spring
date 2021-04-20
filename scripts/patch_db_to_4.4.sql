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