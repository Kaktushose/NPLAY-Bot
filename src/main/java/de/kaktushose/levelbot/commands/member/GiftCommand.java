package de.kaktushose.levelbot.commands.member;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.entities.CommandEvent;
import de.kaktushose.levelbot.database.services.SettingsService;
import de.kaktushose.levelbot.database.services.UserService;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

@CommandController("geschenk")
public class GiftCommand {

    @Inject
    private SettingsService settingsService;
    @Inject
    private UserService userService;

    @Command(
            name = "Geschenke",
            usage = "{prefix}geschenk",
            desc = "Fröhliches Halloween!",
            category = "Levelsystem"
    )
    public void onCommand(CommandEvent event) {
        if (settingsService.getRewardedUsers().contains(event.getAuthor().getIdLong())) {
            event.reply("Du hast dein Geschenk bereits erhalten!");
            return;
        }
        userService.addCoins(event.getAuthor().getIdLong(), 66);
        settingsService.addRewardedUser(event.getAuthor().getIdLong());
        event.reply(new EmbedBuilder()
                .setTitle("Mut zahlt sich aus, " + event.getMember().getEffectiveName())
                .setDescription("Bei einem nächtlichen Spaziergang über den Geisterfriedhof :ghost hast du **66 Münzen** :moneybag: gefunden. Zwar sind sie etwas vermodert, doch als Zahlungsmittel werden sie wohl noch angenommen. Probiere es doch gleich mal aus!")
                .setColor(Color.ORANGE)
        );
    }

}
