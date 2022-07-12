package de.kaktushose.levelbot.bot.commands;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.kaktushose.levelbot.bot.data.SettingsService;

@CommandController(value = "botinfo", category = "Sonstiges", ephemeral = true)
public class BotInfoCommand {

    @Inject
    private SettingsService settingsService;
    @Inject
    private EmbedCache embedCache;

    @Command(name = "Bot Information", desc = "Zeigt allgemeine Informationen über den Bot an")
    public void onBotInfo(CommandEvent event) {
        long guildId = event.getGuild().getIdLong();
        event.reply(embedCache.getEmbed("botInfo")
                .injectValue("prefix", event.getCommandContext().getContextualPrefix())
                .injectValue("version", settingsService.getVersion(guildId))
        );
    }
}
