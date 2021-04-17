package de.kaktushose.levelbot.commands.member;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.entities.CommandEvent;
import de.kaktushose.levelbot.database.model.GuildSettings;
import de.kaktushose.levelbot.database.service.LevelService;

@CommandController({"botinfo", "credits"})
public class BotInfoCommand {

    @Inject
    private LevelService levelService;
    @Inject
    private EmbedCache embedCache;

    @Command(
            name = "Bot Information",
            usage = "{prefix}botinfo",
            desc = "Zeigt allgemeine Inforamtionen über den Bot an",
            category = "Sonstiges"
    )
    public void onBotInfo(CommandEvent event) {
        GuildSettings guildSettings = levelService.getGuildSettings(event.getGuild().getIdLong());
        event.reply(embedCache.getEmbed("botInfo")
                .injectValue("prefix", guildSettings.getBotPrefix())
                .injectValue("version", guildSettings.getVersion())
        );
    }
}