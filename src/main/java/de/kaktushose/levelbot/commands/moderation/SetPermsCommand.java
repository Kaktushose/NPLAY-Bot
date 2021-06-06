package de.kaktushose.levelbot.commands.moderation;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.entities.CommandEvent;
import com.github.kaktushose.jda.commands.entities.CommandSettings;
import de.kaktushose.levelbot.database.model.BotUser;
import de.kaktushose.levelbot.database.services.UserService;
import net.dv8tion.jda.api.entities.Member;

@CommandController("setperms")
@Permission("moderator")
public class SetPermsCommand {

    @Inject
    private UserService userService;
    @Inject
    private EmbedCache embedCache;

    @Command(
            name = "Berechtigung ändern",
            usage = "{prefix}setperms <member> <level>",
            desc = "Setzt das Berechtigungslevel eines Benutzers auf den angegebenen Wert",
            category = "Moderation"
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

        // update in db
        userService.setPermission(target.getUserId(), level);
        // update in jda-commands
        CommandSettings commandSettings = event.getJdaCommands().getDefaultSettings();
        // first remove user from all levels, then reapply them
        commandSettings.getPermissionHolders("owner").remove(target.getUserId());
        commandSettings.getPermissionHolders("admin").remove(target.getUserId());
        commandSettings.getPermissionHolders("moderator").remove(target.getUserId());
        switch (level) {
            case 4:
                commandSettings.getPermissionHolders("owner").add(target.getUserId());
            case 3:
                commandSettings.getPermissionHolders("admin").add(target.getUserId());
            case 2:
                commandSettings.getPermissionHolders("moderator").add(target.getUserId());
            case 1:
                commandSettings.getMutedUsers().remove(target.getUserId());
        }

        // reply
        event.reply(embedCache.getEmbed("permissionSet")
                .injectValue("user", member.getAsMention())
                .injectValue("value", level)
        );
    }
}
