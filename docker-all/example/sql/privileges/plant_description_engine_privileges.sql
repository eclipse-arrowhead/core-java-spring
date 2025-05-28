USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'plant_description_engine'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`pde_rule` TO 'plant_description_engine'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`plant_description` TO 'plant_description_engine'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'plant_description_engine'@'localhost';

REVOKE ALL, GRANT OPTION FROM 'plant_description_engine'@'%';

GRANT ALL PRIVILEGES ON `arrowhead`.`pde_rule` TO 'plant_description_engine'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`plant_description` TO 'plant_description_engine'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'plant_description_engine'@'%';

FLUSH PRIVILEGES;
