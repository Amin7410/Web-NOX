-- V17__add_status_to_workspaces.sql

ALTER TABLE workspaces ADD COLUMN status VARCHAR(20) DEFAULT 'DRAFT' NOT NULL;
