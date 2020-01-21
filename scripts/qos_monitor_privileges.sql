USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'qos_monitor'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'qos_monitor'@'localhost';

REVOKE ALL, GRANT OPTION FROM 'qos_monitor'@'%';

GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'qos_monitor'@'%';

FLUSH PRIVILEGES;