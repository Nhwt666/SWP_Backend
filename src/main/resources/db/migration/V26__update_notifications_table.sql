-- Drop existing table and recreate with new structure
DROP TABLE IF EXISTS notifications;

CREATE TABLE notifications (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    message NVARCHAR(500) NOT NULL,
    type NVARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL,
    ticket_id BIGINT,
    is_read BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    expires_at DATETIME NOT NULL,
    status_change NVARCHAR(1000),
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Create indexes for better performance
CREATE INDEX idx_notification_user_id ON notifications(user_id);
CREATE INDEX idx_notification_ticket_id ON notifications(ticket_id);
CREATE INDEX idx_notification_expires_at ON notifications(expires_at);
CREATE INDEX idx_notification_created_at ON notifications(created_at); 