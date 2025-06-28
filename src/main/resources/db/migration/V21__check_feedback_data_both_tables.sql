-- Check feedback data in both tables
-- This will help us understand where the feedback data is stored

-- Check tickets table for feedback
SELECT 'TICKETS TABLE' as source, COUNT(*) as count
FROM tickets 
WHERE rating IS NOT NULL OR feedback IS NOT NULL
UNION ALL
SELECT 'REVIEWS TABLE' as source, COUNT(*) as count
FROM reviews;

-- Show recent feedback from tickets table
SELECT TOP 5 
    'TICKETS' as source,
    id, 
    rating, 
    feedback, 
    feedback_date,
    status
FROM tickets 
WHERE rating IS NOT NULL OR feedback IS NOT NULL 
ORDER BY feedback_date DESC;

-- Show recent feedback from reviews table
SELECT TOP 5 
    'REVIEWS' as source,
    id,
    rating,
    feedback,
    created_at as feedback_date,
    ticket_id
FROM reviews 
ORDER BY created_at DESC; 