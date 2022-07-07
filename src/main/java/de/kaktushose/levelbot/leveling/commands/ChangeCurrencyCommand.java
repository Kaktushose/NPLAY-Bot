package de.kaktushose.levelbot.leveling.commands;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.kaktushose.levelbot.account.data.UserService;
import net.dv8tion.jda.api.entities.Member;

@CommandController(value = "add", category = "Moderation")
@Permission("moderator")
public class ChangeCurrencyCommand {

    @Inject
    private UserService userService;
    @Inject
    private EmbedCache embedCache;

    @Command(
            name = "Add",
            usage = "{prefix}add <coins|xp|diamonds> <member> <amount>",
            desc = "Ändert die Währungen eines Nutzers um den angegebenen Wert",
            isSuper = true
    )
    public void onChangeCurrency(CommandEvent event) {
        event.sendSpecificHelpMessage();
    }

    @Command(
            value = "coins",
            name = "Münzen ändern",
            usage = "{prefix}add coins <member> <amount>",
            desc = "Ändert die Anzahl der Münzen eines Benutzers um den angegebenen Wert.",
            category = "Moderation"
    )
    public void onAddCoins(CommandEvent event, Member member, Integer amount) {
        userService.addCurrencies(member.getIdLong(), amount, 0, 0);
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
            desc = "Ändert die Anzahl der XP eines Benutzers um den angegebenen Wert.",
            category = "Moderation"
    )
    public void onAddXp(CommandEvent event, Member member, Integer amount) {
        userService.addCurrencies(member.getIdLong(), 0, amount, 0);
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
            desc = "Ändert die Anzahl der Diamanten eines Benutzers um den angegebenen Wert.",
            category = "Moderation"
    )
    public void onAddDiamonds(CommandEvent event, Member member, Integer amount) {
        userService.addCurrencies(member.getIdLong(), 0, 0, amount);
        event.reply(embedCache.getEmbed("currencyChange")
                .injectValue("currency", "Diamanten")
                .injectValue("user", member.getAsMention())
                .injectValue("value", amount)
                .injectValue("operation", amount > 0 ? "erhöht" : "verringert")
        );
    }
}
