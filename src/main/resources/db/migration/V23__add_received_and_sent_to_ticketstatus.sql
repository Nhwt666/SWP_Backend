-- Add RECEIVED and CONFIRMED statuses to ticket_status enum
-- This migration adds support for DNA testing kit management process

-- For SQL Server, we need to alter the table to add the new enum values
-- First, let's check if the column exists and add the new values
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tickets' AND COLUMN_NAME = 'status')
BEGIN
    -- Add new status values to the existing enum
    -- Note: SQL Server doesn't have native enum support, so we rely on the application to validate
    -- The enum values are defined in the Java code and will be validated at the application level
    PRINT 'Ticket status column exists. New statuses RECEIVED and CONFIRMED will be available for use.';
END
ELSE
BEGIN
    PRINT 'Ticket status column not found.';
END 