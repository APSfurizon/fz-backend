ALTER TABLE IF EXISTS orders DROP COLUMN IF EXISTS order_internal_comment;
ALTER TABLE IF EXISTS orders DROP COLUMN IF EXISTS order_requires_attention;
ALTER TABLE IF EXISTS orders DROP COLUMN IF EXISTS order_checkin_text;