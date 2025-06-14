package com.github.kaktushose.nplaybot.rank;

import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.items.ItemService;
import com.github.kaktushose.nplaybot.rank.leaderboard.LeaderboardPage;
import com.github.kaktushose.nplaybot.rank.model.RankConfig;
import com.github.kaktushose.nplaybot.rank.model.RankInfo;
import com.github.kaktushose.nplaybot.rank.model.UserInfo;
import com.github.kaktushose.nplaybot.rank.model.XpChangeResult;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class RankService {

    private static final Logger log = LoggerFactory.getLogger(RankService.class);
    private final DataSource dataSource;
    private final ItemService itemService;
    private final Bot bot;
    private final Guild guild;

    public RankService(DataSource dataSource, ItemService itemService, Bot bot) {
        this.dataSource = dataSource;
        this.itemService = itemService;
        this.bot = bot;
        this.guild = bot.getGuild();
    }

    public boolean createUser(UserSnowflake user) {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("INSERT INTO users VALUES(?) ON CONFLICT DO NOTHING RETURNING user_id");
            statement.setLong(1, user.getIdLong());
            var result = statement.executeQuery();
            boolean created = result.next();
            if (created) {
                log.info("Inserting new user: {}", user);
            }
            return created;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeUser(UserSnowflake user) {
        log.info("Deleting user: {}", user);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("DELETE FROM users WHERE user_id = ?");
            statement.setLong(1, user.getIdLong());
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<RankInfo> getRankInfo(int rankId) {
        log.debug("Querying rank info for rank: {}", rankId);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT *
                    FROM ranks
                    WHERE rank_id = ?
                    """
            );
            statement.setLong(1, rankId);

            var result = statement.executeQuery();

            if (result.next()) {
                return Optional.of(new RankInfo(
                        result.getInt("rank_id"),
                        result.getLong("role_id"),
                        result.getString("name"),
                        result.getString("color"),
                        result.getInt("bound"),
                        result.getBoolean("lootbox_reward"),
                        result.getInt("item_reward_id")
                ));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public UserInfo getUserInfo(UserSnowflake user) {
        log.debug("Querying user info for user: {}", user);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT *
                    FROM users
                    WHERE user_id = ?
                    """
            );
            statement.setLong(1, user.getIdLong());

            var result = statement.executeQuery();
            result.next();

            var currentRank = getRankInfo(result.getInt("rank_id")).orElseThrow();
            var nextRank = getRankInfo(result.getInt("rank_id") + 1);

            return UserInfo.fromResultSet(result, currentRank, nextRank);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public RankConfig getRankConfig() {
        log.debug("Querying rank config");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT message_cooldown, min_message_length, xp_loot_chance
                    FROM rank_settings
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, guild.getIdLong());

            var result = statement.executeQuery();
            result.next();

            return new RankConfig(
                    result.getInt("message_cooldown"),
                    result.getInt("min_message_length"),
                    result.getDouble("xp_loot_chance")
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateCooldown(int cooldown) {
        log.info("Updating cooldown, new value {}", cooldown);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    UPDATE rank_settings
                    SET message_cooldown = ?
                    WHERE guild_id = ?
                    """
            );
            statement.setInt(1, cooldown);
            statement.setLong(2, guild.getIdLong());

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateMinMessageLength(int length) {
        log.info("Updating minimum message length, new value {}", length);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    UPDATE rank_settings
                    SET min_message_length = ?
                    WHERE guild_id = ?
                    """
            );
            statement.setInt(1, length);
            statement.setLong(2, guild.getIdLong());

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateXpLootChance(double chance) {
        log.info("Updating xp loot chance for guild, new value {}", chance);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    UPDATE rank_settings
                    SET xp_loot_chance = ?
                    WHERE guild_id = ?
                    """
            );
            statement.setDouble(1, chance);
            statement.setLong(2, guild.getIdLong());

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<Long> getValidChannels() {
        log.debug("Querying valid channels");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT valid_channels
                    FROM rank_settings
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, guild.getIdLong());

            var result = statement.executeQuery();
            result.next();
            return new HashSet<>(Arrays.asList((Long[]) result.getArray("valid_channels").getArray()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateValidChannels(Set<Long> validChannels) {
        log.debug("Querying valid channels");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    UPDATE rank_settings
                    SET valid_channels = ?
                    WHERE guild_id = ?
                    """
            );
            statement.setArray(1, connection.createArrayOf("BIGINT", validChannels.toArray()));
            statement.setLong(2, guild.getIdLong());

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isValidMessage(Message message) {
        log.debug("Checking message: {}", message);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT rank_settings.*, users.last_valid_message
                    FROM rank_settings JOIN users ON user_id = ?
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, message.getAuthor().getIdLong());
            statement.setLong(2, message.getGuildIdLong());

            var result = statement.executeQuery();
            result.next();
            var lastMessage = result.getLong("last_valid_message");
            var messageCooldown = result.getInt("message_cooldown");
            var minimumLength = result.getInt("min_message_length");

            if (System.currentTimeMillis() - lastMessage < messageCooldown) {
                log.debug("User still has cooldown ({} < {})", System.currentTimeMillis() - lastMessage, messageCooldown);
                return false;
            }

            if (message.getContentDisplay().length() < minimumLength) {
                log.debug("Message is too short (Length: {}, Required: {}", message.getContentDisplay().length(), minimumLength);
                return false;
            }

            log.debug("Message is valid");
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateValidMessage(UserSnowflake user) {
        log.debug("Setting last_valid_message for user {} to {}", user, System.currentTimeMillis());
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    UPDATE users
                    SET last_valid_message = ?,
                    message_count = message_count + 1
                    WHERE user_id = ?
                    """
            );
            statement.setLong(1, System.currentTimeMillis());
            statement.setLong(2, user.getIdLong());

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public XpChangeResult addRandomXp(UserSnowflake user) {
        log.debug("Adding random xp to user: {}", user);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("SELECT * FROM add_random_xp(?)");
            statement.setLong(1, user.getIdLong());

            var result = statement.executeQuery();
            result.next();
            log.debug("New xp: {}", result.getInt("current_xp"));
            return new XpChangeResult(
                    result.getBoolean("rank_changed"),
                    getRankInfo(result.getInt("previous_rank")),
                    getRankInfo(result.getInt("current_rank")).orElseThrow(),
                    getRankInfo(result.getInt("next_rank")),
                    result.getInt("current_xp")
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public XpChangeResult addXp(UserSnowflake user, int amount) {
        log.info("Adding {} xp to {}", amount, user);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("SELECT * FROM add_xp(?, ?)");
            statement.setLong(1, user.getIdLong());
            statement.setInt(2, amount);

            var result = statement.executeQuery();
            result.next();
            return new XpChangeResult(
                    result.getBoolean("rank_changed"),
                    getRankInfo(result.getInt("previous_rank")),
                    getRankInfo(result.getInt("current_rank")).orElseThrow(),
                    getRankInfo(result.getInt("next_rank")),
                    result.getInt("current_xp")
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public XpChangeResult setXp(UserSnowflake user, int value) {
        log.info("Setting xp of user {} to {}", user, value);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("SELECT * FROM set_xp(?, ?)");
            statement.setLong(1, user.getIdLong());
            statement.setInt(2, value);

            var result = statement.executeQuery();
            result.next();
            return new XpChangeResult(
                    result.getBoolean("rank_changed"),
                    getRankInfo(result.getInt("previous_rank")),
                    getRankInfo(result.getInt("current_rank")).orElseThrow(),
                    getRankInfo(result.getInt("next_rank")),
                    value
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void onXpChange(XpChangeResult result, Member member, EmbedCache embedCache) {
        log.info("Checking for rank change: member={}, xpChangeResult={}", member, result);

        if (!result.rankChanged()) {
            log.info("Rank hasn't changed");
            return;
        }
        log.info("Applying changes. Previous rank: {}, New rank: {}", result.previousRank(), result.currentRank());

        if (result.previousRank().isPresent()) {
            if (result.currentRank().rankId() < result.previousRank().get().rankId()) {
                var embed = getRankInfo(result.currentRank().rankId() - 1).isPresent() ? "rankDecrease" : "rankDecreaseMax";
                var message = new MessageCreateBuilder().addContent(member.getAsMention())
                        .addEmbeds(embedCache.getEmbed(embed).injectValues(result.getEmbedValues(member)).toMessageEmbed())
                        .build();
                bot.getDatabase().getSettingsService().getBotChannel().sendMessage(message).queue();
                log.info("Rank change done");
                return;
            }
        }

        Optional<Lootbox> lootbox;
        String reward = "keine Belohnung";
        if (result.currentRank().lootboxReward()) {
            lootbox = Optional.of(getRandomLootbox());
            log.info("Rank up reward: {}", lootbox.get());
            reward = "\uD83C\uDF81 eine Lootbox";
        } else {
            lootbox = Optional.empty();
        }

        Optional<Role> itemRole = Optional.empty();
        if (result.currentRank().itemRewardId() > 0) {
            itemRole = itemService.createTransaction(member, result.currentRank().itemRewardId(), "Rank Reward");
            var item = itemService.getItem(result.currentRank().itemRewardId());
            var emoji = bot.getDatabase().getItemService().getTypeEmoji(item.typeId());
            reward = String.format("%s %s", emoji, item.name());
            log.info("Rank up reward: {}", item);
        }

        updateRankRoles(member, result.currentRank(), itemRole);

        var embed = result.nextRank().isPresent() ? "rankIncrease" : "rankIncreaseMax";
        var message = new MessageCreateBuilder().addContent(member.getAsMention())
                .addEmbeds(embedCache.getEmbed(embed).injectValues(result.getEmbedValues(member)).injectValue("reward", reward).toMessageEmbed())
                .build();

        bot.getDatabase().getSettingsService().getBotChannel().sendMessage(message).queue(msg ->
                lootbox.ifPresent(it -> LootboxListener.newListener(bot, it, member, msg, true))
        );
        log.info("Rank change done");
    }

    public Lootbox getRandomLootbox() {
        log.debug("Querying random lootbox");
        try (Connection connection = dataSource.getConnection()) {
            var result = connection.prepareStatement("SELECT * FROM get_random_lootbox()").executeQuery();
            result.next();

            var statement = connection.prepareStatement("SELECT * FROM lootbox_rewards WHERE reward_id = ?");
            statement.setInt(1, result.getInt(1));
            result = statement.executeQuery();
            result.next();
            return new Lootbox(
                    result.getInt("reward_id"),
                    result.getInt("xp_reward"),
                    result.getInt("karma_reward"),
                    result.getObject("item_reward") == null ? -1 : result.getInt("item_reward")
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<RankInfo> getRankByXp(int xp) {
        log.debug("Querying rank for xp {}", xp);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("SELECT * FROM ranks WHERE ? >= bound ORDER BY bound DESC LIMIT 1");
            statement.setInt(1, xp);
            var result = statement.executeQuery();
            if (!result.next()) {
                return Optional.empty();
            }
            return getRankInfo(result.getInt("rank_id"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateRankRoles(Member member, RankInfo currentRank, Optional<Role> itemRole) {
        if (itemRole.isEmpty()) {
            updateRankRoles(member, currentRank);
            return;
        }
        var validRole = guild.getRoleById(currentRank.roleId());
        var invalidRoles = getRankRoleIds().stream()
                .map(guild::getRoleById)
                .filter(it -> it != validRole)
                .toList();
        log.debug("Updating roles for {}. Valid role: {}, invalid Roles {}", member, validRole, invalidRoles);
        guild.modifyMemberRoles(member, List.of(validRole, itemRole.get()), invalidRoles).queue();
    }

    public void updateRankRoles(Member member, RankInfo currentRank) {
        var validRole = guild.getRoleById(currentRank.roleId());
        var invalidRoles = getRankRoleIds().stream()
                .map(guild::getRoleById)
                .filter(it -> it != validRole)
                .toList();
        log.debug("Updating roles for {}. Valid role: {}, invalid Roles {}", member, validRole, invalidRoles);
        guild.modifyMemberRoles(member, List.of(validRole), invalidRoles).queue();
    }

    public List<Long> getRankRoleIds() {
        log.debug("Querying all role ids");
        try (Connection connection = dataSource.getConnection()) {
            var result = connection.prepareStatement("SELECT role_id FROM ranks").executeQuery();
            var roleIds = new ArrayList<Long>();
            while (result.next()) {
                roleIds.add(result.getLong(1));
            }
            return roleIds;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<LeaderboardPage> getLeaderboard() {
        log.debug("Querying leaderboard");
        try (Connection connection = dataSource.getConnection()) {
            var result = connection.prepareStatement("""
                    SELECT users.xp, users.user_id, ranks.role_id
                    FROM users JOIN ranks ON ranks.rank_id = users.rank_id
                    ORDER BY xp DESC;
                    """
            ).executeQuery();

            List<LeaderboardPage> pages = new ArrayList<>();
            List<LeaderboardPage.LeaderboardRow> rows = new ArrayList<>();
            int rowCount = 1;
            while (result.next()) {
                log.trace("Adding row {}", rowCount);
                if (rowCount == 11) {
                    log.trace("Page is full, starting new page");
                    pages.add(new LeaderboardPage(rows));
                    rows = new ArrayList<>();
                    rowCount = 1;
                }
                rows.add(new LeaderboardPage.LeaderboardRow(
                        result.getInt("xp"),
                        result.getLong("user_id"),
                        result.getLong("role_id")
                ));
                rowCount++;
            }

            if(pages.isEmpty() && !rows.isEmpty()) {
                pages.add(new LeaderboardPage(rows));
            }

            log.debug("Result of leaderboard query: {} pages", pages.size());
            return pages;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void resetDailyStatistics() {
        log.info("Resetting start_xp for all users");
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("UPDATE users SET start_xp = xp").execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateStatistics() {
        log.info("Updating rank statistics");
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("SELECT * FROM update_rank_statistics()").execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void increaseTotalMessageCount() {
        log.debug("Increasing total message count by one");
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("SELECT * FROM update_total_message_statistics()").execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void switchDaily(UserSnowflake user, boolean enabled) {
        log.info("Setting daily message for user {} to {}", user, enabled);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("UPDATE users SET daily_message = ? where user_id = ?");
            statement.setBoolean(1, enabled);
            statement.setLong(2, user.getIdLong());
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<UserInfo> getDailyRankInfos() {
        log.debug("Querying daily rank infos");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT *
                    FROM users
                    WHERE daily_message = true
                    """
            );
            var result = statement.executeQuery();
            var users = new ArrayList<UserInfo>();
            while (result.next()) {
                var currentRank = getRankInfo(result.getInt("rank_id")).orElseThrow();
                var nextRank = getRankInfo(result.getInt("rank_id") + 1);

                users.add(UserInfo.fromResultSet(result, currentRank, nextRank));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getXpLootDrop(Message message) {
        log.debug("Querying xp loot drop for message {}", message);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("SELECT * FROM get_xp_loot_drop(?)");
            statement.setLong(1, message.getGuildIdLong());

            var result = statement.executeQuery();
            result.next();
            var xp = result.getInt(1);
            log.debug("Xp loot drop: {} xp", xp);
            return xp;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isValidChannel(MessageChannelUnion channel) {
        log.debug("Checking channel: {}", channel);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT rank_settings.valid_channels
                    FROM rank_settings
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, guild.getIdLong());

            var result = statement.executeQuery();
            result.next();
            var validChannels = Arrays.asList((Long[]) result.getArray("valid_channels").getArray());

            var channelId = channel.getIdLong();
            if (channel.getType().isThread()) {
                channelId = channel.asThreadChannel().getParentChannel().getIdLong();
            }
            var valid = validChannels.contains(channelId);

            if (valid) {
                log.debug("Channel is valid");
            } else {
                log.debug("Invalid message channel");
            }

            return valid;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getStartDecayRankId() {
        log.debug("Querying start decay rank id");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT rank_decay_start
                    FROM rank_settings
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, guild.getIdLong());

            var result = statement.executeQuery();
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getDecayXp() {
        log.debug("Querying decay xp");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT rank_decay_xp_loss
                    FROM rank_settings
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, guild.getIdLong());

            var result = statement.executeQuery();
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getLootboxChance() {
        log.debug("Querying lootbox chance");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT lootbox_chance
                    FROM rank_settings
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, guild.getIdLong());

            var result = statement.executeQuery();
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getLootboxQueryLimit() {
        log.debug("Querying lootbox_query_limit");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT lootbox_query_limit
                    FROM rank_settings
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, guild.getIdLong());

            var result = statement.executeQuery();
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long getLastDecay() {
        log.debug("Querying last rank decay date");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT last_rank_decay
                    FROM rank_settings
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, guild.getIdLong());

            var result = statement.executeQuery();
            result.next();
            return result.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateLastDecay() {
        log.debug("Updating last rank decay date");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    UPDATE rank_settings
                    SET last_rank_decay = ?
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, System.currentTimeMillis());
            statement.setLong(2, guild.getIdLong());

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<UserInfo> getUsersForRankDecay() {
        log.debug("Querying users eligible for rank decay");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT users.* FROM users
                    JOIN rank_settings ON guild_id = ?
                    WHERE users.rank_id >= rank_settings.rank_decay_start
                    """
            );
            statement.setLong(1, guild.getIdLong());
            var resultSet = statement.executeQuery();
            List<UserInfo> result = new ArrayList<>();
            while (resultSet.next()) {
                var currentRank = getRankInfo(resultSet.getInt("rank_id")).orElseThrow();
                var nextRank = getRankInfo(resultSet.getInt("rank_id") + 1);
                result.add(UserInfo.fromResultSet(resultSet, currentRank, nextRank));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getRankDecayInterval() {
        log.debug("Querying rank decay interval");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT rank_decay_interval
                    FROM rank_settings
                    WHERE guild_id = ?
                    """
            );
            statement.setLong(1, guild.getIdLong());

            var result = statement.executeQuery();
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public record Lootbox(int id, int xpReward, int karmaReward, int itemId) {
        @Override
        public String toString() {
            return "Lootbox{" +
                   "id=" + id +
                   ", xpReward=" + xpReward +
                   ", karmaReward=" + karmaReward +
                   ", itemId=" + itemId +
                   '}';
        }

        public String rewardString(ItemService itemService) {
            var result = new StringBuilder();
            if (xpReward > 0) {
                result.append(String.format("%d XP", xpReward)).append("\n");
            }
            if (karmaReward > 0) {
                result.append(String.format("%d Karma", karmaReward)).append("\n");
            }
            if (itemId > 0) {
                result.append(itemService.getItem(itemId).name());
            }
            return result.toString();
        }
    }

}
