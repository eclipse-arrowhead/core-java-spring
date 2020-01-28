USE `arrowhead`;
REVOKE ALL, GRANT OPTION FROM 'onboarding'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'onboarding'@'localhost';
REVOKE ALL, GRANT OPTION FROM 'onboarding'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'onboarding'@'%';

FLUSH PRIVILEGES;