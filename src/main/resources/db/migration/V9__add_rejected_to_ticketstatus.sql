-- Allow 'REJECTED' as a value for TicketStatus in the tickets table
-- No schema change is required if the column is already NVARCHAR and uses string enums
-- This migration is for documentation and future reference
-- If you have constraints or check constraints, uncomment and adapt the following:
IF EXISTS (
    SELECT 1 FROM sys.check_constraints WHERE name = 'CK_tickets_status'
)
    ALTER TABLE tickets DROP CONSTRAINT CK_tickets_status;
ALTER TABLE tickets ADD CONSTRAINT CK_tickets_status CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'REJECTED'));