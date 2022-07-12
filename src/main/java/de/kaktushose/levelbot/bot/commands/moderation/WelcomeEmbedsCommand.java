package de.kaktushose.levelbot.bot.commands.moderation;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.kaktushose.levelbot.bot.data.SettingsService;

import java.io.File;

@CommandController(value = "send embeds", category = "Moderation", ephemeral = true)
@Permission("moderator")
public class WelcomeEmbedsCommand {

    private static final String STATISTICS_EMBED_TITLE = "Fehler!";
    private final EmbedCache welcomeEmbedCache;
    @Inject
    private SettingsService settingsService;
    @Inject
    private EmbedCache embedCache;

    public WelcomeEmbedsCommand() {
        welcomeEmbedCache = new EmbedCache(new File("welcomeEmbeds.json"));
    }

    @Command(name = "Willkommen Embeds senden", desc = "Sendet die Embeds in <#551483788337872927>")
    public void sendEmbeds(CommandEvent event) {
        event.reply(embedCache.getEmbed("welcomeEmbedsSuccess"));
        welcomeEmbedCache.values().forEach(embedDTO -> {
            if (embedDTO.getTitle().equals(STATISTICS_EMBED_TITLE)) {
                event.getChannel().sendMessageEmbeds(embedDTO.toMessageEmbed()).queue(message ->
                        settingsService.updateStatisticsMessage(event.getGuild(), message)
                );
                return;
            }
            event.getChannel().sendMessageEmbeds(embedDTO.toMessageEmbed()).queue();
        });
    }
}
