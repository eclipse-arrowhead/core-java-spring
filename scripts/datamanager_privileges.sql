USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'datamanager'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`dmhist_services` TO 'datamanager'@'localhost';
#GRANT ALL PRIVILEGES ON `arrowhead`.`dmhist_files` TO 'datamanager'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`dmhist_messages` TO 'datamanager'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`dmhist_entries` TO 'datamanager'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'datamanager'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`dmhist_services` TO 'datamanager'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`dmhist_files` TO 'datamanager'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`dmhist_messages` TO 'datamanager'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`dmhist_entries` TO 'datamanager'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'datamanager'@'%';

FLUSH PRIVILEGES;
