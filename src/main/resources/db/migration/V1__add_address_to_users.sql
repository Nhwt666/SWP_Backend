ALTER TABLE users
DROP COLUMN address;

ALTER TABLE users
    ADD address NVARCHAR(255) NULL;
