DELIMITER //
-- clear old database
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;


-- create tables
CREATE TABLE bot_settings (
    guild_id BIGINT NOT NULL PRIMARY KEY,
    bot_channel_id BIGINT NOT NULL DEFAULT -1,
    log_channel_id BIGINT NOT NULL DEFAULT -1
);

CREATE TABLE rank_settings (
    guild_id BIGINT NOT NULL PRIMARY KEY,
    message_cooldown BIGINT NOT NULL DEFAULT 600000,
    min_message_length INT NOT NULL DEFAULT 10,
    valid_channels BIGINT[] NOT NULL DEFAULT ARRAY[]::BIGINT[],
    xp_loot_chance REAL NOT NULL DEFAULT 2.3,
    rank_decay_start INT NOT NULL DEFAULT 8,
    ranK_decay_xp_loss INT NOT NULL DEFAULT 150,
    lootbox_chance REAL NOT NULL DEFAULT 1.0,
    lootbox_query_limit INT NOT NULL DEFAULT 30
);

CREATE TABLE ranks (
    rank_id INT NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    color VARCHAR(255) NOT NULL,
    bound INT NOT NULL,
    role_id BIGINT NOT NULL,
    lootbox_reward BOOLEAN NOT NULL,
    item_reward_id INT NOT NULL
);

CREATE TABLE users (
    user_id BIGINT NOT NULL PRIMARY KEY,
    daily_message BOOLEAN NOT NULL DEFAULT FALSE,
    xp INT NOT NULL DEFAULT 0,
    rank_id INT NOT NULL REFERENCES ranks(rank_id) DEFAULT 1,
    last_valid_message BIGINT NOT NULL DEFAULT 0,
    message_count BIGINT NOT NULL DEFAULT 0,
    start_xp INT NOT NULL DEFAULT 0,
    karma_points INT NOT NULL DEFAULT 0,
    karma_tokens INT NOT NULL DEFAULT 5,
    last_karma INT NOT NULL DEFAULT 0,
    permissions INTEGER DEFAULT 1,
    collect_points INTEGER DEFAULT 0
);

CREATE TABLE xp_chances (
    chance_id SERIAL NOT NULL PRIMARY KEY,
    amount INT NOT NULL,
    chance INT NOT NULL
);

CREATE TABLE rank_statistics (
    date date NOT NULL PRIMARY KEY,
    total_message_count INT NOT NULL DEFAULT 0,
    valid_message_count INT NOT NULL DEFAULT 0,
    total_xp_gain INT NOT NULL DEFAULT 0,
    total_rank_ups INT NOT NULL DEFAULT 0
);

