INSERT INTO `gams_instance` (`id`, `created_at`, `updated_at`, `uid`, `name`, `delay`, `delay_time_unit`, `owner`, `email`) VALUES
(1, '2021-01-25 11:31:08', '2021-01-25 11:31:08', 0xdf97e03d3fdd49c789bede9e1e6f80ef, 'test', 5, 'SECONDS', 'arrowhead', NULL);

INSERT INTO `gams_sensor` (`id`, `created_at`, `updated_at`, `uid`, `instance_id`, `name`, `address`, `type`, `retention_time`, `time_unit`) VALUES
(1, '2021-01-25 11:31:08', '2021-01-25 11:31:08', 0xe40169e0b5534fe9a4fd90cad9aaf74c, 1, 'Event Sensor', NULL, 'EVENT', 24, 'HOURS'),
(2, '2021-01-25 12:15:18', '2021-01-25 12:15:18', 0x25d1372ef6f9481694d9c79711c402d0, 1, 'MyTemperatureSensor', 'string', 'FLOATING_POINT_NUMBER', 24, 'HOURS');