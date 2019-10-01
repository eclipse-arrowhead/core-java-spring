USE `arrowhead`;

GRANT ALL PRIVILEGES ON `arrowhead`.`cloud` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`relay` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gatekeeper_relay` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gateway_relay` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_registry` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_registry_interface_connection` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_definition` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_interface` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'service_registry'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`cloud` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`relay` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gatekeeper_relay` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gateway_relay` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_registry` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_registry_interface_connection` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_definition` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_interface` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'service_registry'@'%';

FLUSH PRIVILEGES;