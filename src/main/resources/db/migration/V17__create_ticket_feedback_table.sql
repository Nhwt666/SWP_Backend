CREATE TABLE ticket_feedback (
    id BIGINT IDENTITY PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    feedback NVARCHAR(1000),
    rating INT CHECK (rating >= 1 AND rating <= 5),
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    CONSTRAINT FK_ticket_feedback_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT FK_ticket_feedback_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT UQ_ticket_feedback_ticket_user UNIQUE (ticket_id, user_id)
); 