CREATE TABLE item_types(
    base_type_id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    emoji VARCHAR NOT NULL,
    role_id BIGINT NOT NULL
);

CREATE TABLE items(
    item_id SERIAL PRIMARY KEY,
    type_id SERIAL NOT NULL REFERENCES item_types(base_type_id),
    name VARCHAR NOT NULL,
    duration BIGINT
);

CREATE TABLE transactions(
    transaction_id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_id SERIAL NOT NULL REFERENCES items(item_id),
    expires_at BIGINT NOT NULL,
    is_play_activity BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE users ADD COLUMN last_karma INT NOT NULL DEFAULT 0;

INSERT INTO item_types VALUES (1, 'XP-Booster', ':moneybag:', -1);

INSERT INTO item_types VALUES (2, 'Premium', ':star:', -1);

CREATE OR REPLACE FUNCTION add_random_xp(id BIGINT)
RETURNS TABLE (rank_changed BOOLEAN, current_rank int, next_rank INT, current_xp INT) AS
$$
DECLARE
	xp INT;
	bonus_xp INT;
	karma INT;
BEGIN
   SELECT get_random_xp INTO xp FROM get_random_xp();
	SELECT users.karma_points INTO karma FROM users WHERE users.user_id = id;
	bonus_xp := 0;
	IF karma < 0 THEN
		bonus_xp := -1;
	ELSIF karma >= 300 THEN
		bonus_xp := 3;
	ELSIF karma >= 200 THEN
		bonus_xp := 2;
	ELSIF karma >= 100 THEN
		bonus_xp := 1;
	END IF;
	IF (SELECT EXISTS(SELECT 1 FROM transactions JOIN items ON transactions.item_id = items.item_id WHERE transactions.user_id=id AND type_id = 1)) THEN
	    bonus_xp := bonus_xp + 2;
	END IF;
	SELECT INTO rank_changed, current_rank, next_rank, current_xp * FROM add_xp(id, xp + bonus_xp);
	RETURN NEXT;
END;
$$ LANGUAGE plpgsql;

ALTER TABLE ranks ADD COLUMN lootbox_reward BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE ranks ADD COLUMN item_reward_id INT NOT NULL DEFAULT -1;
