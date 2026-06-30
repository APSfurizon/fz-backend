BEGIN;

DROP TRIGGER IF EXISTS shift_other_membership_cards_on_deletion ON membership_cards;
DROP FUNCTION IF EXISTS shiftOtherMembershipCards;

ALTER TABLE membership_cards ALTER id_in_year DROP NOT NULL;


DROP FUNCTION IF EXISTS canDeleteMembershipCard; -- So it get's deleted even if it has different params
CREATE OR REPLACE FUNCTION canDeleteMembershipCard(id_in_year int, already_registered bool) RETURNS bool AS $_$
BEGIN
    RETURN(id_in_year IS NULL AND already_registered = false);
END $_$ LANGUAGE 'plpgsql';



CREATE OR REPLACE FUNCTION deleteMembershipAfterOrder() RETURNS TRIGGER AS $_$
DECLARE
    cardId int8;
    idInYear int8;
    registered bool;
BEGIN
    SELECT INTO cardId, idInYear, registered membership_cards.card_db_id, membership_cards.id_in_year, membership_cards.already_registered FROM membership_cards WHERE created_for_order = OLD.id;
    -- This will be evaluated two times --
    IF canDeleteMembershipCard(idInYear, registered) THEN
        DELETE FROM membership_cards WHERE membership_cards.card_db_id = cardId;
    END IF;
    RETURN OLD;
END $_$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION checkIfMembershipCardCanBeDeleted() RETURNS TRIGGER AS $_$
BEGIN
    IF NOT canDeleteMembershipCard(OLD.id_in_year, OLD.already_registered) THEN
        RETURN NULL;
    END IF;
    RETURN OLD;
END $_$ LANGUAGE 'plpgsql';

-- The condition is the same of the delation, IE id_in_year == null && is_registered == false
CREATE TRIGGER prevent_update_id_in_year BEFORE UPDATE OF id_in_year ON membership_cards FOR EACH ROW EXECUTE PROCEDURE checkIfMembershipCardCanBeDeleted();



CREATE OR REPLACE FUNCTION membershipCreationTrigger() RETURNS TRIGGER AS $_$
DECLARE
    cardAlreadyExists bool;
BEGIN
    SELECT INTO cardAlreadyExists EXISTS(SELECT membership_cards.card_db_id FROM membership_cards WHERE membership_cards.user_id = NEW.user_id AND membership_cards.issue_year = NEW.issue_year);
    IF cardAlreadyExists THEN
        NEW.id_in_year := NULL;
    END IF;
    RETURN NEW;
END $_$ LANGUAGE 'plpgsql';
CREATE TRIGGER membership_creation_trigger BEFORE INSERT ON membership_cards FOR EACH ROW EXECUTE FUNCTION membershipCreationTrigger();

COMMIT;