CREATE TABLE event_settings (
    guild_id BIGINT NOT NULL PRIMARY KEY,
    contest_channel_id BIGINT NOT NULL DEFAULT -1,
    contest_vote_emoji VARCHAR NOT NULL DEFAULT '',
    collect_event_name VARCHAR NOT NULL DEFAULT '',
    collect_currency_name VARCHAR NOT NULL DEFAULT '',
    collect_currency_emoji VARCHAR NOT NULL DEFAULT '',
    collect_loot_chance REAL NOT NULL DEFAULT 10,
    collect_event_active BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE contest_entries (
    user_id BIGINT NOT NULL PRIMARY KEY,
    votes INT NOT NULL DEFAULT 0,
    message_id BIGINT NOT NULL
);

CREATE TABLE collect_rewards (
    reward_id SERIAL NOT NULL PRIMARY KEY,
    name VARCHAR NOT NULL DEFAULT 0,
    threshold INT NOT NULL,
    xp INT NOT NULL DEFAULT 0,
    role_id BIGINT NOT NULL DEFAULT 0,
    embed JSONB NOT NULL
);

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL PRIMARY KEY,
    permissions INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE karma_settings (
   guild_id BIGINT NOT NULL PRIMARY KEY,
   VALID_EMOJIS VARCHAR[] NOT NULL DEFAULT ARRAY[]::VARCHAR[],
   default_tokens INT NOT NULL DEFAULT 5,
   play_activity_threshold INT NOT NULL DEFAULT 30
);

CREATE TABLE karma_rewards (
    reward_id SERIAL NOT NULL PRIMARY KEY,
    name VARCHAR NOT NULL DEFAULT 0,
    threshold INT NOT NULL,
    xp INT NOT NULL DEFAULT 0,
    role_id BIGINT NOT NULL DEFAULT 0,
    embed JSONB NOT NULL
);

CREATE TABLE starboard_settings (
    guild_id BIGINT NOT NULL PRIMARY KEY,
    channel_id BIGINT NOT NULL DEFAULT 0,
    threshold INT NOT NULL DEFAULT 5,
    karma_reward INT NOT NULL DEFAULT 5
);

CREATE TABLE starboard_entries (
    message_id BIGINT NOT NULL PRIMARY KEY,
    post_id BIGINT NOT NULL DEFAULT -1,
    is_rewarded BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE item_types(
    base_type_id INT NOT NULL PRIMARY KEY,
    name VARCHAR NOT NULL,
    emoji VARCHAR NOT NULL,
    role_id BIGINT NOT NULL
);

CREATE TABLE items(
    item_id INT NOT NULL PRIMARY KEY,
    type_id INT NOT NULL REFERENCES item_types(base_type_id),
    name VARCHAR NOT NULL,
    duration BIGINT
);

CREATE TABLE transactions(
    transaction_id SERIAL NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_id INT NOT NULL REFERENCES items(item_id),
    expires_at BIGINT NOT NULL,
    is_play_activity BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE lootbox_rewards (
    reward_id SERIAL NOT NULL PRIMARY KEY,
    xp_reward INT NOT NULL DEFAULT -1,
    karma_reward INT NOT NULL DEFAULT -1,
    item_reward INT REFERENCES items(item_id),
    chance INT NOT NULL DEFAULT 0
);

-- create functions


-- update rank trigger
CREATE FUNCTION update_ranks()
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
EXECUTE FUNCTION update_ranks();


-- xp modification
CREATE FUNCTION set_xp(id BIGINT, new_xp INT)
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

CREATE FUNCTION add_xp(id BIGINT, xp_to_add INT)
RETURNS TABLE (rank_changed BOOLEAN, previous_rank int, current_rank int, next_rank INT, current_xp INT) AS
$$
DECLARE
BEGIN
    SELECT xp_to_add + users.xp INTO current_xp FROM users WHERE users.user_id = id;
    SELECT INTO rank_changed, previous_rank, current_rank, next_rank * FROM set_xp(id, current_xp);
	RETURN NEXT;
END;
$$ LANGUAGE plpgsql;


-- random xp generation
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
RETURNS TABLE (rank_changed BOOLEAN, previous_rank int, current_rank int, next_rank INT, current_xp INT) AS
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
	SELECT INTO rank_changed, previous_rank, current_rank, next_rank, current_xp * FROM add_xp(id, xp + bonus_xp);
	RETURN NEXT;
END;
$$ LANGUAGE plpgsql;


-- loot drops


CREATE FUNCTION get_xp_loot_drop(id BIGINT)
RETURNS TABLE (xp INT) AS
$$
DECLARE
	chance INT;
	drop_chance REAL;
BEGIN
	SELECT rank_settings.xp_loot_chance INTO drop_chance FROM rank_settings WHERE guild_id = id;
	chance := floor(random() * 100) + 1;
	IF ROUND(drop_chance) >= chance THEN
		RETURN QUERY SELECT get_random_xp FROM get_random_xp();
	ELSE
		RETURN QUERY SELECT 0;
	END IF;
END;
$$ LANGUAGE plpgsql;

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

CREATE FUNCTION get_collect_loot_drop(id BIGINT)
RETURNS TABLE (points INT) AS
$$
DECLARE
	chance INT;
	drop_chance REAL;
BEGIN
	SELECT event_settings.collect_loot_chance INTO drop_chance FROM event_settings WHERE guild_id = id;
	chance := floor(random() * 100) + 1;
	IF ROUND(drop_chance) >= chance THEN
		RETURN QUERY SELECT 1;
	ELSE
		RETURN QUERY SELECT 0;
	END IF;
END;
$$ LANGUAGE plpgsql;


-- permissions
CREATE FUNCTION get_role_permissions(ids BIGINT[])
RETURNS TABLE (permissions INT) AS
$$
DECLARE
	combined_permissions INTEGER := 0;
	role_permission INTEGER;
	id BIGINT;
BEGIN
    FOREACH id IN ARRAY ids LOOP
        SELECT role_permissions.permissions INTO role_permission FROM role_permissions WHERE role_permissions.role_id = id;
        IF NOT FOUND THEN
            CONTINUE;
        END IF;
        combined_permissions := combined_permissions | role_permission;
    END LOOP;
	RETURN QUERY SELECT combined_permissions;
END;
$$ LANGUAGE plpgsql;


-- statistics


-- valid message statistics
CREATE FUNCTION update_valid_message_statistics()
RETURNS TRIGGER AS
$$
BEGIN
	INSERT INTO rank_statistics (DATE, valid_message_count) VALUES (CURRENT_DATE, NEW.message_count - OLD.message_count)
	ON CONFLICT (DATE) DO UPDATE SET valid_message_count = rank_statistics.valid_message_count + (NEW.message_count - OLD.message_count);
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER valid_message_trigger
BEFORE UPDATE OF message_count ON users
FOR EACH ROW
EXECUTE FUNCTION update_valid_message_statistics();


-- xp gain statistics
CREATE FUNCTION update_xp_gain_statistics()
RETURNS TRIGGER AS
$$
BEGIN
	INSERT INTO rank_statistics (DATE, total_xp_gain) VALUES (CURRENT_DATE, NEW.xp - OLD.xp)
	ON CONFLICT (DATE) DO UPDATE SET total_xp_gain = rank_statistics.total_xp_gain + (NEW.xp - OLD.xp);
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER xp_gain_trigger
BEFORE UPDATE OF xp ON users
FOR EACH ROW
EXECUTE FUNCTION update_xp_gain_statistics();


-- rank up statistics
CREATE FUNCTION update_rank_ups_statistics()
RETURNS TRIGGER AS
$$
BEGIN
	INSERT INTO rank_statistics (DATE, total_rank_ups) VALUES (CURRENT_DATE, NEW.rank_id - OLD.rank_id)
	ON CONFLICT (DATE) DO UPDATE SET total_rank_ups = rank_statistics.total_rank_ups + (NEW.rank_id - OLD.rank_id);
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER rank_up_trigger
AFTER UPDATE OF rank_id ON users
FOR EACH ROW
EXECUTE FUNCTION update_rank_ups_statistics();


-- total message statistics
CREATE FUNCTION update_total_message_statistics()
RETURNS VOID AS
$$
BEGIN
	INSERT INTO rank_statistics (DATE, total_message_count) VALUES (CURRENT_DATE, 1)
	ON CONFLICT (DATE) DO UPDATE SET total_message_count = rank_statistics.total_message_count + 1;
END;
$$ LANGUAGE plpgsql;


-- insert data


-- items
INSERT INTO item_types(base_type_id, name, emoji, role_id) VALUES (1, 'XP-Booster', ':moneybag:', -1);
INSERT INTO item_types(base_type_id, name, emoji, role_id) VALUES (2, 'Premium', ':star:', -1);

INSERT INTO items(item_id, type_id, name, duration) VALUES(1, 1, 'XP-Booster 7 Tage', 604800000);
INSERT INTO items(item_id, type_id, name, duration) VALUES(2, 1, 'XP-Booster 14 Tage', 1209600000);
INSERT INTO items(item_id, type_id, name, duration) VALUES(3, 2, 'Premium 1 Tag', 86400000);
INSERT INTO items(item_id, type_id, name, duration) VALUES(4, 2, 'Premium 3 Tage', 259200000);
INSERT INTO items(item_id, type_id, name, duration) VALUES(5, 2, 'Premium 7 Tage', 604800000);
INSERT INTO items(item_id, type_id, name, duration) VALUES(6, 2, 'Premium 15 Tage', 1296000000);
INSERT INTO items(item_id, type_id, name, duration) VALUES(7, 2, 'Premium 30 Tage', 2592000000);
INSERT INTO items(item_id, type_id, name, duration) VALUES(8, 2, 'Premium 60 Tage', 5184000000);


-- lootbox_rewards
INSERT INTO lootbox_rewards(item_reward, chance) VALUES(1, 7);
INSERT INTO lootbox_rewards(item_reward, chance) VALUES(2, 3);
INSERT INTO lootbox_rewards(item_reward, chance) VALUES(3, 9);
INSERT INTO lootbox_rewards(item_reward, chance) VALUES(4, 7);
INSERT INTO lootbox_rewards(item_reward, chance) VALUES(5, 6);
INSERT INTO lootbox_rewards(item_reward, chance) VALUES(6, 4);
INSERT INTO lootbox_rewards(item_reward, chance) VALUES(7, 3);
INSERT INTO lootbox_rewards(item_reward, chance) VALUES(8, 1);

INSERT INTO lootbox_rewards(xp_reward, chance) VALUES(10, 25);
INSERT INTO lootbox_rewards(xp_reward, chance) VALUES(20, 15);
INSERT INTO lootbox_rewards(xp_reward, chance) VALUES(50, 10);

INSERT INTO lootbox_rewards(karma_reward, chance) VALUES(1, 5);
INSERT INTO lootbox_rewards(karma_reward, chance) VALUES(2, 3);
INSERT INTO lootbox_rewards(karma_reward, chance) VALUES(3, 2);


-- xp_chances
INSERT INTO xp_chances(amount, chance) VALUES(1, 10);
INSERT INTO xp_chances(amount, chance) VALUES(2, 20);
INSERT INTO xp_chances(amount, chance) VALUES(3, 40);
INSERT INTO xp_chances(amount, chance) VALUES(4, 20);
INSERT INTO xp_chances(amount, chance) VALUES(5, 10);


-- ranks
INSERT INTO ranks VALUES(1, 'Treckertourist', '#ffffff', 0, -1, false, -1);
INSERT INTO ranks VALUES(2, 'Agrareinsteiger', '#351700', 5, -1, false, 1);
INSERT INTO ranks VALUES(3, 'Ackerbursche', '#441f00', 30, -1, false, 3);
INSERT INTO ranks VALUES(4, 'Scheunendrescher', '#60350a', 50, -1, true, -1);
INSERT INTO ranks VALUES(5, 'Pferdeflüsterer', '#7c4b19', 100, -1, false, 4);
INSERT INTO ranks VALUES(6, 'Ackerkünstler', '#9f7041', 170, -1, true, -1);
INSERT INTO ranks VALUES(7, 'Grubbermeister', '#b58e67', 250, -1, false, 5);
INSERT INTO ranks VALUES(8, 'Hofleiter', '#7d7036', 350, -1, true, -1);
INSERT INTO ranks VALUES(9, 'Grünland-Guru', '#7e944a', 500, -1, false, 2);
INSERT INTO ranks VALUES(10, 'Forstveteran', '#62862f', 700, -1, true, -1);
INSERT INTO ranks VALUES(11, 'XXL-Farmer', '#2f8631', 1100, -1, false, 6);
INSERT INTO ranks VALUES(12, 'Agrarvisionär', '#2f8631', 1700, -1, true, -1);
INSERT INTO ranks VALUES(13, 'Ackerdemiker', '#064027', 2500, -1, false, 7);
INSERT INTO ranks VALUES(14, 'Landlegende', '#003700', 3500, -1, true, -1);
INSERT INTO ranks VALUES(15, 'Ackerdemiker', '#dbaa19', 5000, -1, false, 8);
