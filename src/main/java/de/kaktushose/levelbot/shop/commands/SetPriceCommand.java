package de.kaktushose.levelbot.shop.commands;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.kaktushose.levelbot.database.services.LevelService;

@CommandController("setprice")
@Permission("moderator")
public class SetPriceCommand {

    @Inject
    private LevelService levelService;
    @Inject
    private EmbedCache embedCache;

    @Command(
            name = "Preis ändern",
            usage = "{prefix}setprice <itemId> <price>",
            desc = "Setzt den Preis eines Items auf den angegebenen Wert",
            category = "Moderation"
    )
    public void onSetPrice(CommandEvent event, int id, int price) {
        if (levelService.setItemPrice(id, price)) {
            event.reply(embedCache.getEmbed("itemPriceChanged"));
        } else {
            event.reply(embedCache.getEmbed("itemNotFound").injectValue("id", id));
        }
    }
}
