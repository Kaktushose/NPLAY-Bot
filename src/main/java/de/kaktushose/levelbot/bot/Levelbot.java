package de.kaktushose.levelbot.bot;

import com.github.kaktushose.jda.commands.JDACommands;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.embeds.EmbedDTO;
import com.github.kaktushose.jda.commands.embeds.error.JsonErrorMessageFactory;
import com.github.kaktushose.jda.commands.embeds.help.JsonHelpMessageFactory;
import de.kaktushose.discord.reactionwaiter.ReactionListener;
import de.kaktushose.levelbot.Bootstrapper;
import de.kaktushose.levelbot.commands.moderation.WelcomeEmbedsCommand;
import de.kaktushose.levelbot.database.model.BotUser;
import de.kaktushose.levelbot.database.model.CollectEvent;
import de.kaktushose.levelbot.database.model.Rank;
import de.kaktushose.levelbot.database.services.*;
import de.kaktushose.levelbot.listener.*;
import de.kaktushose.levelbot.shop.ShopListener;
import de.kaktushose.levelbot.shop.data.ShopService;
import de.kaktushose.levelbot.shop.data.items.Item;
import de.kaktushose.levelbot.util.Statistics;
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
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SuppressWarnings("ConstantConditions")
public class Levelbot {

    private static final Logger log = LoggerFactory.getLogger(Levelbot.class);
    private final UserService userService;
    private final ShopService shopService;
    private final SettingsService settingsService;
    private final LevelService levelService;
    private final EventService eventService;
    private final BoosterService boosterService;
    private final EmbedCache embedCache;
    private final TaskScheduler taskScheduler;
    private final Statistics statistics;
    private final ShutdownHttpServer httpServer;
    private final long guildId;
    private JDACommands jdaCommands;
    private JDA jda;
    private Guild guild;
    private TextChannel botChannel;
    private TextChannel logChannel;

    public Levelbot() {
        userService = null;
        shopService = null;
        settingsService = null;
        levelService = null;
        eventService = null;
        boosterService = null;
        embedCache = null;
        taskScheduler = null;
        statistics = null;
        httpServer = null;
        guildId = 0;
    }

    public Levelbot(GuildType guildType) {
        userService = new UserService();
        shopService = new ShopService(this);
        settingsService = new SettingsService();
        eventService = new EventService(settingsService, userService);
        boosterService = new BoosterService(this);
        levelService = new LevelService(this);
        guildId = guildType.id;
        embedCache = new EmbedCache(new File("commandEmbeds.json"));
        taskScheduler = new TaskScheduler();
        statistics = new Statistics(this, guildId);
        httpServer = new ShutdownHttpServer(this, 8080);
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
                CacheFlag.EMOTE,
                CacheFlag.CLIENT_STATUS
        ).enableCache(
                CacheFlag.ACTIVITY
        ).setChunkingFilter(
                ChunkingFilter.ALL
        ).setMemberCachePolicy(
                MemberCachePolicy.NONE
        ).setActivity(
                Activity.playing("starting...")
        ).setStatus(
                OnlineStatus.DO_NOT_DISTURB
        ).build().awaitReady();

        log.debug("Registering event listeners...");
        jda.addEventListener(
                new JoinLeaveListener(this),
                new LevelListener(this),
                new VoiceTextLink(jda.getTextChannelById(367353132772098052L)),
                new ShopListener(this),
                new DailyRewardListener(this),
                new BoosterListener(),
                new ContestEventListener(settingsService, eventService)
        );

        log.debug("Starting ReactionListener...");
        ReactionListener.setAutoRemove(true);
        ReactionListener.setAutoRemoveDelay(60, TimeUnit.SECONDS);
        ReactionListener.startListening(jda);

        log.debug("Starting jda-commands...");
        jdaCommands = JDACommands.start(jda, Bootstrapper.class, "de.kaktushose.levelbot");
        EmbedCache embeds = new EmbedCache("jdacEmbeds.json");
        embeds.loadEmbedsToCache();
        jdaCommands.getImplementationRegistry().setHelpMessageFactory(
                new JsonHelpMessageFactory(embeds)
        );
        jdaCommands.getImplementationRegistry().setErrorMessageFactory(
                new JsonErrorMessageFactory(embeds)
        );
        guild = jda.getGuildById(guildId);
        botChannel = guild.getTextChannelById(settingsService.getBotChannelId(guildId));
        logChannel = guild.getTextChannelById(settingsService.getLogChannelId(guildId));

        // first start of bot, check for expired items immediately
        shopService.checkForExpiredItems();

