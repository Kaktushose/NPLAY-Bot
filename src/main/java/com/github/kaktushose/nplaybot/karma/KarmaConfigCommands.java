package com.github.kaktushose.nplaybot.karma;

import com.github.kaktushose.jda.commands.annotations.interactions.CommandConfig;
import com.google.inject.Inject;
import com.github.kaktushose.jda.commands.annotations.constraints.Max;
import com.github.kaktushose.jda.commands.annotations.constraints.Min;
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction;
import com.github.kaktushose.jda.commands.annotations.interactions.Permissions;
import com.github.kaktushose.jda.commands.annotations.interactions.Command;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.nplaybot.Database;
import com.github.kaktushose.nplaybot.permissions.BotPermissions;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

@Interaction
@CommandConfig(enabledFor = Permission.BAN_MEMBERS)
public class KarmaConfigCommands {

    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;

    @Command(value = "balance add karma", desc = "Fügt einem User Karma hinzu")
    @Permissions(BotPermissions.MODIFY_USER_BALANCE)
    public void onAddKarma(CommandEvent event, Member target, @Min(Integer.MIN_VALUE) @Max(Integer.MAX_VALUE) Integer amount) {
        var oldKarma = database.getRankService().getUserInfo(target).karma();
        var newKarma = oldKarma + amount;

        database.getKarmaService().addKarma(target, amount);

        if (newKarma > oldKarma) {
            database.getKarmaService().onKarmaIncrease(oldKarma, newKarma, event.getMember(), embedCache);
        } else if (newKarma < oldKarma) {
            database.getKarmaService().onKarmaDecrease(oldKarma, newKarma, event.getMember(), embedCache);
        }

        event.reply(embedCache.getEmbed("addKarmaResult")
                .injectValue("user", target.getAsMention())
                .injectValue("karma", amount)
        );
    }

    @Command(value = "balance set karma", desc = "Setzt die Karma Punkte von einem User auf den angegebenen Wert")
    @Permissions(BotPermissions.MODIFY_USER_BALANCE)
    public void onSetKarma(CommandEvent event, Member target, @Min(Integer.MIN_VALUE) @Max(Integer.MAX_VALUE) Integer value) {
        var oldKarma = database.getRankService().getUserInfo(target).karma();
        database.getKarmaService().setKarma(target, value);

        if (value > oldKarma) {
            database.getKarmaService().onKarmaIncrease(oldKarma, value, event.getMember(), embedCache);
        } else if (value < oldKarma) {
            database.getKarmaService().onKarmaDecrease(oldKarma, value, event.getMember(), embedCache);
        }

        event.reply(embedCache.getEmbed("setKarmaResult")
                .injectValue("user", target.getAsMention())
                .injectValue("karma", value)
        );
    }

    @Command(value = "karma-config set default-karma-tokens", desc = "Legt die tägliche Anzahl an Karma-Tokens für jeden Nutzer fest")
    @Permissions(BotPermissions.MANAGE_KARMA_SETTINGS)
    public void onSetKarmaTokens(CommandEvent event, @Min(1) @Max(Integer.MAX_VALUE) Integer value) {
        database.getKarmaService().setDefaultTokens(value);
        onGetKarmaConfig(event);
    }

    @Command(value = "karma-config display", desc = "Zeigt die Einstellungen für das Karma System an")
    @Permissions(BotPermissions.MANAGE_KARMA_SETTINGS)
    public void onGetKarmaConfig(CommandEvent event) {
        var emojis = database.getKarmaService().getValidUpvoteEmojis();
        var builder = new StringBuilder();
        emojis.forEach(it -> builder.append(it.getFormatted()).append(" "));
        event.reply(embedCache.getEmbed("karmaConfig")
                .injectValue("emojis", builder)
                .injectValue("tokens", database.getKarmaService().getDefaultTokens())
        );
    }
}
