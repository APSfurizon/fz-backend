BEGIN;

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

DROP TRIGGER IF EXISTS delete_membership_card_on_order_deletion ON orders;
CREATE TRIGGER delete_membership_card_on_order_deletion BEFORE DELETE ON orders FOR EACH ROW EXECUTE PROCEDURE deleteMembershipAfterOrder();



CREATE OR REPLACE FUNCTION checkIfMembershipCardCanBeDeleted() RETURNS TRIGGER AS $_$
BEGIN
    IF NOT canDeleteMembershipCard(OLD.id_in_year, OLD.issue_year) THEN
        RETURN NULL;
    END IF;
    RETURN OLD;
END $_$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION shiftOtherMembershipCards() RETURNS TRIGGER AS $_$
BEGIN
    UPDATE membership_cards SET id_in_year = membership_cards.id_in_year - 1 WHERE membership_cards.issue_year = OLD.issue_year AND membership_cards.id_in_year >= OLD.id_in_year;

    IF NOT canDeleteMembershipCard(OLD.id_in_year, OLD.issue_year) THEN
        RAISE EXCEPTION 'Membership card was deleted, but subsequent cards were registered!'; --race condition detected--
    END IF;
    RETURN OLD;
END $_$ LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS check_membership_card_deletion ON membership_cards;
CREATE TRIGGER check_membership_card_deletion BEFORE DELETE ON membership_cards FOR EACH ROW EXECUTE PROCEDURE checkIfMembershipCardCanBeDeleted();
DROP TRIGGER IF EXISTS shift_other_membership_cards_on_deletion ON membership_cards;
CREATE TRIGGER shift_other_membership_cards_on_deletion AFTER DELETE ON membership_cards FOR EACH ROW EXECUTE PROCEDURE shiftOtherMembershipCards();


ALTER TABLE membership_cards DROP CONSTRAINT IF EXISTS membership_cards_order_fk;
ALTER TABLE membership_cards ADD CONSTRAINT membership_cards_order_fk FOREIGN KEY (created_for_order) REFERENCES orders (id) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE membership_cards ADD CONSTRAINT membership_cards_only_one_id_per_year UNIQUE (issue_year, id_in_year) DEFERRABLE INITIALLY DEFERRED;

COMMIT;