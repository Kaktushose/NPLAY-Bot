package de.kaktushose.levelbot.commands.moderation;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.kaktushose.levelbot.account.data.BotUser;
import de.kaktushose.levelbot.account.data.UserService;
import net.dv8tion.jda.api.entities.Member;

@CommandController(value = "setperms", category = "Moderation")
@Permission("moderator")
public class SetPermsCommand {

    @Inject
    private UserService userService;
    @Inject
    private EmbedCache embedCache;

    @Command(
            name = "Berechtigung ändern",
            usage = "{prefix}setperms <member> <level>",
            desc = "Setzt das Berechtigungslevel eines Benutzers auf den angegebenen Wert"
    )
    public void onSetPerms(CommandEvent event, Member member, int level) {
        if (level < 1 || level > 4) {
            event.reply(embedCache.getEmbed("invalidValue")
                    .injectValue("min", 1)
                    .injectValue("max", 4)
            );
            return;
        }

        BotUser executor = userService.getUserById(event.getAuthor().getIdLong());
        BotUser target = userService.getUserById(member.getIdLong());

        // can only update users with lower perms
        if (executor.getPermissionLevel() < level + 1 || executor.getPermissionLevel() < target.getPermissionLevel()) {
            event.reply(embedCache.getEmbed("permissionSetInvalidTarget")
                    .injectValue("user", member.getAsMention())
            );
            return;
        }

        userService.setPermission(target.getUserId(), level);

        event.reply(embedCache.getEmbed("permissionSet")
                .injectValue("user", member.getAsMention())
                .injectValue("value", level)
        );
    }
}
