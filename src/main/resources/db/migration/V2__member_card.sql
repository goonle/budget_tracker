-- A member can have several cards (debit + credit, replaced cards),
-- so card numbers move out of member into member_card.

ALTER TABLE member ADD COLUMN user_id BIGINT REFERENCES app_user (id);
ALTER TABLE member ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE member ADD CONSTRAINT uq_member_user_name UNIQUE (user_id, name);
ALTER TABLE member ADD CONSTRAINT uq_member_id_user UNIQUE (id, user_id);
ALTER TABLE member DROP COLUMN card_last4;

CREATE TABLE
    member_card (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT NOT NULL REFERENCES app_user (id),
        member_id BIGINT NOT NULL,
        card_last4 VARCHAR(4) NOT NULL CHECK (card_last4 ~ '^[0-9]{4}$'),
        created_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        CONSTRAINT fk_member_card_member FOREIGN KEY (member_id, user_id) REFERENCES member (id, user_id),
        CONSTRAINT uq_member_card_user_last4 UNIQUE (user_id, card_last4)
    );

-- The card digits from the CSV line. Kept even when no member is assigned,
-- so the page can show "•••• 1234" and rows can be back-filled later.
ALTER TABLE transaction ADD COLUMN card_last4 VARCHAR(4) CHECK (card_last4 ~ '^[0-9]{4}$');
