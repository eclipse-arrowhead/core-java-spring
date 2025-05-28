USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'gateway'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'gateway'@'localhost';

REVOKE ALL, GRANT OPTION FROM 'gateway'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'gateway'@'%';

FLUSH PRIVILEGES;
