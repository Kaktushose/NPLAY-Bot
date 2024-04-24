CREATE TABLE event_settings (
    guild_id BIGINT NOT NULL PRIMARY KEY,
    contest_channel_id BIGINT NOT NULL DEFAULT 0,
    contest_vote_emoji VARCHAR
);
CREATE TABLE contest_entries (
    user_id BIGINT NOT NULL PRIMARY KEY,
    votes INT NOT NULL DEFAULT 0,
    message_id BIGINT NOT NULL
);
ALTER TABLE users ADD COLUMN permissions INTEGER DEFAULT 1;
