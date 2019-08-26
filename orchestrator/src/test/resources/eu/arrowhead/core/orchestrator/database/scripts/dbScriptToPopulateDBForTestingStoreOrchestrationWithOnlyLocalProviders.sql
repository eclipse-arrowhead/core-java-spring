-- dbScriptToPopulateDBForTestingStoreOrchestrationWithOnlyLocalProviders
-- load the script after created empty arrowhead schema with create_empty_arrowhead_db
-- and started serviceRegistry, authorization and orchestrator core systems

-- to test:
-- at /orchestrator/orchestration/{id} call with: 3
-- at /orchestrator/orchestration call with:
-- -- {
-- --   "orchestrationFlags": {
-- --     "overrideStore": false
-- --   },
-- -- 
-- --   "requestedService": {
-- --     "interfaceRequirements": [
-- --       "HTTP-SECURE-JSON"
-- --     ],
-- --     "serviceDefinitionRequirement": "testServiceDefinition2"
-- --   },
-- --   "requesterSystem": {
-- --     "address": "localhost",
-- --     "port": 12345,
-- --     "systemName": "sysop"
-- --   }
-- -- }

INSERT INTO `arrowhead`.`system_` (`system_name`, `address`, `port`) VALUES ('sysop', 'localhost', '12345');
INSERT INTO `arrowhead`.`system_` (`system_name`, `address`, `port`) VALUES ('testSystemName3', 'localhost', '12345');
INSERT INTO `arrowhead`.`system_` (`system_name`, `address`, `port`) VALUES ('testSystemName4', 'localhost', '12345');
INSERT INTO `arrowhead`.`system_` (`system_name`, `address`, `port`) VALUES ('testSystemName5', 'localhost', '12345');
INSERT INTO `arrowhead`.`system_` (`system_name`, `address`, `port`) VALUES ('testSystemName6', 'localhost', '12345');


INSERT INTO `arrowhead`.`service_definition` (`service_definition`) VALUES ('testServiceDefinition2');
INSERT INTO `arrowhead`.`service_definition` (`service_definition`) VALUES ('testServiceDefinition3');
INSERT INTO `arrowhead`.`service_definition` (`service_definition`) VALUES ('testServiceDefinition4');
INSERT INTO `arrowhead`.`service_definition` (`service_definition`) VALUES ('testServiceDefinition5');
INSERT INTO `arrowhead`.`service_definition` (`service_definition`) VALUES ('testServiceDefinition6');


INSERT INTO `arrowhead`.`service_registry` (`service_id`, `system_id`, `service_uri`) VALUES ('6', '3', 'testServiceUri');
INSERT INTO `arrowhead`.`service_registry` (`service_id`, `system_id`, `service_uri`) VALUES ('6', '4', 'testServiceUri');
INSERT INTO `arrowhead`.`service_registry` (`service_id`, `system_id`, `service_uri`) VALUES ('6', '5', 'testServiceUri');
INSERT INTO `arrowhead`.`service_registry` (`service_id`, `system_id`, `service_uri`) VALUES ('6', '6', 'testServiceUri');
INSERT INTO `arrowhead`.`service_registry` (`service_id`, `system_id`, `service_uri`) VALUES ('6', '7', 'testServiceUri');


INSERT INTO `arrowhead`.`orchestrator_store` (`consumer_system_id`, `provider_system_id`, `foreign_`, `service_id`, `service_interface_id`, `priority`) VALUES ('3', '4', 0, '6', '1', '1');
INSERT INTO `arrowhead`.`orchestrator_store` (`consumer_system_id`, `provider_system_id`, `foreign_`, `service_id`, `service_interface_id`, `priority`) VALUES ('3', '5', 0, '6', '1', '2');
INSERT INTO `arrowhead`.`orchestrator_store` (`consumer_system_id`, `provider_system_id`, `foreign_`, `service_id`, `service_interface_id`, `priority`) VALUES ('3', '6', 0, '6', '1', '3');
INSERT INTO `arrowhead`.`orchestrator_store` (`consumer_system_id`, `provider_system_id`, `foreign_`, `service_id`, `service_interface_id`, `priority`) VALUES ('3', '7', 0, '6', '1', '4');
INSERT INTO `arrowhead`.`orchestrator_store` (`consumer_system_id`, `provider_system_id`, `foreign_`, `service_id`, `service_interface_id`, `priority`) VALUES ('3', '3', 0, '6', '1', '5');

INSERT INTO `arrowhead`.`service_registry_interface_connection` (`service_registry_id`, `interface_id`) VALUES ('6', '1');
INSERT INTO `arrowhead`.`service_registry_interface_connection` (`service_registry_id`, `interface_id`) VALUES ('7', '1');
INSERT INTO `arrowhead`.`service_registry_interface_connection` (`service_registry_id`, `interface_id`) VALUES ('8', '1');
INSERT INTO `arrowhead`.`service_registry_interface_connection` (`service_registry_id`, `interface_id`) VALUES ('9', '1');
INSERT INTO `arrowhead`.`service_registry_interface_connection` (`service_registry_id`, `interface_id`) VALUES ('10', '1');


INSERT INTO `arrowhead`.`authorization_intra_cloud` (`consumer_system_id`, `provider_system_id`, `service_id`) VALUES ('3', '4', '6');
INSERT INTO `arrowhead`.`authorization_intra_cloud` (`consumer_system_id`, `provider_system_id`, `service_id`) VALUES ('3', '5', '6');
INSERT INTO `arrowhead`.`authorization_intra_cloud` (`consumer_system_id`, `provider_system_id`, `service_id`) VALUES ('3', '6', '6');
INSERT INTO `arrowhead`.`authorization_intra_cloud` (`consumer_system_id`, `provider_system_id`, `service_id`) VALUES ('3', '7', '6');
INSERT INTO `arrowhead`.`authorization_intra_cloud` (`consumer_system_id`, `provider_system_id`, `service_id`) VALUES ('3', '3', '6');

