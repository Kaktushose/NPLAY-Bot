package de.kaktushose.levelbot.account.commands;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.kaktushose.levelbot.account.data.UserService;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

@CommandController(value = {"täglich", "daily"}, category = "Levelsystem", ephemeral = true)
public class SwitchDailyCommand {

    @Inject
    private EmbedCache embedCache;
    @Inject
    private UserService userService;

    @Command(
            name = "Täglich Command",
            desc = "Aktiviert bzw. deaktiviert die täglichen Kontoinformationen"
    )
    public void onSwitchDaily(CommandEvent event) {
        if (!userService.switchDaily(event.getAuthor())) {
            event.reply(embedCache.getEmbed("switchDailySuccess").injectValue("action", "deaktiviert"));
        } else {
            event.getAuthor().openPrivateChannel()
                    .flatMap(privateChannel ->
                            privateChannel.sendMessageEmbeds(embedCache.getEmbed("switchDailySuccess")
                                    .injectValue("action", "aktiviert")
                                    .toMessageEmbed()
                            )
                    )
                    .queue(success -> event.reply(embedCache.getEmbed("switchDailySuccess").injectValue("action", "aktiviert")),
                            new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER, e -> {
                                userService.switchDaily(event.getAuthor());
                                event.reply(embedCache.getEmbed("switchDailyError"));
                            })
                    );
        }
    }
}
