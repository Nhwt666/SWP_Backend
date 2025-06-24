IF COL_LENGTH('tickets', 'sample_from_person_a') IS NOT NULL
    ALTER TABLE tickets DROP COLUMN sample_from_person_a;

IF COL_LENGTH('tickets', 'sample_from_person_b') IS NOT NULL
    ALTER TABLE tickets DROP COLUMN sample_from_person_b; 