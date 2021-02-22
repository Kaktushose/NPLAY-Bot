package de.kaktushose.levelbot.commands;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.entities.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

@CommandController
public class PingCommand {

    @Inject
    private EmbedCache embedCache;

    @Command(
            value = "ping",
            name = "Ping Command",
            usage = "{prefix}ping",
            desc = "Zeigt den Ping zur Discord-API an",
            category = "Sonstiges"
    )
    public void onPing(CommandEvent event) {
        long gatewayPing = event.getJDA().getGatewayPing();
        long restPing = event.getJDA().getRestPing().complete();
        event.reply(embedCache
                .getEmbed("pingEmbed")
                .injectValue("gatewayPing", gatewayPing)
                .injectValue("restPing", restPing)
                .toEmbedBuilder()
        );
    }
}
