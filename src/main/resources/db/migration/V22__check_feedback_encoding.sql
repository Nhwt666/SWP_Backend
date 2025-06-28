-- Check current data type of feedback column
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'tickets' AND COLUMN_NAME = 'feedback';

-- Check current feedback data to see encoding issues
SELECT id, feedback FROM tickets WHERE feedback IS NOT NULL;

-- Fix feedback column to use NVARCHAR for proper Unicode support
ALTER TABLE tickets ALTER COLUMN feedback NVARCHAR(1000);

-- Update existing feedback data with proper Unicode encoding
UPDATE tickets 
SET feedback = N'test feedback test tiếng việt test' 
WHERE id = 1802;

UPDATE tickets 
SET feedback = N'tesstttt feedback test tiếng việt nguyên' 
WHERE id = 1852;

-- Verify the fix
SELECT id, feedback FROM tickets WHERE feedback IS NOT NULL; 