USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'device_registry'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`device` TO 'device_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`device_registry` TO 'device_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'device_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`orchestrator_store` TO 'device_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`subscription` TO 'device_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`subscription_publisher_connection` TO 'device_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'device_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud` TO 'device_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gatekeeper_relay` TO 'device_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gateway_relay` TO 'device_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`relay` TO 'device_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud` TO 'device_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_intra_cloud` TO 'device_registry'@'localhost';

REVOKE ALL, GRANT OPTION FROM 'device_registry'@'%';

GRANT ALL PRIVILEGES ON `arrowhead`.`device` TO 'device_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`device_registry` TO 'device_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'device_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`orchestrator_store` TO 'device_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`subscription` TO 'device_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`subscription_publisher_connection` TO 'device_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'device_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud` TO 'device_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gatekeeper_relay` TO 'device_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gateway_relay` TO 'device_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`relay` TO 'device_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud` TO 'device_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_intra_cloud` TO 'device_registry'@'%';

FLUSH PRIVILEGES;