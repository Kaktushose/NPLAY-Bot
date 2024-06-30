package com.github.kaktushose.nplaybot;

import com.github.kaktushose.jda.commands.JDACommands;
import com.github.kaktushose.jda.commands.annotations.Produces;
import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.nplaybot.events.collect.CollectEventListener;
import com.github.kaktushose.nplaybot.events.contest.ContestListener;
import com.github.kaktushose.nplaybot.karma.KarmaListener;
import com.github.kaktushose.nplaybot.permissions.CustomPermissionsProvider;
import com.github.kaktushose.nplaybot.rank.JoinLeaveListener;
import com.github.kaktushose.nplaybot.rank.RankListener;
import com.github.kaktushose.nplaybot.scheduler.TaskScheduler;
import com.github.kaktushose.nplaybot.starboard.StarboardListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.util.concurrent.TimeUnit;

public class Bot {

    private final JDA jda;
    private final JDACommands jdaCommands;
    private final Database database;
    private final EmbedCache embedCache;
    private final TaskScheduler taskScheduler;
    private final Guild guild;

    @SuppressWarnings("DataFlowIssue")
    private Bot(long guildId) throws InterruptedException, RuntimeException {
        try {
            database = new Database();
        } catch (Exception e) {
            throw new RuntimeException("Unable to connect to database!", e);
        }

        embedCache = new EmbedCache("embeds.json");

        jda = JDABuilder.createDefault(database.getSettingsService().getBotToken(guildId))
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setActivity(Activity.customStatus("starting..."))
                .setStatus(OnlineStatus.IDLE)
                .addEventListeners(
                        new RankListener(database, embedCache),
                        new JoinLeaveListener(database.getRankService()),
                        new ContestListener(database.getContestEventService()),
                        new CollectEventListener(database, embedCache),
                        new KarmaListener(database, embedCache),
                        new StarboardListener(database, embedCache)
                )
                .build().awaitReady();

        database.getRankService().indexMembers(jda.getGuildById(guildId));

        jdaCommands = JDACommands.start(jda, Bot.class, "com.github.kaktushose.nplaybot");
        jdaCommands.getDependencyInjector().registerProvider(this);
        jdaCommands.getImplementationRegistry().setPermissionsProvider(new CustomPermissionsProvider(database));

        taskScheduler = new TaskScheduler(this);

        guild = jda.getGuildById(guildId);

        jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.customStatus("Version 3.0.0"));
    }

    public static Bot start(long guildId) throws InterruptedException {
        return new Bot(guildId);
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
