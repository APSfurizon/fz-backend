BEGIN;
ALTER TABLE orders DROP CONSTRAINT IF EXISTS order_only_one_serial_per_event;
ALTER TABLE IF EXISTS orders DROP COLUMN IF EXISTS order_serial_in_event;
COMMIT;