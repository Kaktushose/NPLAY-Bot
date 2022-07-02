package de.kaktushose.levelbot.account.commands;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Optional;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.kaktushose.levelbot.account.data.BotUser;

@CommandController(value = {"tauschen", "wechseln"}, category = "Levelsystem", ephemeral = true)
public class ChangeDiamondsCommand {

    @Inject
    private EmbedCache embedCache;

    @Command(
            name = "Diamanten tauschen",
            desc = "Tauscht Diamanten gegen Münzen ein. Ein Diamant ist 20 Münzen wert"
    )
    public void onChangeDiamonds(CommandEvent event, @Optional("1") @Param(value = "Die Anzahl an Diamanten", name = "Anzahl") long amount) {
//        BotUser botUser = userService.getUserById(event.getAuthor().getIdLong());
//        long diamonds = botUser.getDiamonds();
//        long coins = amount * 20;
//
//        if (diamonds == 0 || amount > diamonds) {
//            event.reply(embedCache.getEmbed("missingCurrency").injectValue("currency", "Diamanten"));
//            return;
//        }
//
//        event.reply(embedCache.getEmbed("confirmAction").injectValue("action", String.format("du %d Diamanten gegen %d Münzen tauschen möchtest?", amount, coins)));
//
//        confirmMessage -> {
//            confirmMessage.addReaction(EmoteType.THUMBSUP.unicode)
//                    .and(confirmMessage.addReaction(EmoteType.THUMBSDOWN.unicode))
//                    .queue();
//
//            ReactionWaiter reactionWaiter = new ReactionWaiter(
//                    confirmMessage,
//                    event.getMember(),
//                    EmoteType.THUMBSUP.unicode,
//                    EmoteType.THUMBSDOWN.unicode
//            );
//
//            reactionWaiter.onEvent(reactionEvent -> {
//                if (reactionEvent.getEmote().equals(EmoteType.THUMBSUP.unicode)) {
//                    userService.exchangeDiamonds(botUser.getUserId(), amount);
//                    confirmMessage.editMessageEmbeds(embedCache.getEmbed("diamondChangeSuccess")
//                            .injectValue("diamonds", amount)
//                            .injectValue("coins", coins)
//                            .toMessageEmbed()
//                    ).queue();
//                }
//                reactionWaiter.stopWaiting(true);
//            });
//        }
//        );
    }
}
