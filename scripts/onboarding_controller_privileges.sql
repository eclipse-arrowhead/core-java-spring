USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'onboarding_controller'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'onboarding_controller'@'localhost';

REVOKE ALL, GRANT OPTION FROM 'onboarding_controller'@'%';

GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'onboarding_controller'@'%';

FLUSH PRIVILEGES;