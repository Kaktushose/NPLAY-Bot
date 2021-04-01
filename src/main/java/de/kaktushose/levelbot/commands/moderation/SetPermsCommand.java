package de.kaktushose.levelbot.commands.moderation;

import com.github.kaktushose.jda.commands.annotations.Command;
import com.github.kaktushose.jda.commands.annotations.CommandController;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.Permission;
import com.github.kaktushose.jda.commands.api.EmbedCache;
import com.github.kaktushose.jda.commands.entities.CommandEvent;
import com.github.kaktushose.jda.commands.entities.CommandSettings;
import de.kaktushose.levelbot.database.Database;
import de.kaktushose.levelbot.database.model.BotUser;
import net.dv8tion.jda.api.entities.Member;

import java.util.Optional;

@CommandController("setperms")
@Permission("moderator")
public class SetPermsCommand {

    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;

    @Command(
            name = "Berechtigung ändern",
            usage = "{prefix}setperms <member> <level>",
            desc = "Setzt das Berechtigungslevel eines Benutzers auf den angegebenen Wert",
            category = "Moderation"
    )
    public void onSetPerms(CommandEvent event, Member member, int level) {
        Optional<BotUser> optional = database.getUsers().findById(member.getIdLong());
        if (optional.isEmpty()) {
            event.reply(embedCache.getEmbed("memberNotFound").toEmbedBuilder());
            return;
        }

        if (level < 1 || level > 4) {
            event.reply(embedCache.getEmbed("invalidValue")
                    .injectValue("min", 1)
                    .injectValue("max", 4)
                    .toEmbedBuilder()
            );
            return;
        }

        BotUser executor = database.getUsers().findById(event.getAuthor().getIdLong()).orElseThrow();
        BotUser target = optional.get();

        // can only update users with lower perms
        if (executor.getPermissionLevel() < level + 1 || executor.getPermissionLevel() < target.getPermissionLevel()) {
            event.reply(embedCache.getEmbed("permissionSetInvalidTarget")
                    .injectValue("user", member.getAsMention())
                    .toEmbedBuilder()
            );
            return;
        }

        // update in db
        target.setPermissionLevel(level);
        database.getUsers().save(target);
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
        }

        // reply
        event.reply(embedCache.getEmbed("permissionSet")
                .injectValue("user", member.getAsMention())
                .injectValue("value", level)
                .toEmbedBuilder()
        );
    }
}
