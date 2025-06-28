CREATE TABLE reviews (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    feedback NVARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_reviews_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id),
    CONSTRAINT fk_reviews_customer FOREIGN KEY (customer_id) REFERENCES users(user_id)
); 