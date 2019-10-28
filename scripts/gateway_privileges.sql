USE `arrowhead`;

GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'gateway'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'gateway'@'%';

FLUSH PRIVILEGES;
