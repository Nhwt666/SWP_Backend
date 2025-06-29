-- Migration V25: Update ticket status constraints to include all status values
-- This migration ensures the database constraint allows all enum values

-- Drop existing constraint if it exists
IF EXISTS (
    SELECT 1 FROM sys.check_constraints WHERE name = 'CK_tickets_status'
)
BEGIN
    ALTER TABLE tickets DROP CONSTRAINT CK_tickets_status;
    PRINT 'Dropped existing CK_tickets_status constraint';
END

-- Add new constraint with all status values
ALTER TABLE tickets ADD CONSTRAINT CK_tickets_status 
CHECK (status IN ('PENDING', 'IN_PROGRESS', 'RECEIVED', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'REJECTED'));

PRINT 'Added updated CK_tickets_status constraint with all status values';
PRINT 'Supported statuses: PENDING, IN_PROGRESS, RECEIVED, CONFIRMED, COMPLETED, CANCELLED, REJECTED'; 