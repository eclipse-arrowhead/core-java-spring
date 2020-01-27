USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'service_registry'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_definition` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_interface` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_registry` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_registry_interface_connection` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`relay` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gatekeeper_relay` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gateway_relay` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_intra_cloud` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_intra_cloud_interface_connection` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud_interface_connection` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`orchestrator_store` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`subscription` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`subscription_publisher_connection` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action_plan` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action_step` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action_plan_action_connection` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action_action_step_connection` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action_step_service_definition_connection` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_next_action_step` TO 'service_registry'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'service_registry'@'localhost';

REVOKE ALL, GRANT OPTION FROM 'service_registry'@'%';

GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_definition` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_interface` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_registry` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_registry_interface_connection` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`relay` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gatekeeper_relay` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`cloud_gateway_relay` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_intra_cloud` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_intra_cloud_interface_connection` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`authorization_inter_cloud_interface_connection` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`orchestrator_store` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`subscription` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`subscription_publisher_connection` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action_plan` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action_step` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action_plan_action_connection` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action_action_step_connection` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action_step_service_definition_connection` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_next_action_step` TO 'service_registry'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'service_registry'@'%';

FLUSH PRIVILEGES;