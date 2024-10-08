package com.github.kaktushose.nplaybot.events;

import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.jda.commands.data.EmbedDTO;
import com.github.kaktushose.nplaybot.Bot;
import com.github.kaktushose.nplaybot.Database;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public abstract class GenericBotEvent {

    private final Bot bot;
    private final Database database;
    private final EmbedCache embedCache;
    private final JDA jda;
    private final Guild guild;

    public GenericBotEvent(Bot bot) {
        this.bot = bot;
        this.database = bot.getDatabase();
        this.embedCache = bot.getEmbedCache();
        this.jda = bot.getJda();
        this.guild = bot.getGuild();
    }

    public Bot getBot() {
        return bot;
    }

    public EventDispatcher getEventDispatcher() {
        return bot.getEventDispatcher();
    }

    public Database getDatabase() {
        return database;
    }

    public EmbedCache getEmbedCache() {
        return embedCache;
    }

    public EmbedDTO getEmbed(String name) {
        return embedCache.getEmbed(name);
    }

    public JDA getJDA() {
        return jda;
    }

    public Guild getGuild() {
        return guild;
    }
}
