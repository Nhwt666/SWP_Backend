-- Add feedback columns to tickets table
ALTER TABLE tickets ADD rating INTEGER;
ALTER TABLE tickets ADD feedback TEXT;
ALTER TABLE tickets ADD feedback_date TIMESTAMP; 