-- Update columns to support Unicode for Vietnamese text
-- For SQL Server, use NVARCHAR for Unicode support

-- Update reason column
ALTER TABLE tickets 
ALTER COLUMN reason NVARCHAR(500);

-- Update address column  
ALTER TABLE tickets 
ALTER COLUMN address NVARCHAR(255);

-- Update phone column
ALTER TABLE tickets 
ALTER COLUMN phone NVARCHAR(20);

-- Update email column
ALTER TABLE tickets 
ALTER COLUMN email NVARCHAR(100);

-- Update result_string column
ALTER TABLE tickets 
ALTER COLUMN result_string NVARCHAR(500);

-- Update sample1_name column
ALTER TABLE tickets 
ALTER COLUMN sample1_name NVARCHAR(255);

-- Update sample2_name column
ALTER TABLE tickets 
ALTER COLUMN sample2_name NVARCHAR(255);

-- Update sample_from_person_a column (if exists)
IF COL_LENGTH('tickets', 'sample_from_person_a') IS NOT NULL
    ALTER TABLE tickets 
    ALTER COLUMN sample_from_person_a NVARCHAR(255);

-- Update sample_from_person_b column (if exists)  
IF COL_LENGTH('tickets', 'sample_from_person_b') IS NOT NULL
    ALTER TABLE tickets 
    ALTER COLUMN sample_from_person_b NVARCHAR(255); 