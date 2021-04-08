package de.kaktushose.nrtv.discord.core.bot;

import de.kaktushose.nrtv.discord.core.bot.commands.user.InfoCommand;
import de.kaktushose.nrtv.discord.core.config.BotConfig;
import de.kaktushose.nrtv.discord.core.config.BotConfigType;
import de.kaktushose.nrtv.discord.core.config.DatabaseConfig;
import de.kaktushose.nrtv.discord.core.database.Database;
import de.kaktushose.nrtv.discord.core.database.data.BotUser;
import de.kaktushose.nrtv.discord.frameworks.command.PermissionLevel;
import de.kaktushose.nrtv.discord.frameworks.event.EventScheduler;
import de.kaktushose.nrtv.discord.frameworks.event.PremiumReward;
import de.kaktushose.nrtv.discord.frameworks.event.RoleReward;
import de.kaktushose.nrtv.discord.frameworks.level.ItemCheck;
import de.kaktushose.nrtv.discord.frameworks.level.shop.Item;
import de.kaktushose.nrtv.discord.frameworks.level.shop.ItemType;
import de.kaktushose.nrtv.discord.util.Logging;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public abstract class Bot {

    protected String prefix;
    protected String version;
    protected JDA jda;
    protected Database database;
    protected DatabaseConfig databaseConfig;
    protected BotConfig botConfig;
    protected ItemCheck itemCheck;
    private Logger logger;
    private Guild guild;
    private BotConfigType configType;
    private EventScheduler eventScheduler;

    public Bot(DatabaseConfig databaseConfig, BotConfigType configType) {
        this.databaseConfig = databaseConfig;
        this.configType = configType;
        database = new Database(this.databaseConfig.getJdbcurl(), this.databaseConfig.getDbuser(), this.databaseConfig.getDbpassword());
        logger = Logging.getLogger();
        itemCheck = new ItemCheck(this);
//        eventScheduler = new EventScheduler(this);
//        eventScheduler.addEventReward(new PremiumReward(25, "PREMIUM basic"));
//        eventScheduler.addEventReward(new RoleReward(3, "Eventrolle Ostern 2021"));
    }

    public void postStart() {

    }

    public void start() throws LoginException, InterruptedException {
        logger.info("Starting bot...");
        database.connect();
        botConfig = database.getBotConfig(configType);
        prefix = botConfig.getPrefix();
        version = botConfig.getVersion();
        jda = JDABuilder.createDefault(botConfig.getToken(), Arrays.asList(GatewayIntent.values()))
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setActivity(Activity.playing("starting...")).build().awaitReady();
        guild = jda.getGuildById(botConfig.getGuildId());
        logger.info("Bot is running at version: " + version);
        updateUserSet();
        postStart();
    }

    public void updateUserSet() {
        logger.debug("Updating database...");
        database.updateUserSet(guild, this);
    }

    public void removeItems() {
        logger.debug("Indexing expiring items...");
        itemCheck.check(guild);
    }

    public void shutdown() {
        logger.info("Shutting down bot...");
        getBotChannel().sendMessage(new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle("Bot wurde heruntergefahren!")
                .setDescription(":wrench: Wir sind in Kürze wieder zurück!").build()).complete();
        database.disconnect();
        jda.shutdown();
        databaseConfig.saveConfig();
        logger.info("Shutdown complete");
    }

    public void restart() {
        logger.info("Restarting the bot");
        database.disconnect();
        jda.shutdown();
        databaseConfig.saveConfig();
        try {
            Runtime.getRuntime().exec("sudo reboot -r now");
        } catch (IOException e) {
            logger.error("Cannot restart the server!", e);
            System.exit(1);
        }
    }

    public void sendDMs() {
        InfoCommand infoCommand = new InfoCommand(this);
        database.getAllBotUsers().forEach(botUser -> {
            if (botUser.isDaily()) {
                Member member = guild.getMemberById(botUser.getId());
                member.getUser().openPrivateChannel().queue(
                        privateChannel -> infoCommand.sendDM(member, privateChannel),
                        new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
            }
        });
    }

    public boolean hasPermission(Member member, PermissionLevel permissionLevel) {
        int level = database.getBotUser(member.getIdLong()).getPermissionLevel();
        if (level >= permissionLevel.level) {
            return true;
        } else {
            logger.debug("Insufficient permissions! Got: " + level + " Expected: " + permissionLevel.level);
            return false;
        }
    }

    public JDA getJda() {
        return jda;
    }

    public DatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }

    public Database getDatabase() {
        return database;
    }

    public void removeDiscordRole(Member member, Role role) {
        logger.info("removing role " + role.getName() + "[" + role.getId() + "] from user: " + member.getId());
        guild.removeRoleFromMember(member, role).queue();
    }

    public void addDiscordRole(Member member, Role role) {
        logger.info("adding role " + role.getName() + "[" + role.getId() + "] to user: " + member.getId());
        guild.addRoleToMember(member, role).queue();
    }

    public void addRole(Member member, Roles roles) {
        addDiscordRole(member, jda.getRoleById(database.getRoleId(roles)));
    }

    public void removeRole(Member member, Roles roles) {
        removeDiscordRole(member, jda.getRoleById(database.getRoleId(roles)));
    }

    public void removeRoleByLevel(int level, Member member) {
        removeDiscordRole(member, getRoleByLevel(level));
    }

    public void addRoleByLevel(int level, Member member) {
        addDiscordRole(member, getRoleByLevel(level));
    }

    public Role getRoleByLevel(int level) {
        return guild.getRoleById(database.getRoleIdByLevel(level));
    }

    public Color getColor(int level) {
        return Arrays.asList(
                new Color(123, 123, 123), // Welcome
                new Color(204, 142, 52), // Bronze
                new Color(192, 192, 192), // Silber
                new Color(226, 176, 7), // Gold
                new Color(229, 228, 226), // Platin
                new Color(185, 242, 255), // Diamant
                new Color(41, 128, 105), // Veteran
                new Color(241, 128, 131), // Supreme
                new Color(250, 199, 199), // Legende
                new Color(11, 11, 11)).get(level); // Black
    }

    public void checkForPromotion(BotUser botUser, GuildMessageReceivedEvent event) {
        int xp = botUser.getXp();
        int level = botUser.getLevel();
        boolean update = false;
        String reward = "keine";

        switch (getLevelByXp(xp)) {
            case 0: { // Welcome
                if (botUser.getLevel() == 0) break;
                botUser.setLevel(0);
                botUser.setCoins(botUser.getCoins() + 100);
                reward = ":moneybag: 100 Münzen";
                update = true;
                break;
            }
            case 1: { // Bronze
                if (botUser.getLevel() == 1) break;
                botUser.setLevel(1);
                botUser.setCoins(botUser.getCoins() + 25);
                reward = ":moneybag: 25 Münzen";
                update = true;
                break;
            }
            case 2: { // Silber
                if (botUser.getLevel() == 2) break;
                botUser.setLevel(2);
                botUser.setDiamonds(botUser.getDiamonds() + 1);
                reward = ":gem: ein Diamant";
                update = true;
                break;
            }
            case 3: { // Gold
                if (botUser.getLevel() == 3) break;
                botUser.setLevel(3);
                addUpItem(botUser, database.getAllItemTypes(ItemType.BOOSTER).get(0));
                reward = ":shopping_bags: Münzenbooster basic (" + TimeUnit.MILLISECONDS.toDays(database.getAllItemTypes(ItemType.BOOSTER).get(0).getDuration()) + " Tage) :moneybag:";
                update = true;
                break;
            }
            case 4: { // Platin
                if (botUser.getLevel() == 4) break;
                botUser.setLevel(4);
                addUpItem(botUser, database.getAllItemTypes(ItemType.XPBOOSTER).get(0));
                reward = ":shopping_bags: XP-Booster basic (" + TimeUnit.MILLISECONDS.toDays(database.getAllItemTypes(ItemType.XPBOOSTER).get(0).getDuration()) + " Tage) :star2:";
                update = true;
                break;
            }
            case 5: { // Diamant
                if (botUser.getLevel() == 5) break;
                botUser.setLevel(5);
                botUser.setDiamonds(botUser.getDiamonds() + 3);
                reward = ":gem: 3 Diamanten";
                update = true;
                break;
            }
            case 6: { // Veteran
                if (botUser.getLevel() == 6) break;
                botUser.setLevel(6);
                addUpItem(botUser, database.getAllItemTypes(ItemType.PREMIUM).get(1));
                reward = ":shopping_bags: PREMIUM basic (" + TimeUnit.MILLISECONDS.toDays(database.getAllItemTypes(ItemType.PREMIUM).get(1).getDuration()) + " Tage) :star:";
                addRole(guild.getMemberById(botUser.getId()), Roles.PREMIUM);
                update = true;
                break;
            }
            case 7: { // Supreme
                if (botUser.getLevel() == 7) break;
                botUser.setLevel(7);
                botUser.setCoins(botUser.getCoins() + 200);
                reward = ":moneybag: 200 Münzen";
                update = true;
                break;
            }
            case 8: { // Legende
                if (botUser.getLevel() == 8) break;
                botUser.setLevel(8);
                botUser.setDiamonds(botUser.getDiamonds() + 6);
                reward = ":gem: 6 Diamanten";
                update = true;
                break;
            }
            case 9: { // Black
                if (botUser.getLevel() == 9) break;
                botUser.setLevel(9);
                botUser.setCoins(botUser.getCoins() + 300);
                reward = ":moneybag: 300 Münzen";
                update = true;
                break;
            }
        }

        database.setBotUser(botUser);

        if (update) {
            Member member = event.getGuild().getMemberById(botUser.getId());

            logger.debug("removing role by level: " + level + " for user: " + member.getIdLong());
            removeRoleByLevel(level, member);
            logger.debug("removing role by level: " + botUser.getLevel() + " for user: " + member.getIdLong());
            addRoleByLevel(botUser.getLevel(), member);

            EmbedBuilder builder = new EmbedBuilder();
            getBotChannel().sendMessage(member.getAsMention()).queue();

            builder.setTitle(":arrow_up: Stufenaufstieg!")
                    .setDescription(event.getMember().getAsMention())
                    .setColor(getColor(botUser.getLevel()))
                    .addField("Neue Stufe:", ":level_slider: " + getRoleByLevel(botUser.getLevel()).getAsMention(), false)
                    .addField("Einmalige Belohnung:", reward, false);

            if (botUser.getLevel() < 9) {
                builder.addField("Nächste Stufe:", ":dart: " + getRoleByLevel(botUser.getLevel() + 1).getAsMention() + " (ab " + database.getXpBounds().get(botUser.getLevel()) + " XP)", false);
            }

            getBotChannel().sendMessage(builder.build()).queue();
        }
    }

    public void addUpItem(BotUser botUser, Item item) {
        ItemType itemType = item.getItemType();
        if (botUser.hasItem(itemType)) {
            botUser.setBuyTime(botUser.getBuyTime(itemType) + item.getDuration(), itemType);
        } else {
            botUser.setBuyTime(System.currentTimeMillis(), itemType);
            botUser.getItemStack().put(itemType, item);
            if (itemType == ItemType.PREMIUM) addRole(guild.getMemberById(botUser.getId()), Roles.PREMIUM);
        }
    }

    public int getNewXp() {
        Map<Integer, Integer> chances = database.getXpChances();
        int random = ThreadLocalRandom.current().nextInt(1, 101);
        for (int key : chances.keySet()) {
            if (random <= chances.get(key)) {
                return key;
            }
        }
        return 0;
    }

    public int getNewCoins() {
        Map<Integer, Integer> chances = database.getCoinChances();
        int random = ThreadLocalRandom.current().nextInt(1, 101);
        for (int key : chances.keySet()) {
            if (random <= chances.get(key)) {
                return key;
            }
        }
        return 0;
    }

    public int getNewDiamonds() {
        Map<Integer, Integer> chances = database.getDiamondChances();
        int random = ThreadLocalRandom.current().nextInt(1, 101);
        for (int key : chances.keySet()) {
            if (random <= chances.get(key)) {
                return key;
            }
        }
        return 0;
    }

    @SuppressWarnings("all")
    public int getLevelByXp(int xp) {
        List<Integer> bounds = database.getXpBounds();
        if (xp < bounds.get(0)) { // < Bronze
            return 0;
        } else if (xp < bounds.get(1)) { //  Silver
            return 1;
        } else if (xp < bounds.get(2)) { //  Gold
            return 2;
        } else if (xp < bounds.get(3)) { // Platin
            return 3;
        } else if (xp < bounds.get(4)) { // Diamant
            return 4;
        } else if (xp < bounds.get(5)) { // Veteran
            return 5;
        } else if (xp < bounds.get(6)) { // Supreme
            return 6;
        } else if (xp < bounds.get(7)) { // Legende
            return 7;
        } else if (xp < bounds.get(8)) { // < Black
            return 8;
        } else if (xp >= bounds.get(8)) { // >= Black
            return 9;
        } else { // default case
            return -1;
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public TextChannel getBotChannel() {
        return jda.getTextChannelById(botConfig.getBotChannelId());
    }

    public String getVersion() {
        return version;
    }

    public Guild getGuild() {
        return guild;
    }

    public int getPresentEventItem() {
        return botConfig.getPresentEventItem();
    }

    public boolean eventItemIsPresent() {
        return botConfig.eventItemIsPresent();
    }

    public boolean eventIsPresent() {
        return botConfig.eventIsPresent();
    }

    public EventScheduler getEventScheduler() {
        return eventScheduler;
    }

    public enum Roles {
        DJ(0),
        PREMIUM(1),
        NICKNAME(2);
        public final long id;

        Roles(long id) {
            this.id = id;
        }

    }
}


