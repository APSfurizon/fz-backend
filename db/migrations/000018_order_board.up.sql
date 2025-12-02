BEGIN;
ALTER TABLE orders ADD order_board int2 NULL CONSTRAINT order_board_check CHECK (((order_board >= 0) AND (order_board <= 2)));
UPDATE orders SET order_board=0 WHERE order_board IS NULL;
ALTER TABLE orders ALTER COLUMN order_board SET NOT NULL;
ALTER TABLE orders ADD order_board_position_id int8 NULL;
COMMIT;