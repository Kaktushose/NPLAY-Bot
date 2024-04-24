package com.github.kaktushose.nplaybot.permissions;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.jda.commands.dispatching.interactions.commands.CommandEvent;
import com.github.kaktushose.nplaybot.Database;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import static com.github.kaktushose.nplaybot.permissions.BotPermissions.*;

@Interaction
public class PermissionCommands {

    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;

    @SlashCommand(value = "permissions list", desc = "Zeigt die Berechtigungen eines Users an", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    @Permissions(USER)
    public void onPermissionsList(CommandEvent event, @Optional Member member) {
        var target = member == null ? event.getMember() : member;

        event.reply(embedCache.getEmbed("userPermissions")
                .injectValue("user", target.getEffectiveName())
                .injectValue("permissions", BotPermissions.listPermissions(database.getPermissionsService().getPermissions(target)))
        );
    }

    @SlashCommand(value = "permissions grant", desc = "Fügt einem Nutzer eine Berechtigung hinzu", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    @Permissions(MODIFY_USER_PERMISSIONS)
    public void onPermissionsGrant(CommandEvent event,
                                   Member member,
                                   @Param("Die Berechtigung die hinzugefügt werden soll")
                                   @Choices({USER, MODIFY_USER_BALANCE, MODIFY_RANK_SETTINGS, MANAGE_EVENTS, MODIFY_USER_PERMISSIONS, BOT_ADMINISTRATOR})
                                   String permission) {
        database.getPermissionsService().grantPermissions(member, permission);

        event.reply(embedCache.getEmbed("userPermissions")
                .injectValue("user", member.getEffectiveName())
                .injectValue("permissions", BotPermissions.listPermissions(database.getPermissionsService().getPermissions(member)))
        );
    }

    @SlashCommand(value = "permissions revoke", desc = "Entzieht einem Nutzer eine Berechtigung", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    @Permissions(MODIFY_USER_PERMISSIONS)
    public void onPermissionsRevoke(CommandEvent event,
                                    Member member,
                                    @Param("Die Berechtigung die entzogen werden soll")
                                    @Choices({USER, MODIFY_USER_BALANCE, MODIFY_RANK_SETTINGS, MANAGE_EVENTS, MODIFY_USER_PERMISSIONS, BOT_ADMINISTRATOR})
                                    String permission) {
        database.getPermissionsService().revokePermissions(member, permission);

        event.reply(embedCache.getEmbed("userPermissions")
                .injectValue("user", member.getEffectiveName())
                .injectValue("permissions", BotPermissions.listPermissions(database.getPermissionsService().getPermissions(member)))
        );
    }

    @SlashCommand(value = "permissions bulk grant", desc = "Fügt allen Nutzern mit der angegebenen Rolle eine Berechtigung hinzu", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    @Permissions(MODIFY_USER_PERMISSIONS)
    public void onPermissionsBulkGrant(CommandEvent event,
                                       Role role,
                                       @Param("Die Berechtigung die hinzugefügt werden soll")
                                       @Choices({USER, MODIFY_USER_BALANCE, MODIFY_RANK_SETTINGS, MANAGE_EVENTS, MODIFY_USER_PERMISSIONS, BOT_ADMINISTRATOR})
                                       String permission) {
        event.getGuild().getMembersWithRoles(role).forEach(member -> database.getPermissionsService().grantPermissions(member, permission));

        event.reply(embedCache.getEmbed("permissionsBulkEdit"));
    }

    @SlashCommand(value = "permissions bulk revoke", desc = "Entzieht allen Nutzern mit der angegebenen Rolle eine Berechtigung", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    @Permissions(MODIFY_USER_PERMISSIONS)
    public void onPermissionsBulkRevoke(CommandEvent event,
                                        Role role,
                                        @Param("Die Berechtigung die entzogen werden soll")
                                        @Choices({USER, MODIFY_USER_BALANCE, MODIFY_RANK_SETTINGS, MANAGE_EVENTS, MODIFY_USER_PERMISSIONS, BOT_ADMINISTRATOR})
                                        String permission) {
        event.getGuild().getMembersWithRoles(role).forEach(member -> database.getPermissionsService().revokePermissions(member, permission));

        event.reply(embedCache.getEmbed("permissionsBulkEdit"));
    }
}
