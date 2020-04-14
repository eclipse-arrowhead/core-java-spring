-- MSVC
use arrowhead;

CREATE TABLE IF NOT EXISTS `mscv_target`
(
    `id`   bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `name` varchar(64) UNIQUE NOT NULL,
    `os`   varchar(16)        NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `mscv_ssh_target`
(
    `id`      bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `address` varchar(255) NOT NULL,
    `port`    integer      NOT NULL,
    CONSTRAINT `fk_parent_target` FOREIGN KEY (`id`) REFERENCES `mscv_target` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `u_address_port` (`address`, `port`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `mscv_mip_category`
(
    `id`           bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `name`         varchar(64) UNIQUE NOT NULL,
    `abbreviation` varchar(5) UNIQUE  NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `mscv_mip_domain`
(
    `id`   bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `name` varchar(64) UNIQUE NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `mscv_standard`
(
    `id`             bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `identification` varchar(16) UNIQUE NOT NULL,
    `name`           varchar(64) UNIQUE NOT NULL,
    `description`    text,
    `reference_uri`  varchar(255)       NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `mscv_mip`
(
    `id`          bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `ext_id`      integer(11)        NOT NULL,
    `name`        varchar(64) UNIQUE NOT NULL,
    `description` text,
    `standard_id` bigint             NOT NULL,
    `category_id` bigint             NOT NULL,
    `domain_id`   bigint             NOT NULL,
    CONSTRAINT `fk_standard` FOREIGN KEY (`standard_id`) REFERENCES `mscv_standard` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_category` FOREIGN KEY (`category_id`) REFERENCES `mscv_mip_category` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_domain` FOREIGN KEY (`domain_id`) REFERENCES `mscv_mip_domain` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `u_mip_category_ext_id` (`ext_id`, `category_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `mscv_mip_verification_list`
(
    `id`          bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `name`        varchar(64) UNIQUE NOT NULL,
    `description` varchar(255)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `mscv_mip_verification_entry`
(
    `id`                   bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `mip_id`               bigint   NOT NULL,
    `weight`               smallint NOT NULL,
    `verification_list_id` bigint   NOT NULL,
    CONSTRAINT `fk_entry_mip` FOREIGN KEY (`mip_id`) REFERENCES `mscv_mip` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_entry_verification_list` FOREIGN KEY (`verification_list_id`) REFERENCES `mscv_mip_verification_list` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `u_entry_mip_list` (`mip_id`, `verification_list_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `mscv_script`
(
    `id`     bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `layer`  varchar(16)  NOT NULL,
    `os`     varchar(16)  NOT NULL,
    `path`   varchar(255) NOT NULL,
    `mip_id` bigint       NOT NULL,
    CONSTRAINT `fk_script_mip` FOREIGN KEY (`mip_id`) REFERENCES `mscv_mip` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `u_script_layer_os_mip` (`layer`, `os`, `mip_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `mscv_verification_execution`
(
    `id`                     bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `execution_date`         TIMESTAMP   NOT NULL,
    `result`                 VARCHAR(16) NOT NULL,
    `verification_list_id`   bigint      NOT NULL,
    `verification_target_id` bigint      NOT NULL,
    CONSTRAINT `fk_execution_verification_list` FOREIGN KEY (`verification_list_id`) REFERENCES `mscv_mip_verification_list` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_execution_target` FOREIGN KEY (`verification_target_id`) REFERENCES `mscv_target` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `u_verification_set_target_date` (`verification_list_id`, `verification_target_id`, `execution_date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `mscv_verification_detail`
(
    `id`                    bigint(20) PRIMARY KEY AUTO_INCREMENT,
    `result`                varchar(16) NOT NULL,
    `details`               varchar(255),
    `verification_entry_id` bigint      NOT NULL,
    `execution_id`          bigint      NOT NULL,
    `script_id`             bigint      NOT NULL,
    CONSTRAINT `fk_details_mip_entry` FOREIGN KEY (`verification_entry_id`) REFERENCES `mscv_mip_verification_entry` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_details_execution` FOREIGN KEY (`execution_id`) REFERENCES `mscv_verification_execution` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_details_script` FOREIGN KEY (`script_id`) REFERENCES `mscv_script` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `u_details_execution_mip` (`verification_entry_id`, `execution_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;



