USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'authorization'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_intra_cloud` TO 'authorization'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_intra_cloud_interface_connection` TO 'authorization'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud` TO 'authorization'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud_interface_connection` TO 'authorization'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'authorization'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud` TO 'authorization'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`relay` TO 'authorization'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gatekeeper_relay` TO 'authorization'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gateway_relay` TO 'authorization'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_definition` TO 'authorization'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_interface` TO 'authorization'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'authorization'@'localhost';

REVOKE ALL, GRANT OPTION FROM 'authorization'@'%';

GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_intra_cloud` TO 'authorization'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_intra_cloud_interface_connection` TO 'authorization'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud` TO 'authorization'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud_interface_connection` TO 'authorization'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'authorization'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud` TO 'authorization'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`relay` TO 'authorization'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gatekeeper_relay` TO 'authorization'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gateway_relay` TO 'authorization'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_definition` TO 'authorization'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_interface` TO 'authorization'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'authorization'@'%';

FLUSH PRIVILEGES;