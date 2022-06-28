USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'timemanager'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'timemanager'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'timemanager'@'%';

FLUSH PRIVILEGES;
