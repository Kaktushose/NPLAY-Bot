package de.kaktushose.levelbot.shop.commands;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.entities.CommandEvent;

@CommandController("kaufen")
public class ShopDeprecatedCommand {

    @Inject
    private EmbedCache embedCache;

    @Command
    public void onBuy(CommandEvent event) {
        event.reply(embedCache.getEmbed("shopCommandDeprecated"));
    }

}
