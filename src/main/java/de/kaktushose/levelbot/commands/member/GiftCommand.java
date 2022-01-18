package de.kaktushose.levelbot.commands.member;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.entities.CommandEvent;
import de.kaktushose.levelbot.bot.Levelbot;
import de.kaktushose.levelbot.database.services.SettingsService;
import de.kaktushose.levelbot.shop.data.items.ItemCategory;
import de.kaktushose.levelbot.shop.data.items.ItemVariant;
import de.kaktushose.levelbot.shop.data.ShopService;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

@CommandController(value = "geschenk", isActive = false)
public class GiftCommand {

    @Inject
    private SettingsService settingsService;
    @Inject
    private ShopService shopService;
    @Inject
    private Levelbot levelbot;

    @Command(
            name = "Geschenke",
            usage = "{prefix}geschenk",
            desc = "Fröhliche Weihnachten!",
            category = "Levelsystem"
    )
    public void onCommand(CommandEvent event) {
        if (settingsService.getRewardedUsers().contains(event.getAuthor().getIdLong())) {
            event.reply("Du hast dein Geschenk bereits erhalten!");
            return;
        }
        shopService.addItem(event.getAuthor().getIdLong(), ItemCategory.PREMIUM, ItemVariant.LIGHT);
        settingsService.addRewardedUser(event.getAuthor().getIdLong());
        event.reply(new EmbedBuilder()
                .setTitle("Ein Geschenk für Dich, " + event.getMember().getEffectiveName() + ":christmas_tree::santa::snowflake:")
                .setDescription("Das ganze Serverteam wünscht Dir **frohe Festtage** und einen **guten Rutsch.**\n\n" +
                        "Wir bedanken uns für Deine Treue und schenken Dir das Item:\n**:gift: PREMIUM light :star:!**\n\n" +
                        "Freue dich über **15 Tage kostenfreies PREMIUM** auf dem Server mit **vielen Vorteilen!**\n\n" +
                        ":point_right: Übrigens: Schau gleich mal im Kanal <#539517056543096843> vorbei - dort läuft gerade ein **weiteres Gewinnspiel** :tada: exklusiv für PREMIUM-User.")
                .setColor(Color.ORANGE)
        );
    }

}
