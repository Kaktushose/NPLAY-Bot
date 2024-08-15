package com.github.kaktushose.nplaybot;

import com.github.kaktushose.jda.commands.JDACommands;
import com.github.kaktushose.jda.commands.annotations.Produces;
import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.jda.commands.embeds.JsonErrorMessageFactory;
import com.github.kaktushose.nplaybot.features.LegacyCommandListener;
import com.github.kaktushose.nplaybot.features.MemberDatabaseSyncListener;
import com.github.kaktushose.nplaybot.features.events.contest.ContestListener;
import com.github.kaktushose.nplaybot.features.karma.KarmaListener;
import com.github.kaktushose.nplaybot.permissions.CustomPermissionsProvider;
import com.github.kaktushose.nplaybot.features.rank.RankListener;
import com.github.kaktushose.nplaybot.scheduler.TaskScheduler;
import com.github.kaktushose.nplaybot.features.starboard.StarboardListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.concurrent.TimeUnit;

public class Bot {

    private final JDA jda;
    private final JDACommands jdaCommands;
    private final Database database;
    private final EmbedCache embedCache;
    private final TaskScheduler taskScheduler;
    private final Guild guild;

    private Bot(long guildId, String token) throws InterruptedException, RuntimeException {
        embedCache = new EmbedCache("embeds.json");

        jda = JDABuilder.createDefault(token)
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.include(guildId))
                .enableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS)
                .setActivity(Activity.customStatus("starting..."))
                .setStatus(OnlineStatus.IDLE)
                .build().awaitReady();

        guild = jda.getGuildById(guildId);

        try {
            database = new Database(this);
        } catch (Exception e) {
            throw new RuntimeException("Unable to connect to database!", e);
        }

        jda.addEventListener(
                new RankListener(database, embedCache, this),
                new ContestListener(database),
                new KarmaListener(database, embedCache),
                new StarboardListener(database, embedCache),
                new MemberDatabaseSyncListener(database),
                new LegacyCommandListener(embedCache)
        );

        jdaCommands = JDACommands.start(jda, Bot.class, "com.github.kaktushose.nplaybot");
        jdaCommands.getDependencyInjector().registerProvider(this);
        jdaCommands.getImplementationRegistry().setErrorMessageFactory(new JsonErrorMessageFactory(embedCache));
        jdaCommands.getImplementationRegistry().setPermissionsProvider(new CustomPermissionsProvider(database));

        jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.customStatus("Version 3.0.0"));

        taskScheduler = new TaskScheduler(this);
    }

    public static Bot start(long guildId, String token) throws InterruptedException {
        return new Bot(guildId, token);
    }

    public void shutdown() {
        taskScheduler.shutdown();
        jdaCommands.shutdown();
        jda.shutdown();
        try {
            jda.awaitShutdown(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        database.closeDataSource();
    }

    public JDA getJda() {
        return jda;
    }

    @Produces(skipIndexing = true)
    public Database getDatabase() {
        return database;
    }

    @Produces(skipIndexing = true)
    public EmbedCache getEmbedCache() {
        return embedCache;
    }

    public Guild getGuild() {
        return guild;
    }
}
