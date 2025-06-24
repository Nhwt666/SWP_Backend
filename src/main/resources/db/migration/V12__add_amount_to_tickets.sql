IF COL_LENGTH('tickets', 'amount') IS NULL
    ALTER TABLE tickets ADD amount DECIMAL(19,2); 