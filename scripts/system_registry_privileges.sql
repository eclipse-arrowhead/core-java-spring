USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'system_registry'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'system_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`system_registry` TO 'system_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`device` TO 'system_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`orchestrator_store` TO 'system_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`subscription` TO 'system_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`subscription_publisher_connection` TO 'system_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'system_registry'@'localhost';

REVOKE ALL, GRANT OPTION FROM 'service_registry'@'%';

GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'system_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`system_registry` TO 'system_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`device` TO 'system_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`orchestrator_store` TO 'system_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`subscription` TO 'system_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`subscription_publisher_connection` TO 'system_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'system_registry'@'%';

FLUSH PRIVILEGES;