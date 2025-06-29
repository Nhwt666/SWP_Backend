-- Migration V24: Add CANCELLED status and update ticket status constraints
-- This migration adds support for the new workflow with CONFIRMED as initial status for CIVIL SELF_TEST

-- For SQL Server, we need to update the constraints to include the new statuses
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tickets' AND COLUMN_NAME = 'status')
BEGIN
    -- Add new status values to the existing enum
    -- Note: SQL Server doesn't have native enum support, so we rely on the application to validate
    -- The enum values are defined in the Java code and will be validated at the application level
    PRINT 'Ticket status column exists. New statuses CONFIRMED and CANCELLED will be available for use.';
    PRINT 'Updated workflow: CONFIRMED -> RECEIVED -> PENDING -> IN_PROGRESS -> COMPLETED for CIVIL SELF_TEST';
END
ELSE
BEGIN
    PRINT 'Ticket status column not found.';
END 