ALTER TABLE merchants RENAME TO credentials;

ALTER TABLE credentials DROP COLUMN business_name;

ALTER INDEX idx_merchants_email RENAME TO idx_credentials_email;
