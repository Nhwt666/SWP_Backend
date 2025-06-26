IF COL_LENGTH('tickets', 'feedback') IS NOT NULL
    ALTER TABLE tickets DROP COLUMN feedback; 