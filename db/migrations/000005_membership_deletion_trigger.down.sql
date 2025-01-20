ALTER TABLE membership_cards DROP CONSTRAINT IF EXISTS membership_cards_only_one_id_per_year;

ALTER TABLE membership_cards DROP CONSTRAINT IF EXISTS membership_cards_order_fk;
ALTER TABLE membership_cards ADD CONSTRAINT membership_cards_order_fk FOREIGN KEY (created_for_order) REFERENCES orders (id) ON DELETE CASCADE ON UPDATE CASCADE;


DROP TRIGGER IF EXISTS shift_other_membership_cards_on_deletion ON membership_cards;
DROP TRIGGER IF EXISTS check_membership_card_deletion ON membership_cards;
DROP FUNCTION IF EXISTS shiftOtherMembershipCards();
DROP FUNCTION IF EXISTS checkIfMembershipCardCanBeDeleted();

DROP TRIGGER IF EXISTS delete_membership_card_on_order_deletion ON orders;
DROP FUNCTION IF EXISTS deleteMembershipAfterOrder();
DROP FUNCTION IF EXISTS canDeleteMembershipCard();
