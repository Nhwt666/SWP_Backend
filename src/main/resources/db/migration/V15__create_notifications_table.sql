CREATE TABLE notification (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message NVARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL,
    is_read BIT NOT NULL,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(user_id)
); 