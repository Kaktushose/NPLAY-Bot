package de.kaktushose.levelbot.bot;

import com.github.kaktushose.jda.commands.annotations.Produces;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.api.JsonEmbedFactory;
import com.github.kaktushose.jda.commands.entities.JDACommands;
import com.github.kaktushose.jda.commands.entities.JDACommandsBuilder;
import com.github.kaktushose.jda.commands.util.CommandDocumentation;
import de.kaktushose.discord.reactionwaiter.ReactionListener;
import de.kaktushose.levelbot.database.model.GuildSettings;
import de.kaktushose.levelbot.database.model.Item;
import de.kaktushose.levelbot.database.model.Rank;
import de.kaktushose.levelbot.database.service.LevelService;
import de.kaktushose.levelbot.database.service.UserService;
import de.kaktushose.levelbot.listener.JoinLeaveListener;
import de.kaktushose.levelbot.listener.LevelListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Levelbot {

    private static final Logger log = LoggerFactory.getLogger(Levelbot.class);
    private final UserService userService;
    private final LevelService levelService;
    private final GuildSettings guildSettings;
    private final EmbedCache embedCache;
    private JDACommands jdaCommands;
    private JDA jda;
    private Guild guild;

    public Levelbot(GuildType guildType) {
        userService = new UserService();
        levelService = new LevelService(userService);
        guildSettings = levelService.getGuildSetting(guildType.id);
        embedCache = new EmbedCache(new File("commandEmbeds.json"));
    }

    public Levelbot start() throws LoginException, InterruptedException {
        log.info("Bot is running at version {}", guildSettings.getVersion());

        embedCache.loadEmbedsToCache();

        log.debug("Starting jda...");
        String token = guildSettings.getBotToken();
        jda = JDABuilder.create(
                token,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS
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
                new LevelListener(this)
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
        jdaCommands.getDefaultSettings().setPrefix(guildSettings.getBotPrefix()).getHelpLabels().add("hilfe");

        log.debug("Applying permissions...");
        userService.getByPermission(0).forEach(botUser ->
                jdaCommands.getDefaultSettings().getMutedUsers().add(botUser.getUserId())
        );
        userService.getByPermission(2).forEach(botUser ->
                jdaCommands.getDefaultSettings().getPermissionHolders("moderator").add(botUser.getUserId())
        );
        userService.getByPermission(3).forEach(botUser -> {
                    jdaCommands.getDefaultSettings().getPermissionHolders("moderator").add(botUser.getUserId());
                    jdaCommands.getDefaultSettings().getPermissionHolders("admin").add(botUser.getUserId());
                }
        );
        userService.getByPermission(4).forEach(botUser -> {
                    jdaCommands.getDefaultSettings().getPermissionHolders("moderator").add(botUser.getUserId());
                    jdaCommands.getDefaultSettings().getPermissionHolders("admin").add(botUser.getUserId());
                    jdaCommands.getDefaultSettings().getPermissionHolders("owner").add(botUser.getUserId());
                }
        );

        guild = jda.getGuildById(496614159254028289L);

        jda.getPresence().setStatus(OnlineStatus.ONLINE);
        jda.getPresence().setActivity(Activity.playing("development"));

        return this;
    }

    public Levelbot stop() {
        jda.shutdown();
        jdaCommands.shutdown();
        return this;
    }

    public Levelbot indexMembers() {
        List<Member> guildMembers = guild.getMembers();
        guildMembers.forEach(member -> userService.createIfAbsent(member.getIdLong()));

        List<Long> guildMemberIds = guildMembers.stream().map(ISnowflake::getIdLong).collect(Collectors.toList());
        userService.getAll().forEach(botUser -> {
            if (!guildMemberIds.contains(botUser.getUserId())) {
                userService.delete(botUser.getUserId());
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
        guild.addRoleToMember(userId, guild.getRoleById(item.getRoleId())).queue();
    }

    public void removeItemRole(long userId, int itemId) {
        Item item = levelService.getItem(itemId);
        guild.removeRoleFromMember(userId, guild.getRoleById(item.getRoleId())).queue();
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

    public enum GuildType {

        TESTING(496614159254028289L),
        PRODUCTION(367353132772098048L);

        public final long id;

        GuildType(long id) {
            this.id = id;
        }

    }
}
