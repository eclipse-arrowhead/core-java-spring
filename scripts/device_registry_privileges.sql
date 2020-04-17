USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'device_registry'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`device` TO 'device_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`device_registry` TO 'device_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'system_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'device_registry'@'localhost';

REVOKE ALL, GRANT OPTION FROM 'device_registry'@'%';

GRANT ALL PRIVILEGES ON `arrowhead`.`device` TO 'device_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`device_registry` TO 'device_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'system_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'device_registry'@'%';

FLUSH PRIVILEGES;