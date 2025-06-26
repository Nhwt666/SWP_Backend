CREATE TABLE notifications (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message NVARCHAR(500) NOT NULL,
    time DATETIME NOT NULL,
    [read] BIT NOT NULL,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(user_id)
); 