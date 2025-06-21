-- Update sample1_name and sample2_name columns to NVARCHAR for Unicode support
ALTER TABLE tickets 
ALTER COLUMN sample1_name NVARCHAR(255);

ALTER TABLE tickets 
ALTER COLUMN sample2_name NVARCHAR(255); 