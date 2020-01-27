USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'event_handler'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`event_type` TO 'event_handler'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`subscription` TO 'event_handler'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`subscription_publisher_connection` TO 'event_handler'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'event_handler'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'event_handler'@'localhost';

REVOKE ALL, GRANT OPTION FROM 'event_handler'@'%';

GRANT ALL PRIVILEGES ON `arrowhead`.`event_type` TO 'event_handler'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`subscription` TO 'event_handler'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`subscription_publisher_connection` TO 'event_handler'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`system_` TO 'event_handler'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'event_handler'@'%';

FLUSH PRIVILEGES;