USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'gatekeeper'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`cloud` TO 'gatekeeper'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`relay` TO 'gatekeeper'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'gatekeeper'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_definition` TO 'gatekeeper'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_interface` TO 'gatekeeper'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gatekeeper_relay` TO 'gatekeeper'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gateway_relay` TO 'gatekeeper'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud` TO 'gatekeeper'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud_interface_connection` TO 'gatekeeper'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`foreign_system` TO 'gatekeeper'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'gatekeeper'@'localhost';

REVOKE ALL, GRANT OPTION FROM 'gatekeeper'@'%';

GRANT ALL PRIVILEGES ON `arrowhead`.`cloud` TO 'gatekeeper'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`relay` TO 'gatekeeper'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'gatekeeper'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_definition` TO 'gatekeeper'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_interface` TO 'gatekeeper'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gatekeeper_relay` TO 'gatekeeper'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gateway_relay` TO 'gatekeeper'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud` TO 'gatekeeper'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud_interface_connection` TO 'gatekeeper'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`foreign_system` TO 'gatekeeper'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'gatekeeper'@'%';

FLUSH PRIVILEGES;