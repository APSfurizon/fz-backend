ALTER TABLE IF EXISTS orders ADD COLUMN IF NOT EXISTS order_internal_comment text NULL default NULL;
ALTER TABLE IF EXISTS orders ADD COLUMN IF NOT EXISTS order_requires_attention bool NOT NULL default FALSE;
ALTER TABLE IF EXISTS orders ADD COLUMN IF NOT EXISTS order_checkin_text text NULL default NULL;
