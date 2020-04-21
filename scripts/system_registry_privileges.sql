USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'system_registry'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`device` TO 'system_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'system_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`system_registry` TO 'system_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_definition` TO 'system_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_interface` TO 'system_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_registry` TO 'system_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_registry_interface_connection` TO 'system_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_intra_cloud` TO 'system_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_intra_cloud_interface_connection` TO 'system_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud` TO 'system_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud_interface_connection` TO 'system_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'system_registry'@'localhost';

REVOKE ALL, GRANT OPTION FROM 'system_registry'@'%';

GRANT ALL PRIVILEGES ON `arrowhead`.`device` TO 'system_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'system_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`system_registry` TO 'system_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_definition` TO 'system_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_interface` TO 'system_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_registry` TO 'system_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_registry_interface_connection` TO 'system_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_intra_cloud` TO 'system_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_intra_cloud_interface_connection` TO 'system_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud` TO 'system_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud_interface_connection` TO 'system_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'system_registry'@'%';

FLUSH PRIVILEGES;