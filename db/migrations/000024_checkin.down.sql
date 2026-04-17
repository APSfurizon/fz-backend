BEGIN;

ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_one_checkinsecret_per_event;
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_one_secret_per_event;
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_one_ordercode_per_event;

ALTER TABLE IF EXISTS orders DROP COLUMN IF EXISTS order_included_gadgets;
ALTER TABLE IF EXISTS orders DROP COLUMN IF EXISTS order_customer_notes;

ALTER TABLE IF EXISTS membership_cards DROP COLUMN IF EXISTS sent_by_email;
ALTER TABLE IF EXISTS membership_cards DROP COLUMN IF EXISTS signed_at;

COMMIT;

