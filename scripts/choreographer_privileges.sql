USE `arrowhead`;

GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action_plan` TO 'choreographer'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action` TO 'choreographer'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action_step` TO 'choreographer'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action_plan_action_connection` TO 'choreographer'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action_action_step_connection` TO 'choreographer'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action_step_service_definition_connection` TO 'choreographer'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_next_action_step` TO 'choreographer'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_definition` TO 'choreographer'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'choreographer'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action_plan` TO 'choreographer'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action` TO 'choreographer'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action_step` TO 'choreographer'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action_plan_action_connection` TO 'choreographer'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action_action_step_connection` TO 'choreographer'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_action_step_service_definition_connection` TO 'choreographer'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`choreographer_next_action_step` TO 'choreographer'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`service_definition` TO 'choreographer'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'choreographer'@'%';

FLUSH PRIVILEGES;