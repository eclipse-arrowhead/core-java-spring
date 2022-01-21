USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'ditto'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'ditto'@'localhost';

REVOKE ALL, GRANT OPTION FROM 'ditto'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'ditto'@'%';

FLUSH PRIVILEGES;
