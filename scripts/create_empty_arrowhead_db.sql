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

-- Choreographer
CREATE USER IF NOT EXISTS 'choreographer'@'localhost' IDENTIFIED BY 'Qa5yx4oBp4Y9RLX';
CREATE USER IF NOT EXISTS 'choreographer'@'%' IDENTIFIED BY 'Qa5yx4oBp4Y9RLX';
source choreographer_privileges.sql

-- Gatekeeper
CREATE USER IF NOT EXISTS 'gatekeeper'@'localhost' IDENTIFIED BY 'fbJKYzKhU5t8QtT';
CREATE USER IF NOT EXISTS 'gatekeeper'@'%' IDENTIFIED BY 'fbJKYzKhU5t8QtT';
source gatekeeper_privileges.sql

-- Gateway
CREATE USER IF NOT EXISTS 'gateway'@'localhost' IDENTIFIED BY 'LfiSM9DpGfDEP5g';
CREATE USER IF NOT EXISTS 'gateway'@'%' IDENTIFIED BY 'LfiSM9DpGfDEP5g';
source gateway_privileges.sql

-- QoS Monitor
CREATE USER IF NOT EXISTS 'qos_monitor'@'localhost' IDENTIFIED BY 'RLY3UEx6nx4kSXy';
CREATE USER IF NOT EXISTS 'qos_monitor'@'%' IDENTIFIED BY 'RLY3UEx6nx4kSXy';
source qos_monitor_privileges.sql