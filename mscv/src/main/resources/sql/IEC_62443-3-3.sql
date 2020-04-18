use arrowhead;

INSERT INTO `mscv_mip_category` (`name`, `abbreviation`)
VALUES ('Identification and authentication control', 'IAC'),
       ('Use control', 'UC'),
       ('System integrity', 'SI'),
       ('Data confidentiality', 'DC'),
       ('Restricted data flow', 'RDF'),
       ('Timely response to events', 'TRE'),
       ('Resource availability', 'RA');

INSERT INTO `mscv_mip_domain` (`name`)
VALUES ('SAFETY'),
       ('SECURITY');

INSERT INTO `mscv_standard` (`identification`, `name`, `description`, `reference_uri`)
VALUES ('IEC 62443-3-3',
        'System security requirements and security levels',
        'This Standard establishes a framework for securing information and communication technology aspects of industrial process measurement and control systems including its networks and devices on those networks, during the operational phase of the plant’s life cycle. This Standard provides guidance on a plant’s operational security requirements and is primarily intended for automation system owners/operators (responsible for ICS operation)',
        'https://webstore.iec.ch/preview/info_iecpas62443-3%7Bed1.0%7Den.pdf');

INSERT INTO `mscv_mip` (`ext_id`, `name`, `description`, `category_id`, `standard_id`, `domain_id`)
VALUES (1, 'Human user identification and authentication',
        'This security metric supports identification and authentication control by assuring that human users can be identified and authenticated on all interfaces that allow human access',
        (SELECT id FROM mscv_mip_category WHERE abbreviation = 'IAC'),
        (SELECT id FROM mscv_standard WHERE identification = 'IEC 62443-3-3'),
        (SELECT id FROM mscv_mip_domain WHERE name = 'SECURITY'));

INSERT INTO `mscv_mip_verification_list` (`name`, `layer`, `verification_interval`)
VALUES ('default', 'DEVICE', 300);

INSERT INTO `mscv_mip_verification_entry` (`mip_id`, `weight`, `verification_list_id`)
VALUES ((SELECT id FROM mscv_mip WHERE name = 'Human user identification and authentication'),
        100,
        (SELECT id FROM mscv_mip_verification_list WHERE name = 'default' AND layer = 'DEVICE'));