        // get offset time until it's 0 am, also ensures that this task only runs once every 24 hours
        long current = TimeUnit.HOURS.toMinutes(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) + Calendar.getInstance().get(Calendar.MINUTE);
        long delay = TimeUnit.HOURS.toMinutes(24) - current;
        taskScheduler.addRepetitiveTask(() -> {
            log.info("Starting daily tasks!");
            try {
                log.info("Sending dm rank infos...");
                dmRankInfo();
                log.info("Checking for expired items...");
                shopService.checkForExpiredItems();
                log.info("Checking for new nitro boosters...");
                boosterService.updateBoosterStatus(guild, botChannel, embedCache);
                log.info("Checking for booster rewards...");
                checkForNitroBoostersRewards();
                log.info("Update user statistics...");
                updateUserStatistics();
                log.info("Done!");
            } catch (Throwable t) {
                log.error("An exception has occurred while executing daily tasks!", t);
            }
        }, delay, TimeUnit.HOURS.toMinutes(24), TimeUnit.MINUTES);

        taskScheduler.addRepetitiveTask(() -> {
            try {
                updateStatistics();
            } catch (Throwable t) {
                log.error("An exception has occurred while updating statistics!", t);
            }
        }, 0, 4, TimeUnit.HOURS);

        String version = settingsService.getVersion(guildId);
        jda.getPresence().setStatus(OnlineStatus.ONLINE);
        jda.getPresence().setActivity(Activity.playing(version));

        getBotChannel().sendMessageEmbeds(embedCache.getEmbed("botStart")
                .injectValue("version", version)
                .toMessageEmbed()
        ).queue();

        httpServer.start();

