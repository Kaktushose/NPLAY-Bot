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

CREATE TABLE lootbox_rewards (
    reward_id SERIAL NOT NULL PRIMARY KEY,
    xp_reward INT NOT NULL DEFAULT -1,
    karma_reward INT NOT NULL DEFAULT -1,
    item_reward INT REFERENCES items(item_id),
    chance INT NOT NULL DEFAULT 0
);

CREATE FUNCTION get_random_lootbox()
RETURNS INT AS
$$
DECLARE
	result_row RECORD;
	box_chance INT;
	chance_sum INT;
BEGIN
	box_chance := floor(random() * 100) + 1;
	chance_sum := 0;
	FOR result_row IN SELECT * FROM lootbox_rewards
	LOOP
		chance_sum := chance_sum + result_row.chance;
		IF chance_sum >= box_chance THEN
			RETURN result_row.reward_id;
		END IF;
	END LOOP;
END;
$$ LANGUAGE plpgsql;

DROP FUNCTION set_xp;
DROP FUNCTION add_xp;
DROP FUNCTION add_random_xp;

CREATE OR REPLACE FUNCTION set_xp(id BIGINT, new_xp INT)
RETURNS TABLE (rank_changed BOOLEAN, previous_rank int, current_rank int, next_rank int) AS
$$
DECLARE
    old_rank INT;
    new_rank INT;
BEGIN
	SELECT INTO old_rank users.rank_id FROM users WHERE users.user_id = id;
	IF new_xp < 0 THEN
	  new_xp := 0;
	END IF;
	UPDATE users SET xp = new_xp WHERE users.user_id = id;
	SELECT INTO new_rank users.rank_id FROM users WHERE users.user_id = id;


	SELECT INTO rank_changed old_rank <> new_rank;
	SELECT INTO previous_rank old_rank;
	SELECT INTO current_rank new_rank;
	SELECT INTO next_rank new_rank + 1;
	RETURN NEXT;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION add_xp(id BIGINT, xp_to_add INT)
RETURNS TABLE (rank_changed BOOLEAN, previous_rank int, current_rank int, next_rank INT, current_xp INT) AS
$$
DECLARE
BEGIN
    SELECT xp_to_add + users.xp INTO current_xp FROM users WHERE users.user_id = id;
    SELECT INTO rank_changed, previous_rank, current_rank, next_rank * FROM set_xp(id, current_xp);
	RETURN NEXT;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION add_random_xp(id BIGINT)
RETURNS TABLE (rank_changed BOOLEAN, previous_rank int, current_rank int, next_rank INT, current_xp INT) AS
$$
DECLARE
	xp INT;
BEGIN
   SELECT get_random_xp INTO xp FROM get_random_xp();
	SELECT INTO rank_changed, previous_rank, current_rank, next_rank, current_xp * FROM add_xp(id, xp);
	RETURN NEXT;
END;
$$ LANGUAGE plpgsql;
