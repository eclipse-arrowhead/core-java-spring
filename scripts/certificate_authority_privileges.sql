USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'certificate_authority'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'certificate_authority'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_definition` TO 'certificate_authority'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_interface` TO 'certificate_authority'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`certificate_authority` TO 'certificate_authority'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`certificate_authority_interface_connection` TO 'certificate_authority'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud` TO 'certificate_authority'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`relay` TO 'certificate_authority'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gatekeeper_relay` TO 'certificate_authority'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gateway_relay` TO 'certificate_authority'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_intra_cloud` TO 'certificate_authority'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_intra_cloud_interface_connection` TO 'certificate_authority'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud` TO 'certificate_authority'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud_interface_connection` TO 'certificate_authority'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`orchestrator_store` TO 'certificate_authority'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`subscription` TO 'certificate_authority'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`subscription_publisher_connection` TO 'certificate_authority'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'certificate_authority'@'localhost';

REVOKE ALL, GRANT OPTION FROM 'certificate_authority'@'%';

GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'certificate_authority'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_definition` TO 'certificate_authority'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_interface` TO 'certificate_authority'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`certificate_authority` TO 'certificate_authority'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`certificate_authority_interface_connection` TO 'certificate_authority'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud` TO 'certificate_authority'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`relay` TO 'certificate_authority'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gatekeeper_relay` TO 'certificate_authority'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gateway_relay` TO 'certificate_authority'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_intra_cloud` TO 'certificate_authority'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_intra_cloud_interface_connection` TO 'certificate_authority'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud` TO 'certificate_authority'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud_interface_connection` TO 'certificate_authority'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`orchestrator_store` TO 'certificate_authority'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`subscription` TO 'certificate_authority'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`subscription_publisher_connection` TO 'certificate_authority'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'certificate_authority'@'%';

FLUSH PRIVILEGES;
