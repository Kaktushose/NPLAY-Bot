package de.kaktushose.levelbot.commands.owner;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.entities.CommandEvent;
import de.kaktushose.discord.reactionwaiter.EmoteType;
import de.kaktushose.discord.reactionwaiter.ReactionWaiter;
import de.kaktushose.levelbot.bot.Levelbot;

@CommandController(value = {"stop", "shutdown"})
@Permission("owner")
public class StopCommand {

    @Inject
    private Levelbot levelbot;
    @Inject
    private EmbedCache embedCache;

    @Command(
            name = "Bot herunterfahren",
            usage = "{prefix}stop",
            desc = "Fährt den Bot herunter.",
            category = "Owner"
    )
    public void onStop(CommandEvent event) {
        event.reply(embedCache.getEmbed("confirmAction")
                        .injectValue("action", "du den Bot herunterfahren möchtest?\n"
                                + "Nur <@393843637437464588> kann den Bot wieder starten!"),
                confirmMessage -> {
                    confirmMessage.addReaction(EmoteType.THUMBSUP.unicode)
                            .and(confirmMessage.addReaction(EmoteType.THUMBSDOWN.unicode))
                            .queue();

                    ReactionWaiter reactionWaiter = new ReactionWaiter(
                            confirmMessage,
                            event.getMember(),
                            EmoteType.THUMBSUP.unicode,
                            EmoteType.THUMBSDOWN.unicode
                    );

                    reactionWaiter.onEvent(reactionEvent -> {
                        if (reactionEvent.getEmote().equals(EmoteType.THUMBSUP.unicode)) {
                            event.reply("https://tenor.com/view/tekashi-69-fade-out-peace-gif-15141419");
                            confirmMessage.delete().queue();
                            levelbot.stop();
                            System.exit(0);
                        }
                        reactionWaiter.stopWaiting(true);
                    });
                });
    }

}