        return this;
    }

    public Levelbot stop() {
        getBotChannel().sendMessageEmbeds(embedCache.getEmbed("botStop").toMessageEmbed()).complete();
        jdaCommands.shutdown();
        jda.shutdown();
        return this;
    }

    public void terminate(int status) {
        System.exit(status);
    }

    public Levelbot indexMembers() {
        log.info("Indexing members...");
        guild.loadMembers().onSuccess(guildMembers -> {
            guildMembers.forEach(member -> userService.createUserIfAbsent(member.getIdLong()));

            List<Long> guildMemberIds = guildMembers.stream().map(ISnowflake::getIdLong).collect(Collectors.toList());
            userService.getAllUsers().forEach(botUser -> {
                if (!guildMemberIds.contains(botUser.getUserId())) {
                    log.info("Removing " + botUser.getUserId());
                    userService.deleteUser(botUser.getUserId());
                }
            });
        });
        return this;
    }

    public void addRankRole(long userId, int rankId) {
        log.info("addRankRole({}, {})", userId, rankId);
        Rank rank = levelService.getRank(rankId);
        Member member = guild.getMemberById(userId);
        if (member == null) {
            return;
        }
        guild.addRoleToMember(member, guild.getRoleById(rank.getRoleId())).queue();
        log.info("Added role {} to member {}", rank.getRoleId(), member);
    }

    public void removeRankRole(long userId, int rankId) {
        log.info("removeRankRole({}, {})", userId, rankId);
        Rank rank = levelService.getRank(rankId);
        guild.removeRoleFromMember(userId, guild.getRoleById(rank.getRoleId())).queue();
        log.info("Removed role {} from member {}", rank.getRoleId(), userId);

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
        if (item.getRoleId() == -1) {
            return;
        }
        guild.removeRoleFromMember(userId, guild.getRoleById(item.getRoleId())).queue();
    }

    public void addCollectEventRole(long userId) {
        CollectEvent event = eventService.getActiveCollectEvent(guildId);
        guild.addRoleToMember(userId, guild.getRoleById(event.getRoleId())).queue();
    }

    public void updateUserStatistics() {
        userService.getAllUserIds().forEach(userService::updateUserStatistics);
    }

    public void dmRankInfo() {
        userService.getUsersByDailyEnabled().forEach(botUser -> {
            User user = jda.getUserById(botUser.getUserId());
            user.openPrivateChannel().flatMap(privateChannel ->
                    privateChannel.sendMessageEmbeds(generateRankInfo(user, true).build())
            ).queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
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
        user.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessageEmbeds(embed.build()))
                .queue(null, new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER, exception -> {
                    TextChannel channel = getBotChannel();
                    channel.sendMessage(user.getAsMention()).and(channel.sendMessageEmbeds(embed.build())).queue();
                }));
    }

    public EmbedBuilder generateRankInfo(User target, boolean dm) {
        BotUser botUser = userService.getUserById(target.getIdLong());

        Rank currentRank = levelService.getCurrentRank(botUser.getUserId());
        Rank nextRank = levelService.getNextRank(botUser.getUserId());
        long nextRankXp = nextRank.getBound() - botUser.getXp();
        long xpGain = botUser.getXp() - botUser.getStartXp();
        long coinsGain = botUser.getCoins() - botUser.getStartCoins();
        long diamondsGain = botUser.getDiamonds() - botUser.getStartDiamonds();

        // if event is active, load another template
        String embed = eventService.isCollectEventActive(guildId) ? "eventRankInfo" : "rankInfo";

        String currentRankInfo = dm ? currentRank.getName() : String.format("<@&%d>", currentRank.getRoleId());
        String nextRankInfo = dm ? String.format("%s (noch %d XP)", nextRank.getName(), nextRankXp) : String.format("<@&%d> (noch %d XP)", nextRank.getRoleId(), nextRankXp);
        nextRankInfo = currentRank.equals(nextRank) ? "N/A" : nextRankInfo;
        String url = target.getAvatarUrl() == null ? "https://cdn.discordapp.com/embed/avatars/0.png" : target.getAvatarUrl();

        EmbedDTO embedDTO = embedCache.getEmbed(embed)
                .injectValue("user", target.getAsMention())
                .injectValue("color", currentRank.getColor())
                .injectValue("currentRank", currentRankInfo)
                .injectValue("nextRank", nextRankInfo)
                .injectValue("avatarUrl", url)
                .injectValue("xpGain", xpGain)
                .injectValue("coinsGain", coinsGain)
                .injectValue("diamondsGain", diamondsGain)
                .injectFields(botUser);


        if (eventService.isCollectEventActive(guildId)) {
            CollectEvent collectEvent = eventService.getActiveCollectEvent(guildId);
            Role collectEventRole = guild.getRoleById(collectEvent.getRoleId());
            long eventPoints = botUser.getEventPoints();
            embedDTO.injectValue("eventName", collectEvent.getName())
                    .injectValue("currencyName", collectEvent.getCurrencyName())
                    .injectValue("currencyEmote", collectEvent.getCurrencyEmote())
                    .injectValue("currencyPoints", eventPoints);
            if (eventPoints >= collectEvent.getItemBound()) {
                embedDTO.injectValue("eventRewards", collectEvent.getItem().getName() + "\n:blue_circle: Eventrolle " + collectEventRole.getName());
            } else if (eventPoints >= collectEvent.getRoleBound()) {
                embedDTO.injectValue("eventRewards", ":blue_circle: Eventrolle " + collectEventRole.getName());
            } else {
                embedDTO.injectValue("eventRewards", ":x: noch keine");
            }
        }

        EmbedBuilder embedBuilder = embedDTO.toEmbedBuilder();

        if (botUser.getTransactions().isEmpty()) {
            embedBuilder.addField("Keine Items in Besitz", "", false);
        }

        botUser.getTransactions().forEach(transaction -> {
            Item item = transaction.getItem();
            embedBuilder.addField(item.getName(), "noch " + item.getRemainingTimeAsDate(transaction.getBuyTime()), false);
        });

        return embedBuilder;
    }

    public void updateStatistics() {
        statistics.queryStatistics().onSuccess(stats -> {
            TextChannel channel = guild.getTextChannelById(WelcomeEmbedsCommand.WELCOME_CHANNEL_ID);
            channel.retrieveMessageById(settingsService.getStatisticsMessageId(guildId)).flatMap(message ->
                    message.editMessageEmbeds(embedCache.getEmbed("statistics")
                            .injectFields(statistics)
                            .toEmbedBuilder()
                            .setTimestamp(Instant.now())
                            .build())
            ).queue();
        });
    }

    public UserService getUserService() {
        return userService;
    }

    public ShopService getShopService() {
        return shopService;
    }

    public LevelService getLevelService() {
        return levelService;
    }

    public SettingsService getSettingsService() {
        return settingsService;
    }

    public BoosterService getBoosterService() {
        return boosterService;
    }

    public EventService getEventService() {
        return eventService;
    }

    public JDA getJda() {
        return jda;
    }

    public EmbedCache getEmbedCache() {
        return embedCache;
    }

    public Levelbot getLevelbot() {
        return this;
    }

    public Guild getGuild() {
        return guild;
    }

    public JDACommands getJdaCommands() {
        return jdaCommands;
    }

    public TextChannel getBotChannel() {
        return botChannel;
    }

    public TextChannel getLogChannel() {
        return logChannel;
    }

    public TaskScheduler getTaskScheduler() {
        return taskScheduler;
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
