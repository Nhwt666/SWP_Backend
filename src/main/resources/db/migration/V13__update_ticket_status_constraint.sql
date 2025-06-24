ALTER TABLE tickets DROP CONSTRAINT CK__tickets__status__403A8C7D;

ALTER TABLE tickets ADD CONSTRAINT CK__tickets__status__403A8C7D
CHECK ([status]='COMPLETED' OR [status]='IN_PROGRESS' OR [status]='PENDING' OR [status]='REJECTED');