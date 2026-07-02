BEGIN;

DROP TRIGGER IF EXISTS membership_creation_trigger ON membership_cards;
DROP FUNCTION IF EXISTS membershipCreationTrigger;

DROP TRIGGER IF EXISTS prevent_update_id_in_year ON membership_cards;

CREATE OR REPLACE FUNCTION checkIfMembershipCardCanBeDeleted() RETURNS TRIGGER AS $_$
BEGIN
    IF NOT canDeleteMembershipCard(OLD.id_in_year, OLD.issue_year) THEN
        RETURN NULL;
    END IF;
    RETURN OLD;
END $_$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION deleteMembershipAfterOrder() RETURNS TRIGGER AS $_$
DECLARE
    cardIdInYear int4;
    issueYear int2;
    cardId int8;
BEGIN
    SELECT INTO cardIdInYear, cardId, issueYear membership_cards.id_in_year, membership_cards.card_db_id, membership_cards.issue_year  FROM membership_cards WHERE created_for_order = OLD.id;
    -- This will be evaluated two times --
    IF canDeleteMembershipCard(cardIdInYear, issueYear) THEN
        DELETE FROM membership_cards WHERE membership_cards.card_db_id = cardId;
    END IF;
    RETURN OLD;
END $_$ LANGUAGE 'plpgsql';

DROP FUNCTION IF EXISTS canDeleteMembershipCard; -- So it get's deleted even if it has different params
CREATE OR REPLACE FUNCTION canDeleteMembershipCard(startingFromId int4, issueYear int2) RETURNS bool AS $_$
BEGIN
    RETURN(SELECT NOT EXISTS(
        SELECT membership_cards.card_db_id
        FROM membership_cards
        WHERE
            membership_cards.issue_year = issueYear
          AND membership_cards.id_in_year >= startingFromId
          AND membership_cards.already_registered = true
    ));
END $_$ LANGUAGE 'plpgsql';

UPDATE membership_cards SET id_in_year = random(0, 99999999) WHERE id_in_year IS NULL;
ALTER TABLE membership_cards ALTER id_in_year SET NOT NULL;


CREATE OR REPLACE FUNCTION shiftOtherMembershipCards() RETURNS TRIGGER AS $_$
BEGIN
    UPDATE membership_cards SET id_in_year = membership_cards.id_in_year - 1 WHERE membership_cards.issue_year = OLD.issue_year AND membership_cards.id_in_year >= OLD.id_in_year;

    IF NOT canDeleteMembershipCard(OLD.id_in_year, OLD.issue_year) THEN
        RAISE EXCEPTION 'Membership card was deleted, but subsequent cards were registered!'; --race condition detected--
    END IF;
    RETURN OLD;
END $_$ LANGUAGE 'plpgsql';
DROP TRIGGER IF EXISTS shift_other_membership_cards_on_deletion ON membership_cards;
CREATE TRIGGER shift_other_membership_cards_on_deletion AFTER DELETE ON membership_cards FOR EACH ROW EXECUTE PROCEDURE shiftOtherMembershipCards();

ALTER TABLE membership_cards DROP CONSTRAINT membership_cards_order_fk;
ALTER TABLE membership_cards ADD CONSTRAINT membership_cards_order_fk FOREIGN KEY (created_for_order) REFERENCES orders (id) ON DELETE CASCADE ON UPDATE CASCADE;

END;