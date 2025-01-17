BEGIN;
ALTER TABLE rooms DROP CONSTRAINT IF EXISTS rooms_only_one_order;
ALTER TABLE rooms ADD CONSTRAINT rooms_only_one_order UNIQUE (order_id);
COMMIT;