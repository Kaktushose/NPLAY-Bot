package de.kaktushose.levelbot.commands.moderation;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;

import java.io.File;

@CommandController("embeds")
@Permission("moderator")
public class WelcomeEmbedsCommand {

    public static final long WELCOME_CHANNEL_ID = 851434963316375602L;
    private final EmbedCache welcomeEmbedCache;
    @Inject
    private EmbedCache embedCache;

    public WelcomeEmbedsCommand() {
        welcomeEmbedCache = new EmbedCache(new File("welcomeEmbeds.json"));
    }

    @Command(
            value = "send",
            name = "Willkommen Embeds senden",
            usage = "{prefix}embeds send",
            desc = "Sendet die Embeds in <#551483788337872927>",
            category = "Moderation"
    )
    public void sendEmbeds(CommandEvent event) {
        welcomeEmbedCache.loadEmbedsToCache();
        welcomeEmbedCache.values().forEach(embedDTO -> {
            event.getGuild().getTextChannelById(WELCOME_CHANNEL_ID).sendMessageEmbeds(embedDTO.toMessageEmbed()).queue();
        });
    }
}

