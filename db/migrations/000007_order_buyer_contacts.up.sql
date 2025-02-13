ALTER TABLE IF EXISTS orders ADD COLUMN IF NOT EXISTS order_buyer_email text NULL default NULL;
ALTER TABLE IF EXISTS orders ADD COLUMN IF NOT EXISTS order_buyer_phone text NULL default NULL;
ALTER TABLE IF EXISTS orders ADD COLUMN IF NOT EXISTS order_buyer_user text NULL default NULL;
ALTER TABLE IF EXISTS orders ADD COLUMN IF NOT EXISTS order_buyer_locale text NULL default NULL;
