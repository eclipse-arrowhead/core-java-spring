USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'mscv'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_*` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gatekeeper_relay` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gateway_relay` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`relay` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_intra_cloud` TO 'mscv'@'localhost';

REVOKE ALL, GRANT OPTION FROM 'mscv'@'%';

GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_*` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gatekeeper_relay` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gateway_relay` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`relay` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_intra_cloud` TO 'mscv'@'%';

FLUSH PRIVILEGES;