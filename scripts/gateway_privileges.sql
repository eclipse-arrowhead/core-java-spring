USE `arrowhead`;

GRANT ALL PRIVILEGES ON `arrowhead`.`cloud` TO 'gateway'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gatekeeper_relay` TO 'gateway'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gateway_relay` TO 'gateway'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`relay` TO 'gateway'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'gateway'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_definition` TO 'gateway'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud` TO 'gateway'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'gateway'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`cloud` TO 'gateway'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gatekeeper_relay` TO 'gateway'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gateway_relay` TO 'gateway'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`relay` TO 'gateway'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'gateway'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_definition` TO 'gateway'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud` TO 'gateway'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'gateway'@'%';

FLUSH PRIVILEGES;
