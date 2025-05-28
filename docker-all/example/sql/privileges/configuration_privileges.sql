USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'configuration'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`configuration_data` TO 'configuration'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'configuration'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`configuration_data` TO 'configuration'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'configuration'@'%';

FLUSH PRIVILEGES;
