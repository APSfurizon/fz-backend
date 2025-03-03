BEGIN;
ALTER TABLE IF EXISTS orders ADD COLUMN IF NOT EXISTS order_serial_in_event int8;
UPDATE orders SET order_serial_in_event = s.serial FROM (SELECT id, ROW_NUMBER() OVER (PARTITION BY event_id ORDER BY id) AS serial FROM orders) AS s WHERE orders.id = s.id;
ALTER TABLE IF EXISTS orders ALTER COLUMN order_serial_in_event SET NOT NULL;
ALTER TABLE orders ADD CONSTRAINT order_only_one_serial_per_event UNIQUE (event_id, order_serial_in_event) DEFERRABLE INITIALLY DEFERRED;
COMMIT;