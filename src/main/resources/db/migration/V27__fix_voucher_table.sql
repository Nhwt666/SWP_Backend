-- Fix voucher table schema issues
-- Drop default constraint on status column if it exists
IF EXISTS (SELECT * FROM sys.default_constraints WHERE name = 'DF__voucher__status__634EBE90')
BEGIN
    ALTER TABLE voucher DROP CONSTRAINT DF__voucher__status__634EBE90;
END

-- Alter status column to varchar(255)
ALTER TABLE voucher ALTER COLUMN status VARCHAR(255);

-- Add default constraint back if needed
ALTER TABLE voucher ADD CONSTRAINT DF_voucher_status DEFAULT 'active' FOR status; 