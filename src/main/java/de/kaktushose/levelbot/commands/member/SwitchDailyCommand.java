package de.kaktushose.levelbot.commands.member;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.entities.CommandEvent;
import de.kaktushose.levelbot.database.Database;
import de.kaktushose.levelbot.database.model.BotUser;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

@CommandController("täglich")
public class SwitchDailyCommand {

    @Inject
    private EmbedCache embedCache;
    @Inject
    private Database database;

    @Command(
            name = "Täglich Command",
            usage = "{prefix}täglich",
            desc = "Aktiviert bzw. deaktiviert die täglichen Kontoinformationen",
            category = "Levelsystem"
    )
    public void onSwitchDaily(CommandEvent event) {
        BotUser botUser = database.getUsers().findById(event.getAuthor().getIdLong()).orElseThrow();

        if (botUser.isDailyUpdate()) {
            botUser.setDailyUpdate(false);
            database.getUsers().save(botUser);
            event.reply(embedCache.getEmbed("switchDailySuccess").injectValue("action", "deaktiviert"));
        } else {
            botUser.setDailyUpdate(true);
            database.getUsers().save(botUser);

            event.getAuthor().openPrivateChannel()
                    .flatMap(privateChannel ->
                            privateChannel.sendMessage(embedCache.getEmbed("switchDailySuccess")
                                    .injectValue("action", "aktiviert")
                                    .toMessageEmbed()
                            )
                    )
                    .queue(success -> event.getMessage().delete().queue(),
                            new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER, e -> {
                                botUser.setDailyUpdate(false);
                                database.getUsers().save(botUser);
                                event.reply(embedCache.getEmbed("switchDailyError"));
                            })
                    );
        }
    }
}
