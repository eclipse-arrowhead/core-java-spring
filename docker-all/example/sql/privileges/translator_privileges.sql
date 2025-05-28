USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'translator'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'translator'@'localhost';

REVOKE ALL, GRANT OPTION FROM 'translator'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'translator'@'%';

FLUSH PRIVILEGES;