-- Check feedback data in tickets table
-- This migration will help us verify if feedback data exists

-- Count tickets with feedback
SELECT COUNT(*) as tickets_with_feedback 
FROM tickets 
WHERE rating IS NOT NULL OR feedback IS NOT NULL;

-- Show recent tickets with feedback
SELECT TOP 10 
    id, 
    rating, 
    feedback, 
    feedback_date,
    status,
    customer_id
FROM tickets 
WHERE rating IS NOT NULL OR feedback IS NOT NULL 
ORDER BY feedback_date DESC; 