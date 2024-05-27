CREATE TABLE starboard_settings (
    guild_id BIGINT NOT NULL PRIMARY KEY,
    channel_id BIGINT NOT NULL DEFAULT 0,
    threshold INT NOT NULL DEFAULT 5,
    karma_reward INT NOT NULL DEFAULT 5
);

CREATE TABLE starboard_entries (
    message_id BIGINT PRIMARY KEY NOT NULL,
    post_id BIGINT NOT NULL DEFAULT -1,
    is_rewarded BOOLEAN NOT NULL DEFAULT false
);
