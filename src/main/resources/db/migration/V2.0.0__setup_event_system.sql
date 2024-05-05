CREATE TABLE event_settings (
    guild_id BIGINT NOT NULL PRIMARY KEY,
    contest_channel_id BIGINT NOT NULL DEFAULT -1,
    contest_vote_emoji VARCHAR NOT NULL DEFAULT '',
    collect_event_name VARCHAR NOT NULL DEFAULT '',
    collect_currency_name VARCHAR NOT NULL DEFAULT '',
    collect_currency_emoji VARCHAR NOT NULL DEFAULT '',
    collect_loot_chance REAL NOT NULL,
    collect_event_active BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE contest_entries (
    user_id BIGINT NOT NULL PRIMARY KEY,
    votes INT NOT NULL DEFAULT 0,
    message_id BIGINT NOT NULL
);

ALTER TABLE users ADD COLUMN permissions INTEGER DEFAULT 1;

ALTER TABLE users ADD COLUMN collect_points INTEGER DEFAULT 0;

CREATE TABLE collect_rewards (
    reward_id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL DEFAULT 0,
    threshold INT NOT NULL,
    xp INT,
    role_id BIGINT,
    embed JSONB NOT NULL
);
