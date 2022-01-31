package de.kaktushose.levelbot.shop.commands;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;

@CommandController("kaufen")
public class ShopDeprecatedCommand {

    @Inject
    private EmbedCache embedCache;

    @Command
    public void onBuy(CommandEvent event) {
        event.reply(embedCache.getEmbed("shopCommandDeprecated"));
    }

}
