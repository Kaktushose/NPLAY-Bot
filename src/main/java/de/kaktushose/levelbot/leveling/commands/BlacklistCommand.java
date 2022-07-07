package de.kaktushose.levelbot.leveling.commands;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.kaktushose.levelbot.account.data.BotUser;
import de.kaktushose.levelbot.account.data.UserService;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

@CommandController(value = {"blacklist", "banlist", "bl"}, category = "Moderation")
@Permission("moderator")
public class BlacklistCommand {

    @Inject
    private UserService userService;
    @Inject
    private EmbedCache embedCache;

    @Command(
            name = "Blacklist",
            usage = "{prefix}blacklist <add|remove> <member> | {prefix}blacklist list",
            desc = "Benutzer, die auf der Blacklist stehen, können keine Commands ausführen",
            isSuper = true
    )
    public void onBlacklist(CommandEvent event) {
        event.sendSpecificHelpMessage();
    }

    @Command(
            value = "add",
            name = "Benutzer sperren",
            usage = "{prefix}blacklist add <member>",
            desc = "Fügt einen Benutzer zur Blacklist hinzu"
    )
    public void onBlacklistAdd(CommandEvent event, Member member) {
        BotUser executor = userService.getUserById(event.getAuthor().getIdLong());
        BotUser target = userService.getUserById(member.getIdLong());
        // can only blacklist users with lower permissions
        if (executor.getPermissionLevel() < target.getPermissionLevel()) {
            event.reply(embedCache.getEmbed("memberBlacklistInvalidTarget").injectValue("user", member.getAsMention()));
            return;
        }
        userService.setPermission(member.getIdLong(), 0);
        event.reply(embedCache.getEmbed("memberBlacklistAdd").injectValue("user", member.getAsMention()));
    }

    @Command(
            value = {"remove", "rm"},
            name = "Benutzer entsperren",
            usage = "{prefix}blacklist remove <member>",
            desc = "Entfernt einen Benutzer von der Blacklist"
    )
    public void onBlacklistRemove(CommandEvent event, Member member) {
        BotUser target = userService.getUserById(member.getIdLong());
        userService.setPermission(member.getIdLong(), 1);
        event.reply(embedCache.getEmbed("memberBlacklistRemove")
                .injectValue("user", member.getAsMention())
        );
    }

    @Command(
            value = {"show", "list", "view"},
            name = "Gesperrte Benutzer",
            usage = "{prefix}blacklist show",
            desc = "Zeigt alle Nutzer, die auf der Blacklist stehen"
    )
    public void onBlacklistShow(CommandEvent event) {
        List<Long> blacklist = userService.getUsersByPermission(0);
        StringBuilder members = new StringBuilder();
        blacklist.forEach(id -> members.append(event.getGuild().retrieveMemberById(id).complete().getEffectiveName()).append(", "));
        event.reply(embedCache.getEmbed("memberBlacklistShow")
                .injectValue("blacklist", members.substring(0, members.length() - 2))
        );
    }
}
