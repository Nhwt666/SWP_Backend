-- Fix timestamp columns to use DATETIME2 for proper timestamp handling
-- This ensures compatibility with LocalDateTime in Java entities

-- Check if columns exist and alter them to DATETIME2
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tickets' AND COLUMN_NAME = 'created_at')
BEGIN
    ALTER TABLE tickets ALTER COLUMN created_at DATETIME2;
END

IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tickets' AND COLUMN_NAME = 'updated_at')
BEGIN
    ALTER TABLE tickets ALTER COLUMN updated_at DATETIME2;
END

IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tickets' AND COLUMN_NAME = 'completed_at')
BEGIN
    ALTER TABLE tickets ALTER COLUMN completed_at DATETIME2;
END

-- Handle feedback_date column - if it's timestamp type, we need to drop and recreate
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tickets' AND COLUMN_NAME = 'feedback_date')
BEGIN
    -- Check if it's timestamp type
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_NAME = 'tickets' 
               AND COLUMN_NAME = 'feedback_date' 
               AND DATA_TYPE = 'timestamp')
    BEGIN
        -- Drop the timestamp column and recreate as DATETIME2
        ALTER TABLE tickets DROP COLUMN feedback_date;
        ALTER TABLE tickets ADD feedback_date DATETIME2;
    END
    ELSE
    BEGIN
        -- If it's not timestamp, just alter to DATETIME2
        ALTER TABLE tickets ALTER COLUMN feedback_date DATETIME2;
    END
END

IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tickets' AND COLUMN_NAME = 'appointment_date')
BEGIN
    ALTER TABLE tickets ALTER COLUMN appointment_date DATE;
END 