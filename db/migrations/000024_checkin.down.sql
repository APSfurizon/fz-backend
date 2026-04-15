BEGIN;

ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_one_checkinsecret_per_event;
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_one_secret_per_event;
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_one_ordercode_per_event;

ALTER TABLE IF EXISTS orders DROP COLUMN IF EXISTS order_customer_notes;

COMMIT;

