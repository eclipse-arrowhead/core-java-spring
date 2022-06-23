DROP DATABASE IF EXISTS `arrowhead`;
CREATE DATABASE `arrowhead`;
USE `arrowhead`;

-- create tables
source create_arrowhead_tables.sql

-- Set up privileges

-- Service Registry
CREATE USER IF NOT EXISTS 'service_registry'@'localhost' IDENTIFIED BY 'ZzNNpxrbZGVvfJ8';
CREATE USER IF NOT EXISTS 'service_registry'@'%' IDENTIFIED BY 'ZzNNpxrbZGVvfJ8';
source service_registry_privileges.sql

-- System Registry
CREATE USER IF NOT EXISTS 'system_registry'@'localhost' IDENTIFIED BY 'Kh12Hhgaxzo7haf';
CREATE USER IF NOT EXISTS 'system_registry'@'%' IDENTIFIED BY 'Kh12Hhgaxzo7haf';
source system_registry_privileges.sql

-- Device Registry
CREATE USER IF NOT EXISTS 'device_registry'@'localhost' IDENTIFIED BY 'iooHU87hNGUalht';
CREATE USER IF NOT EXISTS 'device_registry'@'%' IDENTIFIED BY 'iooHU87hNGUalht';
source device_registry_privileges.sql

-- Onboarding controller
CREATE USER IF NOT EXISTS 'onboarding_controller'@'localhost' IDENTIFIED BY 'JKgh1as5f6oi7aV';
CREATE USER IF NOT EXISTS 'onboarding_controller'@'%' IDENTIFIED BY 'JKgh1as5f6oi7aV';
source onboarding_controller_privileges.sql

-- Authorization
CREATE USER IF NOT EXISTS 'authorization'@'localhost' IDENTIFIED BY 'hqZFUkuHxhekio3';
CREATE USER IF NOT EXISTS 'authorization'@'%' IDENTIFIED BY 'hqZFUkuHxhekio3';
source authorization_privileges.sql

-- Orchestrator
CREATE USER IF NOT EXISTS 'orchestrator'@'localhost' IDENTIFIED BY 'KbgD2mTr8DQ4vtc';
CREATE USER IF NOT EXISTS 'orchestrator'@'%' IDENTIFIED BY 'KbgD2mTr8DQ4vtc';
source orchestrator_privileges.sql

-- Event Handler
CREATE USER IF NOT EXISTS 'event_handler'@'localhost' IDENTIFIED BY 'gRLjXbqu9YwYhfK';
CREATE USER IF NOT EXISTS 'event_handler'@'%' IDENTIFIED BY 'gRLjXbqu9YwYhfK';
source event_handler_privileges.sql

-- DataManager
CREATE USER IF NOT EXISTS 'datamanager'@'localhost' IDENTIFIED BY 'gRLjXbqu0YwYhfK';
CREATE USER IF NOT EXISTS 'datamanager'@'%' IDENTIFIED BY 'gRLjXbqu0YwYhfK';
source datamanager_privileges.sql

-- TimeManager
CREATE USER IF NOT EXISTS 'timemanager'@'localhost' IDENTIFIED BY 'xyp2XEAu5Tbc41g';
CREATE USER IF NOT EXISTS 'timemanager'@'%' IDENTIFIED BY 'xyp2XEAu5Tbc41g';
source timemanager_privileges.sql

-- Choreographer
CREATE USER IF NOT EXISTS 'choreographer'@'localhost' IDENTIFIED BY 'Qa5yx4oBp4Y9RLX';
CREATE USER IF NOT EXISTS 'choreographer'@'%' IDENTIFIED BY 'Qa5yx4oBp4Y9RLX';
source choreographer_privileges.sql

-- Configuration
CREATE USER IF NOT EXISTS 'configuration'@'localhost' IDENTIFIED BY 'yRLjX2qA0YwYhzU';
CREATE USER IF NOT EXISTS 'configuration'@'%' IDENTIFIED BY 'yRLjX2qA0YwYhzU';
source configuration_privileges.sql

-- Gatekeeper
CREATE USER IF NOT EXISTS 'gatekeeper'@'localhost' IDENTIFIED BY 'fbJKYzKhU5t8QtT';
CREATE USER IF NOT EXISTS 'gatekeeper'@'%' IDENTIFIED BY 'fbJKYzKhU5t8QtT';
source gatekeeper_privileges.sql

-- Gateway
CREATE USER IF NOT EXISTS 'gateway'@'localhost' IDENTIFIED BY 'LfiSM9DpGfDEP5g';
CREATE USER IF NOT EXISTS 'gateway'@'%' IDENTIFIED BY 'LfiSM9DpGfDEP5g';
source gateway_privileges.sql

-- Certificate Authority
CREATE USER IF NOT EXISTS 'certificate_authority'@'localhost' IDENTIFIED BY 'FsdG6Kgf9QpPfv2';
CREATE USER IF NOT EXISTS 'certificate_authority'@'%' IDENTIFIED BY 'FsdG6Kgf9QpPfv2';
source certificate_authority_privileges.sql

-- QoS Monitor
CREATE USER IF NOT EXISTS 'qos_monitor'@'localhost' IDENTIFIED BY 'RLY3UEx6nx4kSXy';
CREATE USER IF NOT EXISTS 'qos_monitor'@'%' IDENTIFIED BY 'RLY3UEx6nx4kSXy';
source qos_monitor_privileges.sql

-- Translator
CREATE USER IF NOT EXISTS 'translator'@'localhost' IDENTIFIED BY 'wozYpV58G0HUkbL';
CREATE USER IF NOT EXISTS 'translator'@'%' IDENTIFIED BY 'wozYpV58G0HUkbL';
source translator_privileges.sql

-- GAMS
CREATE USER IF NOT EXISTS 'gams'@'localhost' IDENTIFIED BY 'Kvtmcxrp5fxUCk4';
CREATE USER IF NOT EXISTS 'gams'@'%' IDENTIFIED BY 'Kvtmcxrp5fxUCk4';
source gams_privileges.sql

-- Plant Description Engine
CREATE USER IF NOT EXISTS 'plant_description_engine'@'localhost' IDENTIFIED BY 'ivJ2y9qWCpTmzr0';
CREATE USER IF NOT EXISTS 'plant_description_engine'@'%' IDENTIFIED BY 'ivJ2y9qWCpTmzr0';
source plant_description_engine_privileges.sql

-- MSVC Monitor
CREATE USER IF NOT EXISTS 'mscv'@'localhost' IDENTIFIED BY 'ASdPsa64y56A128';
CREATE USER IF NOT EXISTS 'mscv'@'%' IDENTIFIED BY 'ASdPsa64y56A128';
source mscv_privileges.sql
