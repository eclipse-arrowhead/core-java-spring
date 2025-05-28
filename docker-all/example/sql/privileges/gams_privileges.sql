USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'gams'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_action` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_action_plan` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_processable_action` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_composite_action` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_composite_action_actions` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_event_action` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_logging_action` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_action_plan_actions` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_event` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_http_body_api_call` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_http_url_api_call` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_instance` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_sensor` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_sensor_data` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_sensor_data_double` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_sensor_data_long` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_sensor_data_string` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_aggregation` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_counting_aggregation` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_timeout_guard` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_analysis` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_setpoint_analysis` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_counting_analysis` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_timeout_guard` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_policy` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_match_policy` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_transform_policy` TO 'gams'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_api_policy` TO 'gams'@'localhost';

REVOKE ALL, GRANT OPTION FROM 'gams'@'%';


GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_action` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_action_plan` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_processable_action` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_composite_action` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_composite_action_actions` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_event_action` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_event_action` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_action_plan_actions` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_event` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_http_body_api_call` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_http_url_api_call` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_instance` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_sensor` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_sensor_data` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_sensor_data_double` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_sensor_data_long` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_sensor_data_string` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_aggregation` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_counting_aggregation` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_timeout_guard` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_analysis` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_setpoint_analysis` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_counting_analysis` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_timeout_guard` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_policy` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_match_policy` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_transform_policy` TO 'gams'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`gams_api_policy` TO 'gams'@'%';

FLUSH PRIVILEGES;
