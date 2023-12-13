package com.github.kaktushose.nplaybot;

import com.github.kaktushose.jda.commands.JDACommands;
import com.github.kaktushose.jda.commands.annotations.Produces;
import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.nplaybot.rank.JoinLeaveListener;
import com.github.kaktushose.nplaybot.rank.RankListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Bot {

    private final JDA jda;
    private final JDACommands jdaCommands;
    private final Database database;
    private final EmbedCache embedCache;

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
                .setActivity(Activity.customStatus("starting..."))
                .setStatus(OnlineStatus.IDLE)
                .addEventListeners(
                        new RankListener(database.getRankService(), embedCache),
                        new JoinLeaveListener(database.getRankService())
                )
                .build().awaitReady();

        database.getRankService().indexMembers(jda.getGuildById(guildId));

        jdaCommands = JDACommands.start(jda, Bot.class, "com.github.kaktushose.nplaybot");
        jdaCommands.getDependencyInjector().registerProvider(this);

        jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.customStatus("Version 3.0.0"));
    }

    public static Bot start(long guildId) throws InterruptedException {
        return new Bot(guildId);
    }

    public void shutdown() {
        jdaCommands.shutdown();
        jda.shutdown();
        database.closeDataSource();
    }

    @Produces(skipIndexing = true)
    public Database getDatabase() {
        return database;
    }

    @Produces(skipIndexing = true)
    public EmbedCache getEmbedCache() {
        return embedCache;
    }
}
