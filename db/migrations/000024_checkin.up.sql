BEGIN;

ALTER TABLE IF EXISTS orders ADD COLUMN IF NOT EXISTS order_customer_notes text NULL;
ALTER TABLE IF EXISTS orders ADD COLUMN IF NOT EXISTS order_included_gadgets json NULL;

ALTER TABLE orders ADD CONSTRAINT orders_one_ordercode_per_event UNIQUE(event_id, order_code);
ALTER TABLE orders ADD CONSTRAINT orders_one_secret_per_event UNIQUE(event_id, order_secret);
ALTER TABLE orders ADD CONSTRAINT orders_one_checkinsecret_per_event UNIQUE(event_id, order_checkin_secret);

COMMIT;

