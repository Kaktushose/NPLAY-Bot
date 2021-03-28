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

import java.util.List;
import java.util.Optional;

@CommandController({"blacklist", "banlist", "bl"})
@Permission("moderator")
public class BlacklistCommand {

    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;

    @Command(
            value = "add",
            name = "Benutzer sperren",
            usage = "{prefix}blacklist add <member>",
            desc = "Fügt einen Benutzer zur Blacklist hinzu",
            category = "Moderation"
    )
    public void onBlacklistAdd(CommandEvent event, Member member) {
        Optional<BotUser> optional = database.getUsers().findById(member.getIdLong());

        if (optional.isPresent()) {
            BotUser executor = database.getUsers().findById(event.getAuthor().getIdLong()).orElseThrow();
            BotUser target = optional.get();
            // can only blacklist users with lower permissions
            if (executor.getPermissionLevel() < target.getPermissionLevel()) {
                event.reply(
                        embedCache.getEmbed("memberBlacklistInvalidTarget")
                                .injectValue("user", member.getAsMention())
                                .toEmbedBuilder()
                );
                return;
            }
            // update in db
            target.setPermissionLevel(0);
            database.getUsers().save(target);
            // update in jda-commands
            event.getJdaCommands().getDefaultSettings().getMutedUsers().add(member.getIdLong());
            // reply
            event.reply(embedCache.getEmbed("memberBlacklistAdd")
                    .injectValue("user", member.getAsMention())
                    .toEmbedBuilder()
            );
        } else {
            event.reply(embedCache.getEmbed("memberNotFound").toEmbedBuilder());
        }
    }

    @Command(
            value = {"remove", "rm"},
            name = "Benutzer entsperren",
            usage = "{prefix}blacklist remove <member>",
            desc = "Entfernt einen Benutzer von der Blacklist",
            category = "Moderation"
    )
    public void onBlacklistRemove(CommandEvent event, Member member) {
        Optional<BotUser> optional = database.getUsers().findById(member.getIdLong());

        if (optional.isPresent()) {
            BotUser executor = database.getUsers().findById(event.getAuthor().getIdLong()).orElseThrow();
            BotUser target = optional.get();
            // update in db
            target.setPermissionLevel(1);
            database.getUsers().save(target);
            // update in jda-commands
            event.getJdaCommands().getDefaultSettings().getMutedUsers().remove(member.getIdLong());
            // reply
            event.reply(embedCache.getEmbed("memberBlacklistRemove")
                    .injectValue("user", member.getAsMention())
                    .toEmbedBuilder()
            );
        } else {
            event.reply(embedCache.getEmbed("memberNotFound").toEmbedBuilder());
        }
    }

    @Command(
            value = {"show", "list", "view"},
            name = "Gesperrte Benutzer",
            usage = "{prefix}blacklist show",
            desc = "Zeigt alle Nutzer, die auf der Blacklist stehen",
            category = "Moderation"
    )
    public void onBlacklistShow(CommandEvent event) {
        List<BotUser> blacklist = database.getUsers().findByPermissionLevel(0);
        StringBuilder members = new StringBuilder();
        blacklist.forEach(botUser -> members.append(event.getGuild().getMemberById(botUser.getUserId()).getEffectiveName()).append(", "));
        event.reply(
                embedCache.getEmbed("memberBlacklistShow")
                        .injectValue("blacklist", members.substring(0, members.length() - 2))
                .toEmbedBuilder()
        );
    }
}
