USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'onboarding_controller'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'onboarding_controller'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'onboarding_controller'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud` TO 'onboarding_controller'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gatekeeper_relay` TO 'onboarding_controller'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gateway_relay` TO 'onboarding_controller'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`relay` TO 'onboarding_controller'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud` TO 'onboarding_controller'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_intra_cloud` TO 'onboarding_controller'@'localhost';

REVOKE ALL, GRANT OPTION FROM 'onboarding_controller'@'%';

GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'onboarding_controller'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'onboarding_controller'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud` TO 'onboarding_controller'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gatekeeper_relay` TO 'onboarding_controller'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gateway_relay` TO 'onboarding_controller'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`relay` TO 'onboarding_controller'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud` TO 'onboarding_controller'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_intra_cloud` TO 'onboarding_controller'@'%';

FLUSH PRIVILEGES;