package de.kaktushose.levelbot.commands.moderation;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;

@CommandController({"delete", "purge", "clear"})
@Permission("moderator")
public class BulkDeleteCommand {

    @Inject
    private EmbedCache embedCache;

    @Command(
            name = "Nachrichten löschen",
            usage = "{prefix}delete <amount>",
            desc = "Löscht die angegebene Zahl von Nachrichten aus einem Channel",
            category = "Moderation"
    )
    public void onBulkDeleteMessages(CommandEvent event, int amount) {
        // discord api only allows amount between 2 and 100
        if (amount < 2 || amount > 100) {
            event.reply(embedCache.getEmbed("invalidValue").injectValue("min", 2).injectValue("max", 100));
            return;
        }

        // amount + 1 so we delete the command message as well
        // complete aka blocking to make sure that the success message is sent correctly
        List<Message> messageHistory = event.getChannel().getHistory().retrievePast(amount + 1).complete();
        messageHistory.forEach(message -> message.delete().complete());

        event.reply(embedCache.getEmbed("bulkDeleteSuccess")
                        .injectValue("amount", amount),
                message -> message.delete().queueAfter(10, TimeUnit.SECONDS,
                        null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE)
                )); // delete success message after 10 secs
    }
}
