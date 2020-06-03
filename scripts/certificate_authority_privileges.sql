USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'certificate_authority'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'certificate_authority'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`ca_certificate` TO 'certificate_authority'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`ca_trusted_key` TO 'certificate_authority'@'localhost';

REVOKE ALL, GRANT OPTION FROM 'certificate_authority'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'certificate_authority'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`ca_certificate` TO 'certificate_authority'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`ca_trusted_key` TO 'certificate_authority'@'%';

FLUSH PRIVILEGES;
