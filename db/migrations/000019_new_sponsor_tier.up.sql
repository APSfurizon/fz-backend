BEGIN;
ALTER TABLE orders DROP CONSTRAINT orders_sponsorship_check;
ALTER TABLE orders ADD CONSTRAINT orders_sponsorship_check CHECK (((order_sponsorship_type >= 0) AND (order_sponsorship_type <= 3)));
COMMIT;