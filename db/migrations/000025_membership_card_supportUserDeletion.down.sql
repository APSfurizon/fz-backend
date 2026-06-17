BEGIN;

ALTER TABLE membership_cards DROP COLUMN fiscal_code;
ALTER TABLE membership_cards DROP COLUMN birthday;
ALTER TABLE membership_cards DROP COLUMN last_name;
ALTER TABLE membership_cards DROP COLUMN first_name;

-- This may fail if there are any membership cards without a user
-- If it fails, run the following command:
-- DELETE FROM membership_cards WHERE user_id IS NULL;
ALTER TABLE membership_cards ALTER user_id SET NOT NULL;
COMMIT;