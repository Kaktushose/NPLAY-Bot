package de.kaktushose.levelbot.commands.moderation;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.entities.CommandEvent;
import de.kaktushose.levelbot.database.model.BotUser;
import de.kaktushose.levelbot.database.services.UserService;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

@CommandController({"blacklist", "banlist", "bl"})
@Permission("moderator")
public class BlacklistCommand {

    @Inject
    private UserService userService;
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
        BotUser executor = userService.getUserById(event.getAuthor().getIdLong());
        BotUser target = userService.getUserById(member.getIdLong());
        // can only blacklist users with lower permissions
        if (executor.getPermissionLevel() < target.getPermissionLevel()) {
            event.reply(embedCache.getEmbed("memberBlacklistInvalidTarget").injectValue("user", member.getAsMention()));
            return;
        }
        // update in db
        userService.setPermission(member.getIdLong(), 0);
        // update in jda-commands
        event.getJdaCommands().getDefaultSettings().getMutedUsers().add(member.getIdLong());
        // reply
        event.reply(embedCache.getEmbed("memberBlacklistAdd").injectValue("user", member.getAsMention()));
    }

    @Command(
            value = {"remove", "rm"},
            name = "Benutzer entsperren",
            usage = "{prefix}blacklist remove <member>",
            desc = "Entfernt einen Benutzer von der Blacklist",
            category = "Moderation"
    )
    public void onBlacklistRemove(CommandEvent event, Member member) {
        BotUser target = userService.getUserById(member.getIdLong());
        // update in db
        userService.setPermission(member.getIdLong(), 1);
        // update in jda-commands
        event.getJdaCommands().getDefaultSettings().getMutedUsers().remove(member.getIdLong());
        // reply
        event.reply(embedCache.getEmbed("memberBlacklistRemove")
                .injectValue("user", member.getAsMention())
        );
    }

    @Command(
            value = {"show", "list", "view"},
            name = "Gesperrte Benutzer",
            usage = "{prefix}blacklist show",
            desc = "Zeigt alle Nutzer, die auf der Blacklist stehen",
            category = "Moderation"
    )
    public void onBlacklistShow(CommandEvent event) {
        List<BotUser> blacklist = userService.getUsersByPermission(0);
        StringBuilder members = new StringBuilder();
        blacklist.forEach(botUser -> members.append(event.getGuild().getMemberById(botUser.getUserId()).getEffectiveName()).append(", "));
        event.reply(embedCache.getEmbed("memberBlacklistShow")
                .injectValue("blacklist", members.substring(0, members.length() - 2))
        );
    }
}