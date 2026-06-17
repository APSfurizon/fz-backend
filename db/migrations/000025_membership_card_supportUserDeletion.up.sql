BEGIN;
ALTER TABLE membership_cards ALTER user_id DROP NOT NULL;
ALTER TABLE membership_cards ADD COLUMN first_name TEXT;
ALTER TABLE membership_cards ADD COLUMN last_name TEXT;
ALTER TABLE membership_cards ADD COLUMN birthday date;
ALTER TABLE membership_cards ADD COLUMN fiscal_code varchar(16) NULL;

UPDATE membership_cards
    SET
        first_name = info.info_first_name,
        last_name = info.info_last_name,
        birthday = info.info_birthday,
        fiscal_code = info.info_fiscal_code
    FROM (
        SELECT
            membership_info.info_first_name,
            membership_info.info_last_name,
            membership_info.info_birthday,
            membership_info.info_fiscal_code,
            membership_info.user_id
        FROM membership_info
    ) AS info
    WHERE membership_cards.user_id = info.user_id;

-- In case there somehow are cards with an user id which doesn't exist anymore
UPDATE membership_cards SET first_name = '' WHERE first_name IS NULL;
UPDATE membership_cards SET last_name = '' WHERE last_name IS NULL;
UPDATE membership_cards SET birthday = '1970-01-01' WHERE birthday IS NULL;

ALTER TABLE membership_cards ALTER first_name SET NOT NULL;
ALTER TABLE membership_cards ALTER last_name SET NOT NULL;
ALTER TABLE membership_cards ALTER birthday SET NOT NULL;
COMMIT;