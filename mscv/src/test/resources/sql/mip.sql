INSERT INTO `mscv_mip_category` (`id`, `name`, `abbreviation`)
VALUES (1, 'IAC', 'IAC'),
       (2, 'ABC', 'ABC');

INSERT INTO `mscv_mip_domain` (`id`, `name`)
VALUES (1, 'SAFETY'),
       (2, 'SECURITY');

INSERT INTO `mscv_standard` (`id`, `identification`, `name`, `description`, `reference_uri`)
VALUES (1, 'BLA-4671-347', 'name', 'description', 'uri');

INSERT INTO `mscv_mip` (`id`, `ext_id`, `name`, `description`, `category_id`, `standard_id`, `domain_id`)
VALUES (1, 1, 'SafIAC', 'Description SafIAC', '1', '1', '1'),
       (2, 2, 'SecIAC', 'Description SecIAC', '1', '1', '2'),
       (3, 1, 'SecABC', 'Description SecABC', '2', '1', '2');



