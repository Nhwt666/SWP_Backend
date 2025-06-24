IF COL_LENGTH('tickets', 'rejected_reason') IS NULL
    ALTER TABLE tickets ADD rejected_reason NVARCHAR(500); 