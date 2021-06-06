package de.kaktushose.levelbot.commands.member;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Optional;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.entities.CommandEvent;
import de.kaktushose.discord.reactionwaiter.EmoteType;
import de.kaktushose.discord.reactionwaiter.ReactionWaiter;
import de.kaktushose.levelbot.database.model.BotUser;
import de.kaktushose.levelbot.database.services.UserService;

@CommandController({"tauschen", "wechseln"})
public class ChangeDiamondsCommand {

    @Inject
    private UserService userService;
    @Inject
    private EmbedCache embedCache;

    @Command(
            name = "Diamanten tauschen",
            usage = "{prefix}tauschen <anzahl>",
            desc = "Tauscht Diamanten gegen Münzen ein. Ein Diamant ist 20 Münzen wert",
            category = "Levelsystem"
    )
    public void onChangeDiamonds(CommandEvent event, @Optional("1") long amount) {
        BotUser botUser = userService.getUserById(event.getAuthor().getIdLong());
        long diamonds = botUser.getDiamonds();
        long coins = amount * 20;
        if (diamonds == 0 || amount > diamonds) {
            event.reply(embedCache.getEmbed("missingCurrency").injectValue("currency", "Diamanten"));
            return;
        }

        event.reply(
                embedCache.getEmbed("confirmAction").injectValue(
                        "action",
                        String.format("du %d Diamanten gegen %d Münzen tauschen möchtest?", amount, coins)
                ),
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
                            userService.exchangeDiamonds(botUser.getUserId(), amount);
                            event.reply(embedCache.getEmbed("diamondChangeSuccess")
                                    .injectValue("diamonds", amount)
                                    .injectValue("coins", coins)
                            );
                        }
                        reactionWaiter.stopWaiting(true);
                    });
                }
        );
    }
}
