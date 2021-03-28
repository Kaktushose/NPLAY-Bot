package de.kaktushose.levelbot.commands.moderation;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.entities.CommandEvent;
import de.kaktushose.levelbot.database.Database;
import de.kaktushose.levelbot.database.model.BotUser;
import net.dv8tion.jda.api.entities.Member;

import java.util.Optional;

@CommandController("set")
@Permission("moderator")
public class SetCurrencyCommand {

    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;

    @Command(
            value = "coins",
            name = "Münzen setzen",
            usage = "{prefix}set coins <member> <amount>",
            desc = "Setzt die Anzahl der Münzen eines Benutzers auf den angegbenen Wert.",
            category = "Moderation"
    )
    public void onSetCoins(CommandEvent event, Member member, Integer amount) {
        Optional<BotUser> optional = database.getUsers().findById(member.getIdLong());

        if (optional.isPresent()) {
            BotUser botUser = optional.get();
            botUser.setCoins(amount);
            database.getUsers().save(botUser);

            event.reply(embedCache.getEmbed("currencySet")
                    .injectValue("currency", "Münzen")
                    .injectValue("user", member.getAsMention())
                    .injectValue("value", amount)
                    .toEmbedBuilder()
            );
        } else {
            event.reply(embedCache.getEmbed("memberNotFound").toEmbedBuilder());
        }
    }

    @Command(
            value = "xp",
            name = "XP setzen",
            usage = "{prefix}set xp <member> <amount>",
            desc = "Setzt die Anzahl der XP eines Benutzers auf den angegbenen Wert.",
            category = "Moderation"
    )
    public void onSetXp(CommandEvent event, Member member, Integer amount) {
        Optional<BotUser> optional = database.getUsers().findById(member.getIdLong());

        if (optional.isPresent()) {
            BotUser botUser = optional.get();
            botUser.setXp(amount);
            database.getUsers().save(botUser);

            event.reply(embedCache.getEmbed("currencySet")
                    .injectValue("currency", "XP")
                    .injectValue("user", member.getAsMention())
                    .injectValue("value", amount)
                    .toEmbedBuilder()
            );
        } else {
            event.reply(embedCache.getEmbed("memberNotFound").toEmbedBuilder());
        }
    }

    @Command(
            value = "diamonds",
            name = "Diamanten setzen",
            usage = "{prefix}set diamonds <member> <amount>",
            desc = "Setzt die Anzahl der Diamanten eines Benutzers auf den angegbenen Wert.",
            category = "Moderation"
    )
    public void onSetDiamonds(CommandEvent event, Member member, Integer amount) {
        Optional<BotUser> optional = database.getUsers().findById(member.getIdLong());

        if (optional.isPresent()) {
            BotUser botUser = optional.get();
            botUser.setDiamonds(amount);
            database.getUsers().save(botUser);

            event.reply(embedCache.getEmbed("currencySet")
                    .injectValue("currency", "Diamanten")
                    .injectValue("user", member.getAsMention())
                    .injectValue("value", amount)
                    .toEmbedBuilder()
            );
        } else {
            event.reply(embedCache.getEmbed("memberNotFound").toEmbedBuilder());
        }
    }

}
