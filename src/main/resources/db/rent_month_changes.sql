-- Schema changes for rent-month calculation and the monthly pending rent reminder (PostgreSQL).
-- spring.jpa.hibernate.ddl-auto=update applies the first two automatically; this script
-- is for reference or for running them by hand.

-- 1. Renter email address (optional, contact info only; renters are never emailed)
ALTER TABLE renters ADD COLUMN IF NOT EXISTS email VARCHAR(255);

-- 2. Email log that stops the same monthly owner report being sent twice
CREATE TABLE IF NOT EXISTS rent_payment_email_log (
    id                 BIGSERIAL PRIMARY KEY,
    notification_key   VARCHAR(100)  NOT NULL,
    notification_month VARCHAR(7)    NOT NULL,
    notification_type  VARCHAR(30)   NOT NULL,
    renter_id          BIGINT,
    recipient          VARCHAR(500),
    status             VARCHAR(20)   NOT NULL,
    attempts           INTEGER       NOT NULL,
    error_message      VARCHAR(1000),
    created_at         TIMESTAMP     NOT NULL,
    sent_at            TIMESTAMP,
    version            BIGINT,
    CONSTRAINT uq_rent_payment_email_log_key UNIQUE (notification_key)
);

-- 3. OPTIONAL: allow split payments (several payment rows for the same renter and rent month).
--    Only needed if the database has a one-row-per-month unique constraint on rent_payments.
--    Check first:
--      SELECT conname FROM pg_constraint
--      WHERE conrelid = 'rent_payments'::regclass AND contype = 'u';
--    then, if one exists on (renter_id, rent_month, rent_year):
--      ALTER TABLE rent_payments DROP CONSTRAINT <constraint_name>;

-- To resend a month's reminder manually, delete its log rows, e.g.:
--   DELETE FROM rent_payment_email_log WHERE notification_month = '2026-10';
