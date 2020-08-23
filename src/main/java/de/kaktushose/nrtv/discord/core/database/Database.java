package de.kaktushose.nrtv.discord.core.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import de.kaktushose.nrtv.discord.core.bot.Bot;
import de.kaktushose.nrtv.discord.core.config.BotConfig;
import de.kaktushose.nrtv.discord.core.config.BotConfigType;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;
import de.kaktushose.nrtv.discord.frameworks.event.EventItem;
import de.kaktushose.nrtv.discord.frameworks.event.EventPoint;
import de.kaktushose.nrtv.discord.frameworks.event.EventType;
import de.kaktushose.nrtv.discord.frameworks.level.shop.*;
import de.kaktushose.nrtv.discord.util.Leaderboard;
import de.kaktushose.nrtv.discord.util.Logging;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class Database {

    private Logger logger = Logging.getLogger();
    private boolean connected;
    private final String jdbcurl;
    private final String username;
    private final String password;

    private HikariConfig config;
    private HikariDataSource dataSource;

    public Database(String jdbcurl, String username, String password) {
        this.jdbcurl = jdbcurl;
        this.username = username;
        this.password = password;

    }

    public void connect() {
        if (!connected) {
            config = new HikariConfig();
            logger.info("Connecting to database");
            config.setJdbcUrl(jdbcurl);
            config.setUsername(username);
            config.setPassword(password);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            try {
                dataSource = new HikariDataSource(config);
                connected = true;
                logger.info("Database connection pool successfully opened");
            } catch (HikariPool.PoolInitializationException e) {
                logger.error(" Error while connecting to database", e);
                System.exit(1);
            }
        }
    }

    public void disconnect() {
        if (connected) {
            dataSource.close();
            logger.info("Database disconnected");
            connected = false;
        }
    }

    public void updateUserSet(Guild guild, Bot bot) {
        logger.info("Updating user set for guild " + guild.getName());
        List<Long> botUserIDs = getAllBotUsers().stream().map(BotUser::getId).collect(Collectors.toList());
        List<Long> memberIDs = guild.getMembers().stream().map(ISnowflake::getIdLong).collect(Collectors.toList());

        // this method only gets invoked when the bot is started (every 24h) thus the xp and coins for the last 24h statistic is set
        getAllBotUsers().forEach(botUser -> {
            botUser.setStartCoins(botUser.getCoins());
            botUser.setStartXp(botUser.getXp());
            botUser.setStartDiamonds(botUser.getDiamonds());
            setBotUser(botUser);
        });

        // goes through all guild members and checks if they are in the DB. If not, the user will be added to the DB
        memberIDs.forEach(id -> {
            if (!botUserIDs.contains(id)) {
                if (!guild.getMemberById(id).getUser().isBot()) {
                    BotUser botUser = new BotUser(id);
                    guild.addRoleToMember(guild.getMemberById(id), bot.getRoleByLevel(0)).queue();
                    botUser.setCoins(100);
                    addBotUser(botUser);
                }
            }
        });

        // goes through all BotUsers(DB) and checks if they are on the guild. If not, the user will be deleted from the DB
        botUserIDs.forEach(id -> {
            if (!memberIDs.contains(id)) {
                removeBotUser(getBotUser(id));
            }
        });

    }

    public BotUser getBotUser(long id) {
        logger.debug("resolving data for user " + id);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("select * from users where id = " + id);
            BotUser botUser = new BotUser(-1);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                botUser = new BotUser(
                        resultSet.getLong("id"),
                        resultSet.getLong("last_xp"),
                        resultSet.getLong("premium_buy_time"),
                        resultSet.getLong("booster_buy_time"),
                        resultSet.getLong("dj_buy_time"),
                        resultSet.getLong("nickname_buy_time"),
                        resultSet.getLong("xpbooster_buy_time"),
                        resultSet.getInt("role_level"),
                        resultSet.getInt("xp"),
                        resultSet.getInt("coins"),
                        resultSet.getInt("diamonds"),
                        resultSet.getInt("permission_level"),
                        resultSet.getInt("start_coins"),
                        resultSet.getInt("start_xp"),
                        resultSet.getInt("messages"),
                        resultSet.getInt("event_points"),
                        resultSet.getInt("start_diamonds"),
                        resultSet.getBoolean("daily"),
                        Collections.emptyMap());
                setItemStack(botUser, resultSet);
            }
            return botUser;
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
            return null;
        }
    }

    public List<BotUser> getAllBotUsers() {
        List<BotUser> botUsers = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("select id from users");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                botUsers.add(getBotUser(resultSet.getLong("id")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return botUsers;
    }

    public void setBotUser(BotUser botUser) {
        logger.debug("updating data for user " + botUser.getId());
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("update users set " +
                    "id = ?, " +
                    "role_level = ?," +
                    "last_xp = ?," +
                    "xp = ?," +
                    "coins = ?," +
                    "premium = ?," +
                    "booster = ?," +
                    "dj = ?," +
                    "nickname = ?," +
                    "premium_buy_time = ?," +
                    "booster_buy_time = ?," +
                    "dj_buy_time = ?," +
                    "nickname_buy_time = ?," +
                    "permission_level = ?," +
                    "start_xp = ?," +
                    "start_coins = ?," +
                    "messages = ?," +
                    "event_points = ?," +
                    "diamonds = ?," +
                    "xpbooster = ?," +
                    "xpbooster_buy_time = ?," +
                    "daily = ?," +
                    "start_diamonds = ? " +
                    "where id = ?");
            prepareStatement(botUser, statement);
            statement.setLong(24, botUser.getId());
            statement.execute();
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }
    }

    public void addBotUser(BotUser botUser) {
        if (botUser.getId() == -1) return;
        logger.debug("adding user " + botUser.getId() + " to database");
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("insert into users values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            prepareStatement(botUser, statement);
            statement.setLong(1, botUser.getId());
            statement.execute();
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }
    }

    public void removeBotUser(BotUser botUser) {
        removeBotUser(botUser.getId());
    }

    public void removeBotUser(long id) {
        logger.debug("removing user " + id + " from database");
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("delete from users where id = " + id);
            statement.execute();
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }
    }

    public Leaderboard getXpLeaderBoard() {
        Leaderboard leaderBoard = new Leaderboard();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("select id from users order by xp desc");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                leaderBoard.add(resultSet.getLong("id"));
            }
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }
        return leaderBoard;
    }

    public Leaderboard getCoinsLeaderBoard() {
        Leaderboard leaderBoard = new Leaderboard();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("select id from users order by coins desc");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                leaderBoard.add(resultSet.getLong("id"));
            }
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }
        return leaderBoard;
    }

    public Leaderboard getDiamondsLeaderBoard() {
        Leaderboard leaderBoard = new Leaderboard();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("select id from users order by diamonds desc");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                leaderBoard.add(resultSet.getLong("id"));
            }
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }
        return leaderBoard;
    }

    public List<Item> getAllItemTypes(ItemType itemType) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement;
            ResultSet resultSet;
            List<Item> items = new ArrayList<>();
            switch (itemType) {
                case NICKNAME:
                    statement = connection.prepareStatement("select * from nickname_types");
                    resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        items.add(new NicknamePerk(resultSet.getInt("price"),
                                resultSet.getString("name"),
                                "Mit dem Nickname PERK erhältst du das Recht, Deinen Nicknamen auf dem Server selbstständig jederzeit zu ändern!",
                                resultSet.getLong("duration"),
                                resultSet.getInt("id")));
                    }
                    break;
                case BOOSTER:
                    statement = connection.prepareStatement("select * from booster_types");
                    resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        items.add(new Booster(resultSet.getInt("price"),
                                resultSet.getString("name"),
                                "Mit dem Münzenbooster erhältst Du +2 Münzen je gezählter Nachricht zusätzlich - optimal für viele Münzen!",
                                resultSet.getLong("duration"),
                                resultSet.getInt("id")));
                    }
                    break;
                case DJ:
                    statement = connection.prepareStatement("select * from dj_types");
                    resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        items.add(new DJRole(resultSet.getInt("price"),
                                resultSet.getString("name"),
                                "Mit der DJ Rolle für den \"Rythm\" bist Du der Star!",
                                resultSet.getLong("duration"),
                                resultSet.getInt("id")));
                    }
                    break;
                case PREMIUM:
                    statement = connection.prepareStatement("select * from premium_types");
                    resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        items.add(new PremiumRole(resultSet.getInt("price"),
                                resultSet.getString("name"),
                                "Erhalte mit dieser besonderen Rolle satte 12 exklusive Vorteile auf unserem Server!",
                                resultSet.getLong("duration"),
                                resultSet.getInt("id")));
                    }
                    break;
                case XPBOOSTER:
                    statement = connection.prepareStatement("select * from xpbooster_types");
                    resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        items.add(new XpBooster(resultSet.getInt("price"),
                                resultSet.getString("name"),
                                " Mit dem XP-Booster erhältst Du +2 XP je gezählter Nachricht zusätzlich - steige so leichter in neue Stufen auf!",
                                resultSet.getLong("duration"),
                                resultSet.getInt("id")));
                    }
                    break;
                default:
                    return null;
            }
            return items;
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
            return null;
        }
    }

    public List<Long> getMutedChannelIds() {
        List<Long> channels = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("select * from muted_channels");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                channels.add(resultSet.getLong("id"));
            }
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }
        return channels;
    }

    public void addMutedChannel(long id) {
        logger.debug("adding channel " + id + " to muted channels");
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("insert into muted_channels values(?)");
            statement.setLong(1, id);
            statement.execute();
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }
    }

    public void removeMutedChannel(long id) {
        logger.debug("removing channel " + id + " to muted channels");
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("delete from muted_channels where id = ?");
            statement.setLong(1, id);
            statement.execute();
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }
    }

    public List<Integer> getXpBounds() {
        List<Integer> bounds = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("select bound from roles where role_level > 0");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                bounds.add(resultSet.getInt("bound"));
            }
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }
        return bounds;
    }

    public Map<Integer, Integer> getXpChances() {
        Map<Integer, Integer> chances = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("select * from xp_chances");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                chances.put(resultSet.getInt("id"), resultSet.getInt("chance"));
            }
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }
        return chances;
    }

    public Map<Integer, Integer> getCoinChances() {
        Map<Integer, Integer> chances = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("select * from coin_chances");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                chances.put(resultSet.getInt("id"), resultSet.getInt("chance"));
            }
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }
        return chances;
    }

    public Map<Integer, Integer> getDiamondChances() {
        Map<Integer, Integer> chances = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("select * from diamond_chances");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                chances.put(resultSet.getInt("id"), resultSet.getInt("chance"));
            }
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }
        return chances;
    }

    public long getRoleIdByLevel(int level) {
        long result = -1;
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("select discord_id from roles where role_level = " + level);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                result = resultSet.getLong("discord_id");
            }
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }
        return result;
    }

    public long getRoleId(Bot.Roles roles) {
        long result = -1;
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("select discord_id from item_roles where id = " + roles.id);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                result = resultSet.getLong("discord_id");
            }
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }
        return result;
    }

    public EventItem getEventItem(int id) {
        EventItem item = new EventItem(0, "", "", id, 0);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("select * from event_types where id = " + id);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                item.setPrice(resultSet.getInt("price"));
                item.setName(resultSet.getString("name"));
                item.setDescription(resultSet.getString("description"));
                item.setRoleId(resultSet.getLong("role_id"));
            }
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }


        return item;
    }

    public BotConfig getBotConfig(BotConfigType configType) {
        BotConfig botConfig = new BotConfig("", "", "", -1, -1, -1, false);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("select * from settings where id = " + configType.id);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                botConfig.setPrefix(resultSet.getString("prefix"));
                botConfig.setToken(resultSet.getString("token"));
                botConfig.setVersion(resultSet.getString("version"));
                botConfig.setGuildId(resultSet.getLong("guildid"));
                botConfig.setBotChannelId(resultSet.getLong("bot_channel"));
                botConfig.setPresentEventItem(resultSet.getInt("present_event"));
                botConfig.setEventIsPresent(resultSet.getBoolean("event_active"));
            }
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }
        return botConfig;
    }

    public EventPoint getEventPoint(EventType eventType) {
        EventPoint eventPoint = new EventPoint("", "");
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("select * from event_points where id = " + eventType.id);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                eventPoint.setName(resultSet.getString("name"));
                eventPoint.setEmote(resultSet.getString("emote"));
            }

        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }
        return eventPoint;
    }

    public Item getItemType(int id, ItemType itemType) {
        Item item;
        PreparedStatement statement;
        ResultSet resultSet;
        try (Connection connection = dataSource.getConnection()) {
            switch (itemType) {
                case NICKNAME:
                    statement = connection.prepareStatement("select * from nickname_types where id = " + id);
                    item = new NicknamePerk(0, "", "Mit dem Nickname PERK erhältst du das Recht, Deinen Nicknamen auf dem Server selbstständig jederzeit zu ändern!", 0, id);
                    break;
                case BOOSTER:
                    statement = connection.prepareStatement("select * from booster_types where id = " + id);
                    item = new Booster(0, "", "Mit dem Münzenbooster erhältst Du +2 Münzen je gezählter Nachricht zusätzlich - optimal für viele Münzen", 0, id);
                    break;
                case XPBOOSTER:
                    statement = connection.prepareStatement("select * from xpbooster_types where id = " + id);
                    item = new XpBooster(0, "", "Mit dem XP-Booster erhältst Du +2 XP je gezählter Nachricht zusätzlich - steige so leichter in neue Stufen auf!", 0, id);
                    break;
                case DJ:
                    statement = connection.prepareStatement("select * from dj_types where id = " + id);
                    item = new DJRole(0, "", "Mit der DJ Rolle für den Bot \"Rythm\" bist Du der Star!", 0, id);
                    break;
                case PREMIUM:
                    statement = connection.prepareStatement("select * from premium_types where id = " + id);
                    item = new PremiumRole(0, "", "Erhalte mit dieser besonderen Rolle satte 13 exklusive Vorteile auf unserem Server!", 0, id);
                    break;
                default:
                    return null;
            }
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                item.setName(resultSet.getString("name"));
                item.setPrice(resultSet.getInt("price"));
                item.setDuration(resultSet.getLong("duration"));
            }

        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
            return null;
        }
        return item;
    }

    private void setItemStack(BotUser botUser, ResultSet resultSet) {
        Map<ItemType, Item> itemStack = new HashMap<>();
        int id;
        Item item;
        try {
            id = resultSet.getInt("premium");
            if (id > -1) {
                item = getItemType(id, ItemType.PREMIUM);
                itemStack.put(ItemType.PREMIUM, item);
            }
            id = resultSet.getInt("booster");
            if (id > -1) {
                item = getItemType(id, ItemType.BOOSTER);
                itemStack.put(ItemType.BOOSTER, item);
            }
            id = resultSet.getInt("xpbooster");
            if (id > -1) {
                item = getItemType(id, ItemType.XPBOOSTER);
                itemStack.put(ItemType.XPBOOSTER, item);
            }
            id = resultSet.getInt("dj");
            if (id > -1) {
                item = getItemType(id, ItemType.DJ);
                itemStack.put(ItemType.DJ, item);
            }
            id = resultSet.getInt("nickname");
            if (id > -1) {
                item = getItemType(id, ItemType.NICKNAME);
                itemStack.put(ItemType.NICKNAME, item);
            }
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }
        botUser.setItemStack(itemStack);
    }

    private void prepareStatement(BotUser botUser, PreparedStatement statement) {
        try {
            statement.setLong(1, botUser.getId());
            statement.setInt(2, botUser.getLevel());
            statement.setLong(3, botUser.getLastXp());
            statement.setInt(4, botUser.getXp());
            statement.setInt(5, botUser.getCoins());
            statement.setLong(10, botUser.getPremiumBuyTime());
            statement.setLong(11, botUser.getBoosterBuyTime());
            statement.setLong(12, botUser.getDjBuyTime());
            statement.setLong(13, botUser.getNickNameBuyTime());
            statement.setInt(14, botUser.getPermissionLevel());
            statement.setInt(15, botUser.getStartXp());
            statement.setInt(16, botUser.getStartCoins());
            statement.setInt(17, botUser.getMessages());
            statement.setInt(18, botUser.getEventPoints());
            statement.setInt(19, botUser.getDiamonds());
            statement.setLong(21, botUser.getXpBoosterBuyTime());
            statement.setBoolean(22, botUser.isDaily());
            statement.setInt(23, botUser.getStartDiamonds());
            Map<ItemType, Item> itemStack = botUser.getItemStack();

            for (ItemType itemType : ItemType.values()) {
                switch (itemType) {
                    case PREMIUM:
                        if (itemStack.containsKey(ItemType.PREMIUM)) {
                            statement.setInt(6, itemStack.get(ItemType.PREMIUM).getType());
                        } else {
                            statement.setInt(6, -1);
                        }
                        break;
                    case BOOSTER:
                        if (itemStack.containsKey(ItemType.BOOSTER)) {
                            statement.setInt(7, itemStack.get(ItemType.BOOSTER).getType());
                        } else {
                            statement.setInt(7, -1);
                        }
                        break;
                    case DJ:
                        if (itemStack.containsKey(ItemType.DJ)) {
                            statement.setInt(8, itemStack.get(ItemType.DJ).getType());
                        } else {
                            statement.setInt(8, -1);
                        }
                        break;
                    case NICKNAME:
                        if (itemStack.containsKey(ItemType.NICKNAME)) {
                            statement.setInt(9, itemStack.get(ItemType.NICKNAME).getType());
                        } else {
                            statement.setInt(9, -1);
                        }
                        break;
                    case XPBOOSTER:
                        if (itemStack.containsKey(ItemType.XPBOOSTER)) {
                            statement.setInt(20, itemStack.get(ItemType.XPBOOSTER).getType());
                        } else {
                            statement.setInt(20, -1);
                        }
                        break;
                }
            }
        } catch (SQLException e) {
            logger.error("An error occurred during a sql operation", e);
        }
    }

}


