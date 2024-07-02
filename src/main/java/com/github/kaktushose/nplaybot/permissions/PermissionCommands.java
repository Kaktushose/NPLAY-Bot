package com.github.kaktushose.nplaybot.permissions;

import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.data.EmbedCache;
import com.github.kaktushose.jda.commands.dispatching.interactions.commands.CommandEvent;
import com.github.kaktushose.jda.commands.dispatching.interactions.components.ComponentEvent;
import com.github.kaktushose.nplaybot.Database;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

import java.util.List;

import static com.github.kaktushose.nplaybot.permissions.BotPermissions.*;

@Interaction
public class PermissionCommands {

    private static final String NONE = "NONE";
    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;
    private Member targetMember;
    private Role targetRole;

    @SlashCommand(value = "permissions list", desc = "Zeigt die Berechtigungen eines Users an", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    @Permissions(USER)
    public void onPermissionsList(CommandEvent event, @Optional Member member) {
        var target = member == null ? event.getMember() : member;

        event.reply(embedCache.getEmbed("permissionsList")
                .injectValue("target", target.getEffectiveName())
                .injectValue("permissions", BotPermissions.listPermissions(database.getPermissionsService().getMemberPermissions(target)))
        );
    }

    @SlashCommand(value = "permissions user edit", desc = "Bearbeitet die Berechtigungen von einem Nutzer. Hat keinen Einfluss auf die Rollen-Berechtigungen", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    @Permissions(MANAGE_USER_PERMISSIONS)
    public void onPermissionsUserEdit(CommandEvent event, Member member) {
        targetMember = member;

        var permissionsMap = BotPermissions.permissionsMapping();
        permissionsMap.put(NONE, 0);
        permissionsMap.remove(BOT_OWNER);

        var menu = event.getSelectMenu(
                "PermissionCommands.onPermissionsUserSelect",
                net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.class
        ).createCopy();
        menu.getOptions().clear();
        menu.setMaxValues(SelectMenu.OPTIONS_MAX_AMOUNT);

        permissionsMap.forEach((label, value) -> menu.addOption(label, String.valueOf(value)));
        var permissions = database.getPermissionsService().getUserPermissions(member.getUser());
        menu.setDefaultValues(BotPermissions.getRawPermissionsValues(permissions).stream().map(String::valueOf).toList());

        event.getReplyContext().getBuilder().addActionRow(menu.build());
        event.reply(embedCache.getEmbed("permissionsEdit").injectValue("target", targetMember.getEffectiveName()));
    }

    @StringSelectMenu(value = "Wähle eine oder mehrere Berechtigungen aus")
    @SelectOption(label = "dummy option", value = "dummy option")
    @Permissions(MANAGE_USER_PERMISSIONS)
    public void onPermissionsUserSelect(ComponentEvent event, List<String> selection) {
        database.getPermissionsService().setUserPermissions(targetMember, BotPermissions.combine(selection.stream().map(Integer::valueOf).toList()));

        event.keepComponents(false).reply(embedCache.getEmbed("permissionsList")
                .injectValue("target", targetMember.getEffectiveName())
                .injectValue("permissions", BotPermissions.listPermissions(database.getPermissionsService().getUserPermissions(targetMember.getUser())))
        );
    }

    @SlashCommand(value = "permissions role edit", desc = "Bearbeitet die Berechtigungen von einer Rolle", isGuildOnly = true, enabledFor = Permission.BAN_MEMBERS)
    @Permissions(MANAGE_USER_PERMISSIONS)
    public void onPermissionsRoleEdit(CommandEvent event, Role role) {
        targetRole = role;

        var permissionsMap = BotPermissions.permissionsMapping();
        permissionsMap.put(NONE, 0);
        permissionsMap.remove(USER);
        permissionsMap.remove(BOT_OWNER);

        var menu = event.getSelectMenu(
                "PermissionCommands.onPermissionsRoleSelect",
                net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu.class
        ).createCopy();
        menu.getOptions().clear();
        menu.setMaxValues(SelectMenu.OPTIONS_MAX_AMOUNT);

        permissionsMap.forEach((label, value) -> menu.addOption(label, String.valueOf(value)));
        var permissions = database.getPermissionsService().getRolePermissions(List.of(targetRole));
        menu.setDefaultValues(BotPermissions.getRawPermissionsValues(permissions).stream().map(String::valueOf).toList());

        event.getReplyContext().getBuilder().addActionRow(menu.build());
        event.reply(embedCache.getEmbed("permissionsEdit").injectValue("target", targetRole.getName()));
    }

    @StringSelectMenu(value = "Wähle eine oder mehrere Berechtigungen aus")
    @SelectOption(label = "dummy option", value = "dummy option")
    @Permissions(MANAGE_USER_PERMISSIONS)
    public void onPermissionsRoleSelect(ComponentEvent event, List<String> selection) {
        database.getPermissionsService().setRolePermissions(targetRole, BotPermissions.combine(selection.stream().map(Integer::valueOf).toList()));

        event.keepComponents(false).reply(embedCache.getEmbed("permissionsList")
                .injectValue("target", targetRole.getName())
                .injectValue("permissions", BotPermissions.listPermissions(database.getPermissionsService().getRolePermissions(List.of(targetRole))))
        );
    }
}