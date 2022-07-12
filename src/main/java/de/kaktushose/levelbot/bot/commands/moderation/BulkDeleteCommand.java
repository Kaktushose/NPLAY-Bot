package de.kaktushose.levelbot.bot.commands.moderation;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.annotations.constraints.Max;
import com.github.kaktushose.jda.commands.annotations.constraints.Min;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.concurrent.TimeUnit;

@CommandController(value = "delete", category = "Moderation", ephemeral = true)
@Permission("moderator")
public class BulkDeleteCommand {

    @Inject
    private EmbedCache embedCache;

    @Command(name = "Nachrichten löschen", desc = "Löscht die angegebene Zahl von Nachrichten aus einem Channel")
    public void onBulkDeleteMessages(
            CommandEvent event,
            @Min(value = 1, message = "Muss mindestens eine Nachricht löschen")
            @Max(value = 100, message = "Kann maximal 100 Nachrichten gleichzeitig löschen")
            @Param("Die Anzahl zu löschender Nachrichten") int amount
    ) {
        event.getChannel().getHistory().retrievePast(amount).queue(history -> history.forEach(message -> message.delete().queue()));

        event.reply(embedCache.getEmbed("bulkDeleteSuccess").injectValue("amount", amount));
    }
}
