package de.kaktushose.levelbot.bot.commands.moderation;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.annotations.constraints.NotUser;
import com.github.kaktushose.jda.commands.annotations.interactions.Choices;
import com.github.kaktushose.jda.commands.annotations.interactions.Param;
import com.github.kaktushose.jda.commands.dispatching.CommandEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import de.kaktushose.levelbot.account.data.BotUser;
import de.kaktushose.levelbot.account.data.UserService;
import net.dv8tion.jda.api.entities.Member;

@CommandController(value = "set perms", category = "Moderation", ephemeral = true)
@Permission("moderator")
public class SetPermsCommand {

    private static final String KAKTUSHOSE_USER_ID = "393843637437464588";

    @Inject
    private UserService userService;
    @Inject
    private EmbedCache embedCache;

    @Command(name = "Berechtigung ändern", desc = "Ändert das Berechtigungslevel eines Users")
    public void onSetPerms(CommandEvent event, @NotUser(KAKTUSHOSE_USER_ID) Member member,
                           @Param("Das neue Berechtigungslevel")
                           @Choices({"user", "moderation", "admin", "owner"}) String level) {
        BotUser executor = userService.getBotUser(event.getAuthor());
        BotUser target = userService.getBotUser(member);
        int permissionLevel = getLevel(level);

        // can only update users with lower perms and also not the target itself
        if (executor.getPermissionLevel() < permissionLevel + 1 ||
                executor.getPermissionLevel() < target.getPermissionLevel() ||
                executor.getUserId().equals(target.getUserId())) {
            event.reply(embedCache.getEmbed("permissionSetInvalidTarget")
                    .injectValue("user", member.getAsMention())
            );
            return;
        }

        userService.setPermission(member, permissionLevel);

        event.reply(embedCache.getEmbed("permissionSet")
                .injectValue("user", member.getAsMention())
                .injectValue("value", level)
        );
    }

    private int getLevel(String choice) {
        return switch (choice) {
            case "moderation" -> 2;
            case "admin" -> 3;
            case "owner" -> 4;
            default -> 1;
        };
    }

}
