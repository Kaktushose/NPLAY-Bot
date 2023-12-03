create table guild_settings (
    guild_id bigint(20) not null primary key,
    bot_token varchar(255) not null,
    bot_channel_id bigint(20) not null,
    log_channel_id bigint(20) not null,
    message_cooldown int(11) not null
);
create table users (
    user_id bigint(20) not null primary key,
    permission_level int(11) not null,
    daily_message boolean not null,
    xp int(11) not null,
    rank int(11) not null,
    last_valid_message bigint(20) not null,
    message_count bigint(20) not null,
    start_xp int(11) not null,
);
create table ranks (
    rank_id int(11) not null primary key,
    name varchar(255) not null,
    color varchar(255) not null,
    bound int(11) not null,
    role_id bigint(20) not null
);
create table xp_chances (
    amount int (11) not null primary key,
    chance int(11) not null
);
create table rank_statistics (
    timestamp bigint(20) not null primary key,
    total_message_count int(11) not null,
    valid_message_count int (11) not null,
    total_xp_gain int(11) not null,
    total_rank_ups int (11) not null
)