INSERT INTO `arrowhead`.`authorization_intra_cloud_interface_connection` (`authorization_intra_cloud_id`, `interface_id`) VALUES ('1', '1');
INSERT INTO `arrowhead`.`authorization_intra_cloud_interface_connection` (`authorization_intra_cloud_id`, `interface_id`) VALUES ('2', '1');
INSERT INTO `arrowhead`.`authorization_intra_cloud_interface_connection` (`authorization_intra_cloud_id`, `interface_id`) VALUES ('3', '1');
INSERT INTO `arrowhead`.`authorization_intra_cloud_interface_connection` (`authorization_intra_cloud_id`, `interface_id`) VALUES ('4', '1');
INSERT INTO `arrowhead`.`authorization_intra_cloud_interface_connection` (`authorization_intra_cloud_id`, `interface_id`) VALUES ('5', '1');

INSERT INTO `arrowhead`.`orchestrator_store` (`consumer_system_id`, `provider_system_id`, `foreign_`, `service_id`, `service_interface_id`, `priority`) VALUES ('3', '4', 0, '7', '1', '1');
INSERT INTO `arrowhead`.`orchestrator_store` (`consumer_system_id`, `provider_system_id`, `foreign_`, `service_id`, `service_interface_id`, `priority`) VALUES ('3', '5', 0, '7', '1', '2');
INSERT INTO `arrowhead`.`orchestrator_store` (`consumer_system_id`, `provider_system_id`, `foreign_`, `service_id`, `service_interface_id`, `priority`) VALUES ('3', '6', 0, '7', '1', '3');
INSERT INTO `arrowhead`.`orchestrator_store` (`consumer_system_id`, `provider_system_id`, `foreign_`, `service_id`, `service_interface_id`, `priority`) VALUES ('3', '7', 0, '7', '1', '4');
INSERT INTO `arrowhead`.`orchestrator_store` (`consumer_system_id`, `provider_system_id`, `foreign_`, `service_id`, `service_interface_id`, `priority`) VALUES ('3', '3', 0, '7', '1', '5');


INSERT INTO `arrowhead`.`service_registry` (`service_id`, `system_id`, `service_uri`) VALUES ('7', '3', 'testServiceUri');
INSERT INTO `arrowhead`.`service_registry` (`service_id`, `system_id`, `service_uri`) VALUES ('7', '4', 'testServiceUri');
INSERT INTO `arrowhead`.`service_registry` (`service_id`, `system_id`, `service_uri`) VALUES ('7', '5', 'testServiceUri');
INSERT INTO `arrowhead`.`service_registry` (`service_id`, `system_id`, `service_uri`) VALUES ('7', '6', 'testServiceUri');
INSERT INTO `arrowhead`.`service_registry` (`service_id`, `system_id`, `service_uri`) VALUES ('7', '7', 'testServiceUri');


INSERT INTO `arrowhead`.`service_registry_interface_connection` (`service_registry_id`, `interface_id`) VALUES ('11', '1');
INSERT INTO `arrowhead`.`service_registry_interface_connection` (`service_registry_id`, `interface_id`) VALUES ('12', '1');
INSERT INTO `arrowhead`.`service_registry_interface_connection` (`service_registry_id`, `interface_id`) VALUES ('13', '1');
INSERT INTO `arrowhead`.`service_registry_interface_connection` (`service_registry_id`, `interface_id`) VALUES ('14', '1');
INSERT INTO `arrowhead`.`service_registry_interface_connection` (`service_registry_id`, `interface_id`) VALUES ('15', '1');


INSERT INTO `arrowhead`.`authorization_intra_cloud` (`consumer_system_id`, `provider_system_id`, `service_id`) VALUES ('3', '4', '7');
INSERT INTO `arrowhead`.`authorization_intra_cloud` (`consumer_system_id`, `provider_system_id`, `service_id`) VALUES ('3', '5', '7');
INSERT INTO `arrowhead`.`authorization_intra_cloud` (`consumer_system_id`, `provider_system_id`, `service_id`) VALUES ('3', '6', '7');
INSERT INTO `arrowhead`.`authorization_intra_cloud` (`consumer_system_id`, `provider_system_id`, `service_id`) VALUES ('3', '7', '7');
INSERT INTO `arrowhead`.`authorization_intra_cloud` (`consumer_system_id`, `provider_system_id`, `service_id`) VALUES ('3', '3', '7');

INSERT INTO `arrowhead`.`authorization_intra_cloud_interface_connection` (`authorization_intra_cloud_id`, `interface_id`) VALUES ('6', '1');
INSERT INTO `arrowhead`.`authorization_intra_cloud_interface_connection` (`authorization_intra_cloud_id`, `interface_id`) VALUES ('7', '1');
INSERT INTO `arrowhead`.`authorization_intra_cloud_interface_connection` (`authorization_intra_cloud_id`, `interface_id`) VALUES ('8', '1');
INSERT INTO `arrowhead`.`authorization_intra_cloud_interface_connection` (`authorization_intra_cloud_id`, `interface_id`) VALUES ('9', '1');
INSERT INTO `arrowhead`.`authorization_intra_cloud_interface_connection` (`authorization_intra_cloud_id`, `interface_id`) VALUES ('10', '1');