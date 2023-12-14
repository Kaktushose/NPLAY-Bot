CREATE TABLE guild_settings (
    guild_id BIGINT NOT NULL PRIMARY KEY,
    bot_token VARCHAR(255) NOT NULL,
    bot_channel_id BIGINT NOT NULL,
    log_channel_id BIGINT NOT NULL
);

CREATE TABLE rank_settings (
    guild_id BIGINT NOT NULL PRIMARY KEY,
    message_cooldown INT NOT NULL,
    min_message_length INT NOT NULL,
    valid_channels BIGINT[] NOT NULL
);

CREATE TABLE ranks (
    rank_id INT NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    color VARCHAR(255) NOT NULL,
    bound INT NOT NULL,
    role_id BIGINT NOT NULL
);

CREATE TABLE users (
    user_id BIGINT NOT NULL PRIMARY KEY,
    daily_message BOOLEAN NOT NULL DEFAULT FALSE,
    xp INT NOT NULL DEFAULT 0,
    rank_id INT NOT NULL REFERENCES ranks(rank_id) DEFAULT 1,
    last_valid_message BIGINT NOT NULL DEFAULT 0,
    message_count BIGINT NOT NULL DEFAULT 0,
    start_xp INT NOT NULL DEFAULT 0
);

CREATE TABLE xp_chances (
    chance_id INT NOT NULL PRIMARY KEY,
    amount INT NOT NULL,
    chance INT NOT NULL
);

CREATE TABLE rank_statistics (
    timestamp BIGINT NOT NULL PRIMARY KEY,
    total_message_count INT NOT NULL,
    valid_message_count INT NOT NULL,
    total_xp_gain INT NOT NULL,
    total_rank_ups INT NOT NULL
);

CREATE FUNCTION update_rank_trigger()
RETURNS TRIGGER AS
$$
DECLARE
    new_rank ranks;
BEGIN
    SELECT INTO new_rank * FROM ranks WHERE NEW.xp >= bound ORDER BY bound DESC LIMIT 1;

    UPDATE users SET rank_id = new_rank.rank_id WHERE user_id = NEW.user_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_rank_trigger
AFTER UPDATE OF xp ON users
FOR EACH ROW
EXECUTE FUNCTION update_rank_trigger();

CREATE FUNCTION set_xp(id BIGINT, new_xp INT)
RETURNS TABLE (rank_changed BOOLEAN, current_rank int, next_rank int) AS
$$
DECLARE
    old_rank INT;
    new_rank INT;
BEGIN
	SELECT INTO old_rank users.rank_id FROM users WHERE users.user_id = id;
	UPDATE users SET xp = new_xp WHERE users.user_id = id;
	SELECT INTO new_rank users.rank_id FROM users WHERE users.user_id = id;


	SELECT INTO rank_changed old_rank <> new_rank;
	SELECT INTO current_rank new_rank;
	SELECT INTO next_rank new_rank + 1;
	RETURN NEXT;
END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION add_xp(id BIGINT, xp_to_add INT)
RETURNS TABLE (rank_changed BOOLEAN, current_rank int, next_rank INT, current_xp INT) AS
$$
DECLARE
BEGIN
    SELECT xp_to_add + users.xp INTO current_xp FROM users WHERE users.user_id = id;
    SELECT INTO rank_changed, current_rank, next_rank * FROM set_xp(id, current_xp);
	RETURN NEXT;
END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION get_random_xp()
RETURNS INT AS
$$
DECLARE
	result_row RECORD;
	xp_chance INT;
	chance_sum INT;
BEGIN
	xp_chance := floor(random() * 100) + 1;
	chance_sum := 0;
	FOR result_row IN SELECT * FROM xp_chances
	LOOP
		chance_sum := chance_sum + result_row.chance;
		IF chance_sum >= xp_chance THEN
			RETURN result_row.amount;
		END IF;
	END LOOP;
END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION add_random_xp(id BIGINT)
RETURNS TABLE (rank_changed BOOLEAN, current_rank int, next_rank INT, current_xp INT) AS
$$
DECLARE
	xp INT;
BEGIN
   SELECT get_random_xp INTO xp FROM get_random_xp();
	SELECT INTO rank_changed, current_rank, next_rank, current_xp * FROM add_xp(id, xp);
	RETURN NEXT;
END;
$$ LANGUAGE plpgsql;
