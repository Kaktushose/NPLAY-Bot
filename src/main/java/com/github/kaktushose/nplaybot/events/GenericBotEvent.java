package com.github.kaktushose.nplaybot.events;

import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.Database;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public abstract class GenericBotEvent {

    private final Bot bot;
    private final Database database;
    private final EmbedCache embedCache;
    private final JDA jda;
    private final Guild guild;
    private final Member member;

    public GenericBotEvent(Bot bot, Member member) {
        this.bot = bot;
        this.database = bot.getDatabase();
        this.embedCache = bot.getEmbedCache();
        this.jda = bot.getJda();
        this.guild = bot.getGuild();
        this.member = member;
    }

    public Database getDatabase() {
        return database;
    }

    public EmbedCache getEmbedCache() {
        return embedCache;
    }

    public JDA getJDA() {
        return jda;
    }

    public Guild getGuild() {
        return guild;
    }

    public Member getMember() {
        return member;
    }
}
