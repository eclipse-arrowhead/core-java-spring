USE `arrowhead`;

GRANT ALL PRIVILEGES ON `arrowhead`.`cloud` TO 'gatekeeper'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`relay` TO 'gatekeeper'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gatekeeper_relay` TO 'gatekeeper'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gateway_relay` TO 'gatekeeper'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud` TO 'gatekeeper'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud_interface_connections` TO 'gatekeeper'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`foreign_system` TO 'gatekeeper'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'gatekeeper'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`cloud` TO 'gatekeeper'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`relay` TO 'gatekeeper'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gatekeeper_relay` TO 'gatekeeper'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gateway_relay` TO 'gatekeeper'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud` TO 'gatekeeper'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud_interface_connections` TO 'gatekeeper'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`foreign_system` TO 'gatekeeper'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'gatekeeper'@'%';

FLUSH PRIVILEGES;