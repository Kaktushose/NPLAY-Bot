package de.kaktushose.levelbot.commands.moderation;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.kaktushose.levelbot.database.services.UserService;
import net.dv8tion.jda.api.entities.Member;

@CommandController("set")
@Permission("moderator")
public class SetCurrencyCommand {

    @Inject
    private UserService userService;
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
        userService.setCoins(member.getIdLong(), amount);
        event.reply(embedCache.getEmbed("currencySet")
                .injectValue("currency", "Münzen")
                .injectValue("user", member.getAsMention())
                .injectValue("value", amount)
        );
    }

    @Command(
            value = "xp",
            name = "XP setzen",
            usage = "{prefix}set xp <member> <amount>",
            desc = "Setzt die Anzahl der XP eines Benutzers auf den angegbenen Wert.",
            category = "Moderation"
    )
    public void onSetXp(CommandEvent event, Member member, Integer amount) {
        userService.setXp(member.getIdLong(), amount);
        event.reply(embedCache.getEmbed("currencySet")
                .injectValue("currency", "XP")
                .injectValue("user", member.getAsMention())
                .injectValue("value", amount)
        );
    }

    @Command(
            value = "diamonds",
            name = "Diamanten setzen",
            usage = "{prefix}set diamonds <member> <amount>",
            desc = "Setzt die Anzahl der Diamanten eines Benutzers auf den angegbenen Wert.",
            category = "Moderation"
    )
    public void onSetDiamonds(CommandEvent event, Member member, Integer amount) {
        userService.setDiamonds(member.getIdLong(), amount);
        event.reply(embedCache.getEmbed("currencySet")
                .injectValue("currency", "Diamanten")
                .injectValue("user", member.getAsMention())
                .injectValue("value", amount)
        );
    }
}
