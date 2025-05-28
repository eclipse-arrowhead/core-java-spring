USE `arrowhead`;

REVOKE ALL, GRANT OPTION FROM 'mscv'@'localhost';

GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_target` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_ssh_target` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_mip_category` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_mip_domain` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_standard` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_mip` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_mip_verification_list` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_mip_verification_entry` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_script` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_verification_result` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_verification_result_detail` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`MSCV_QRTZ_JOB_DETAILS` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`MSCV_QRTZ_TRIGGERS` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`MSCV_QRTZ_SIMPLE_TRIGGERS` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`MSCV_QRTZ_CRON_TRIGGERS` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`MSCV_QRTZ_SIMPROP_TRIGGERS` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`MSCV_QRTZ_BLOB_TRIGGERS` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`MSCV_QRTZ_CALENDARS` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`MSCV_QRTZ_PAUSED_TRIGGER_GRPS` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`MSCV_QRTZ_FIRED_TRIGGERS` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`MSCV_QRTZ_SCHEDULER_STATE` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`MSCV_QRTZ_LOCKS` TO 'mscv'@'localhost';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'mscv'@'localhost';

REVOKE ALL, GRANT OPTION FROM 'mscv'@'%';

GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_target` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_ssh_target` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_mip_category` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_mip_domain` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_standard` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_mip` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_mip_verification_list` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_mip_verification_entry` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_script` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_verification_result` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`mscv_verification_result_detail` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`MSCV_QRTZ_JOB_DETAILS` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`MSCV_QRTZ_TRIGGERS` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`MSCV_QRTZ_SIMPLE_TRIGGERS` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`MSCV_QRTZ_CRON_TRIGGERS` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`MSCV_QRTZ_SIMPROP_TRIGGERS` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`MSCV_QRTZ_BLOB_TRIGGERS` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`MSCV_QRTZ_CALENDARS` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`MSCV_QRTZ_PAUSED_TRIGGER_GRPS` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`MSCV_QRTZ_FIRED_TRIGGERS` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`MSCV_QRTZ_SCHEDULER_STATE` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`MSCV_QRTZ_LOCKS` TO 'mscv'@'%';
GRANT ALL PRIVILEGES ON `arrowhead`.`logs` TO 'mscv'@'%';

FLUSH PRIVILEGES;