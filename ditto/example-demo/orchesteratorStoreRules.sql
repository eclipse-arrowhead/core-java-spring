use arrowhead;
select id from system_ where system_name = "ditto";
select id from system_ where system_name = "fan";

SELECT id INTO @thermometer from service_definition where service_definition="thermometer" LIMIT 1;
SELECT id INTO @ditto from system_ where system_name="ditto" LIMIT 1;
SELECT id INTO @fan from system_ where system_name="fan" LIMIT 1;
SELECT id INTO @http_secure_json from service_interface where interface_name = "HTTP-SECURE-JSON";
select id from service_interface where interface_name = "HTTP-SECURE-JSON";

insert into orchestrator_store values (1, @fan, @ditto, 0, @thermometer, @http_secure_json, 1, null, NOW(), NOW());
insert into authorization_intra_cloud values (1, NOW(), NOW(), @fan, @ditto, @thermometer);

SELECT id INTO @thermometer_rule from authorization_intra_cloud where service_id = @thermometer;

insert into authorization_intra_cloud_interface_connection values (1, @thermometer_rule, @http_secure_json, NOW(), NOW());