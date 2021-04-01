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

@CommandController("add")
@Permission("moderator")
public class ChangeCurrencyCommand {

    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;

    @Command(
            value = "coins",
            name = "Münzen ändern",
            usage = "{prefix}add coins <member> <amount>",
            desc = "Ändert die Anzahl der Münzen eines Benutzers um den angegbenen Wert.",
            category = "Moderation"
    )
    public void onChangeCoins(CommandEvent event, Member member, Integer amount) {
        Optional<BotUser> optional = database.getUsers().findById(member.getIdLong());
        if (optional.isEmpty()) {
            event.reply(embedCache.getEmbed("memberNotFound"));
            return;
        }

        BotUser botUser = optional.get();
        botUser.setCoins(botUser.getCoins() + amount);
        database.getUsers().save(botUser);

        event.reply(embedCache.getEmbed("currencyChange")
                .injectValue("currency", "Münzen")
                .injectValue("user", member.getAsMention())
                .injectValue("value", amount)
                .injectValue("operation", amount > 0 ? "erhöht" : "verringert")
        );
    }

    @Command(
            value = "xp",
            name = "XP ändern",
            usage = "{prefix}add xp <member> <amount>",
            desc = "Ändert die Anzahl der XP eines Benutzers um den angegbenen Wert.",
            category = "Moderation"
    )
    public void onChangeXp(CommandEvent event, Member member, Integer amount) {
        Optional<BotUser> optional = database.getUsers().findById(member.getIdLong());
        if (optional.isEmpty()) {
            event.reply(embedCache.getEmbed("memberNotFound"));
            return;
        }

        BotUser botUser = optional.get();
        botUser.setXp(botUser.getXp() + amount);
        database.getUsers().save(botUser);

        event.reply(embedCache.getEmbed("currencyChange")
                .injectValue("currency", "XP")
                .injectValue("user", member.getAsMention())
                .injectValue("value", amount)
                .injectValue("operation", amount > 0 ? "erhöht" : "verringert")
        );
    }

    @Command(
            value = "diamonds",
            name = "Diamanten ändern",
            usage = "{prefix}add diamonds <member> <amount>",
            desc = "Ändert die Anzahl der Diamanten eines Benutzers um den angegbenen Wert.",
            category = "Moderation"
    )
    public void onChangeDiamonds(CommandEvent event, Member member, Integer amount) {
        Optional<BotUser> optional = database.getUsers().findById(member.getIdLong());
        if (optional.isEmpty()) {
            event.reply(embedCache.getEmbed("memberNotFound"));
            return;
        }

        BotUser botUser = optional.get();
        botUser.setDiamonds(botUser.getDiamonds() + amount);
        database.getUsers().save(botUser);

        event.reply(embedCache.getEmbed("currencyChange")
                .injectValue("currency", "Diamanten")
                .injectValue("user", member.getAsMention())
                .injectValue("value", amount)
                .injectValue("operation", amount > 0 ? "erhöht" : "verringert")
        );
    }
}