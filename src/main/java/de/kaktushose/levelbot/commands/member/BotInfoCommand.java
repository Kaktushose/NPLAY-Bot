package de.kaktushose.levelbot.commands.member;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.entities.CommandEvent;
import de.kaktushose.levelbot.database.model.Config;

@CommandController({"botinfo", "credits"})
public class BotInfoCommand {

    @Inject
    private Config config;
    @Inject
    private EmbedCache embedCache;

    @Command(
            name = "Bot Information",
            usage = "{prefix}botinfo",
            desc = "Zeigt allgemeine Inforamtionen über den Bot an",
            category = "Sonstiges"
    )
    public void onBotInfo(CommandEvent event) {
        event.reply(embedCache.getEmbed("botInfo")
                .injectValue("prefix", config.getBotPrefix())
                .injectValue("version", config.getVersion())
        );
    }
}