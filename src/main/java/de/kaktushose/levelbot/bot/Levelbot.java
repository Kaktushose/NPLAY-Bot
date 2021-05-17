package de.kaktushose.levelbot.bot;

import com.github.kaktushose.jda.commands.annotations.Produces;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.api.JsonEmbedFactory;
import com.github.kaktushose.jda.commands.entities.JDACommands;
import com.github.kaktushose.jda.commands.entities.JDACommandsBuilder;
import de.kaktushose.discord.reactionwaiter.ReactionListener;
import de.kaktushose.levelbot.database.model.BotUser;
import de.kaktushose.levelbot.database.model.Item;
import de.kaktushose.levelbot.database.model.Rank;
import de.kaktushose.levelbot.database.model.Transaction;
import de.kaktushose.levelbot.database.services.BoosterService;
import de.kaktushose.levelbot.database.services.LevelService;
import de.kaktushose.levelbot.database.services.SettingsService;
import de.kaktushose.levelbot.database.services.UserService;
import de.kaktushose.levelbot.listener.JoinLeaveListener;
import de.kaktushose.levelbot.listener.LevelListener;
import de.kaktushose.levelbot.listener.NitroBoosterListener;
import de.kaktushose.levelbot.listener.VoiceTextLink;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Levelbot {

    private static final Logger log = LoggerFactory.getLogger(Levelbot.class);
    private final UserService userService;
    private final SettingsService settingsService;
    private final LevelService levelService;
    private final BoosterService boosterService;
    private final EmbedCache embedCache;
    private final TaskScheduler taskScheduler;
    private final long guildId;
    private JDACommands jdaCommands;
    private JDA jda;
    private Guild guild;
    private TextChannel botChannel;

    public Levelbot(GuildType guildType) {
        userService = new UserService();
        settingsService = new SettingsService();
        levelService = new LevelService(userService, settingsService);
        boosterService = new BoosterService(userService, settingsService);
        guildId = guildType.id;
        embedCache = new EmbedCache(new File("commandEmbeds.json"));
        taskScheduler = new TaskScheduler();
    }

    public Levelbot start() throws LoginException, InterruptedException {
        log.info("Bot is running at version {}", settingsService.getVersion(guildId));

        embedCache.loadEmbedsToCache();

        log.debug("Starting jda...");
        String token = settingsService.getBotToken(guildId);
        jda = JDABuilder.create(
                token,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_PRESENCES
        ).disableCache(
                CacheFlag.ACTIVITY,
                CacheFlag.EMOTE,
                CacheFlag.CLIENT_STATUS
        ).setChunkingFilter(
                ChunkingFilter.ALL
        ).setMemberCachePolicy(
                MemberCachePolicy.ALL
        ).setActivity(
                Activity.playing("starting...")
        ).setStatus(
                OnlineStatus.DO_NOT_DISTURB
        ).build().awaitReady();

        log.debug("Registering event listeners...");
        jda.addEventListener(
                new JoinLeaveListener(this),
                new LevelListener(this),
                new VoiceTextLink(jda.getTextChannelById(839150041955565588L)),
                new NitroBoosterListener(this)
        );

        log.debug("Starting ReactionListener...");
        ReactionListener.setAutoRemove(true);
        ReactionListener.setAutoRemoveDelay(60, TimeUnit.SECONDS);
        ReactionListener.startListening(jda);

        log.debug("Starting jda-commands...");
        jdaCommands = new JDACommandsBuilder(jda)
                .setEmbedFactory(new JsonEmbedFactory(new File("jdacEmbeds.json")))
                .addProvider(this)
                .setCommandPackage("de.kaktushose.levelbot.commands")
                .build();
        jdaCommands.getDefaultSettings().setPrefix(settingsService.getBotPrefix(guildId)).getHelpLabels().add("hilfe");

        log.debug("Applying permissions...");
        userService.getUsersByPermission(0).forEach(botUser ->
                jdaCommands.getDefaultSettings().getMutedUsers().add(botUser.getUserId())
        );
        userService.getUsersByPermission(2).forEach(botUser ->
                jdaCommands.getDefaultSettings().getPermissionHolders("moderator").add(botUser.getUserId())
        );
        userService.getUsersByPermission(3).forEach(botUser -> {
                    jdaCommands.getDefaultSettings().getPermissionHolders("moderator").add(botUser.getUserId());
                    jdaCommands.getDefaultSettings().getPermissionHolders("admin").add(botUser.getUserId());
                }
        );
        userService.getUsersByPermission(4).forEach(botUser -> {
                    jdaCommands.getDefaultSettings().getPermissionHolders("moderator").add(botUser.getUserId());
                    jdaCommands.getDefaultSettings().getPermissionHolders("admin").add(botUser.getUserId());
                    jdaCommands.getDefaultSettings().getPermissionHolders("owner").add(botUser.getUserId());
                }
        );

        guild = jda.getGuildById(guildId);
        botChannel = guild.getTextChannelById(settingsService.getBotChannelId(guildId));

        taskScheduler.addRepetitiveTask(() -> {
            try {
                checkForExpiredItems();
                dmRankInfo();
                checkForNitroBoostersRewards();
            } catch (Throwable t) {
                log.error("An exception has occurred while executing daily tasks!", t);
            }
        }, 0, 1, TimeUnit.DAYS);

        jda.getPresence().setStatus(OnlineStatus.ONLINE);
        jda.getPresence().setActivity(Activity.playing("development"));

//        getBotChannel().sendMessage(embedCache.getEmbed("botStart")
//                .injectValue("version", settingsService.getVersion(guildId))
//                .toMessageEmbed()
//        ).queue();
        return this;
    }

    public Levelbot stop() {
        getBotChannel().sendMessage(embedCache.getEmbed("botStop").toMessageEmbed()).complete();
        jdaCommands.shutdown();
        jda.shutdown();
        return this;
    }

    public void terminate(int status) {
        System.exit(0);
    }

    public Levelbot indexMembers() {
        List<Member> guildMembers = guild.getMembers();
        guildMembers.forEach(member -> userService.createUserIfAbsent(member.getIdLong()));

        List<Long> guildMemberIds = guildMembers.stream().map(ISnowflake::getIdLong).collect(Collectors.toList());
        userService.getAllUsers().forEach(botUser -> {
            if (!guildMemberIds.contains(botUser.getUserId())) {
                userService.deleteUser(botUser.getUserId());
            }
        });
        return this;
    }

    public void addRankRole(long userId, int rankId) {
        Rank rank = levelService.getRank(rankId);
        guild.addRoleToMember(userId, guild.getRoleById(rank.getRoleId())).queue();
    }

    public void removeRankRole(long userId, int rankId) {
        Rank rank = levelService.getRank(rankId);
        guild.removeRoleFromMember(userId, guild.getRoleById(rank.getRoleId())).queue();
    }

    public void addItemRole(long userId, int itemId) {
        Item item = levelService.getItem(itemId);
        if (item.getRoleId() == -1) {
            return;
        }
        guild.addRoleToMember(userId, guild.getRoleById(item.getRoleId())).queue();
    }

    public void removeItemRole(long userId, int itemId) {
        Item item = levelService.getItem(itemId);
        guild.removeRoleFromMember(userId, guild.getRoleById(item.getRoleId())).queue();
    }

    public void checkForExpiredItems() {
        userService.getAllUsers().forEach(botUser -> {
            for (Transaction transaction : botUser.getTransactions()) {
                Item item = transaction.getItem();
                long remaining = item.getRemainingTimeAsLong(transaction.getBuyTime());
                long userId = botUser.getUserId();
                int itemId = item.getItemId();
                if (remaining < 0) {
                    userService.removeItem(userId, itemId);
                    removeItemRole(userId, itemId);
                    sendItemExpiredInformation(userId, itemId, transaction.getBuyTime());
                } else if (remaining < 86400000) {
                    taskScheduler.addSingleTask(() -> {
                        try {
                            userService.removeItem(userId, itemId);
                            removeItemRole(userId, itemId);
                            sendItemExpiredInformation(userId, itemId, transaction.getBuyTime());
                        } catch (Throwable t) {
                            log.error("An exception has occurred while removing an item!", t);
                        }
                    }, remaining, TimeUnit.MILLISECONDS);
                }
            }
        });
    }

    public void dmRankInfo() {
        userService.getUsersByDailyEnabled().forEach(botUser -> {
            User user = jda.getUserById(botUser.getUserId());
            user.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(generateRankInfo(user).build()))
                    .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
        });
    }

    public void checkForNitroBoostersRewards() {
        boosterService.getActiveNitroBoosters().forEach(nitroBooster -> {
            if (TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - nitroBooster.getBoostStart()) % 30 == 0) {
                boosterService.addMonthlyReward(nitroBooster.getUserId());
            }
        });
    }

    public void sendItemExpiredInformation(long userId, int itemId, long buyTime) {
        User user = jda.getUserById(userId);
        Item item = levelService.getItem(itemId);
        EmbedBuilder embed = embedCache.getEmbed("itemExpired")
                .injectValue("item", item.getName())
                .injectValue("date", new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(buyTime)))
                .toEmbedBuilder();
        user.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(embed.build()))
                .queue(null, new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER, exception -> {
                    TextChannel channel = getBotChannel();
                    channel.sendMessage(user.getAsMention()).and(channel.sendMessage(embed.build())).queue();
                }));
    }

    public EmbedBuilder generateRankInfo(User target) {
        BotUser botUser = userService.getUserById(target.getIdLong());

        Rank currentRank = levelService.getCurrentRank(botUser.getUserId());
        Rank nextRank = levelService.getNextRank(botUser.getUserId());
        long nextRankXp = nextRank.getBound() - botUser.getXp();
        long xpGain = botUser.getXp() - botUser.getStartXp();
        long coinsGain = botUser.getCoins() - botUser.getStartCoins();
        long diamondsGain = botUser.getDiamonds() - botUser.getStartDiamonds();

        EmbedBuilder embedBuilder = embedCache.getEmbed("rankInfo")
                .injectValue("user", target.getAsMention())
                .injectValue("color", currentRank.getColor())
                .injectValue("currentRank", String.format("<@&%d>", currentRank.getRoleId()))
                .injectValue("nextRank", String.format("<@&%d> (noch %d XP)", nextRank.getRoleId(), nextRankXp))
                .injectValue("avatarUrl", target.getAvatarUrl())
                .injectValue("xpGain", xpGain)
                .injectValue("coinsGain", coinsGain)
                .injectValue("diamondsGain", diamondsGain)
                .injectFields(botUser)
                .toEmbedBuilder();

        if (botUser.getTransactions().isEmpty()) {
            embedBuilder.addField("Keine Items in Besitz", "", false);
        }

        botUser.getTransactions().forEach(transaction -> {
            Item item = transaction.getItem();
            embedBuilder.addField(item.getName(), item.getRemainingTimeAsDate(transaction.getBuyTime()), false);
        });

        return embedBuilder;
    }

    @Produces
    public UserService getUserService() {
        return userService;
    }

    @Produces
    public LevelService getLevelService() {
        return levelService;
    }

    @Produces
    public SettingsService getSettingsService() {
        return settingsService;
    }

    @Produces
    public BoosterService getBoosterService() {
        return boosterService;
    }

    @Produces
    public JDA getJda() {
        return jda;
    }

    @Produces
    public EmbedCache getEmbedCache() {
        return embedCache;
    }

    @Produces
    public Levelbot getLevelbot() {
        return this;
    }

    public JDACommands getJdaCommands() {
        return jdaCommands;
    }

    public TextChannel getBotChannel() {
        return botChannel;
    }

    public enum GuildType {

        TESTING(496614159254028289L),
        PRODUCTION(367353132772098048L);

        public final long id;

        GuildType(long id) {
            this.id = id;
        }

    }
}
