package de.kaktushose.levelbot.commands.moderation;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.entities.CommandEvent;
import de.kaktushose.discord.reactionwaiter.EmoteType;
import de.kaktushose.discord.reactionwaiter.ReactionWaiter;
import de.kaktushose.levelbot.database.Database;
import de.kaktushose.levelbot.database.model.BotUser;
import de.kaktushose.levelbot.database.model.Item;
import de.kaktushose.levelbot.database.model.Transaction;
import de.kaktushose.levelbot.util.NumberEmojis;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@CommandController({"remove", "rm"})
@Permission("moderator")
public class RemoveItemCommand {

    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;

    @Command(
            name = "Item entfernen",
            usage = "{prefix}remove <member>",
            desc = "Entfernt ein Item aus dem Besitz eines Benutzers",
            category = "Moderation"
    )
    public void onRemoveItem(CommandEvent event, Member member) {
        Optional<BotUser> optional = database.getUsers().findById(member.getIdLong());
        if (optional.isEmpty()) {
            event.reply(embedCache.getEmbed("memberNotFound"));
            return;
        }

        BotUser target = optional.get();
        List<Transaction> transactions = target.getTransactions();
        AtomicInteger counter = new AtomicInteger(1);
        List<String> emotes = new ArrayList<>();
        EmbedBuilder builder = embedCache.getEmbed("removeItem")
                .injectValue("user", member.getAsMention())
                .toEmbedBuilder();

        transactions.forEach(transaction -> {
            Item item = transaction.getItem();
            String emote = EmoteType.getNumber(counter.getAndIncrement()).unicode;
            emotes.add(emote);
            builder.addField(emote + ": " + item.getName(), "", false);
        });

        event.reply(builder, chooseMessage -> {
            emotes.forEach(emote -> chooseMessage.addReaction(emote).queue());

            ReactionWaiter reactionWaiter = new ReactionWaiter(chooseMessage, event.getMember(), emotes);
            reactionWaiter.onEvent(reactionEvent -> {

                switch (reactionEvent.getEmote()) {
                    case NumberEmojis.ONE:
                        transactions.remove(0);
                        break;
                    case NumberEmojis.TWO:
                        transactions.remove(1);
                        break;
                    case NumberEmojis.THREE:
                        transactions.remove(2);
                        break;
                    case NumberEmojis.FOUR:
                        transactions.remove(3);
                        break;
                    case NumberEmojis.FIVE:
                        transactions.remove(4);
                        break;
                    default:
                        return;
                }
                target.setTransactions(transactions);
                database.getUsers().save(target);
                reactionWaiter.stopWaiting(true);
                chooseMessage.delete().queue();
                event.reply(embedCache.getEmbed("removeItemSuccess").injectValue("user", member.getAsMention()));
            });
        });


    }
}
