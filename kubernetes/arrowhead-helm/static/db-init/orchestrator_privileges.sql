USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'orchestrator'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`orchestrator_store` TO 'orchestrator'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`foreign_system` TO 'orchestrator'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`orchestrator_store_flexible` TO 'orchestrator'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'orchestrator'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud` TO 'orchestrator'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`relay` TO 'orchestrator'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gatekeeper_relay` TO 'orchestrator'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gateway_relay` TO 'orchestrator'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_definition` TO 'orchestrator'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_interface` TO 'orchestrator'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`qos_reservation` TO 'orchestrator'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'orchestrator'@'localhost';

REVOKE ALL, GRANT OPTION FROM 'orchestrator'@'%';

GRANT ALL PRIVILEGES ON `arrowhead`.`orchestrator_store` TO 'orchestrator'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`foreign_system` TO 'orchestrator'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`orchestrator_store_flexible` TO 'orchestrator'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'orchestrator'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud` TO 'orchestrator'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`relay` TO 'orchestrator'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gatekeeper_relay` TO 'orchestrator'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gateway_relay` TO 'orchestrator'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_definition` TO 'orchestrator'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_interface` TO 'orchestrator'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`qos_reservation` TO 'orchestrator'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'orchestrator'@'%';

FLUSH PRIVILEGES;