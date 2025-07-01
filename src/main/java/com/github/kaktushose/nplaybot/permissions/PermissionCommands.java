package com.github.kaktushose.nplaybot.permissions;

import com.github.kaktushose.jda.commands.dispatching.reply.Component;
import com.google.inject.Inject;
import com.github.kaktushose.jda.commands.annotations.interactions.*;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ComponentEvent;
import com.github.kaktushose.jda.commands.embeds.EmbedCache;
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent;
import com.github.kaktushose.nplaybot.Database;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.BitSet;
import java.util.List;

import static com.github.kaktushose.nplaybot.permissions.BotPermissions.*;

@Interaction
@CommandConfig(enabledFor = Permission.BAN_MEMBERS)
public class PermissionCommands {

    private static final String NONE = "NONE";
    @Inject
    private Database database;
    @Inject
    private EmbedCache embedCache;
    private Member targetMember;
    private Role targetRole;

    @Command(value = "permissions list", desc = "Zeigt die Berechtigungen eines Users an")
    @Permissions(USER)
    public void onPermissionsList(CommandEvent event, @Param(optional = true) Member member) {
        var target = member == null ? event.getMember() : member;

        event.reply(embedCache.getEmbed("permissionsList")
                .injectValue("target", target.getEffectiveName())
                .injectValue("permissions", BotPermissions.listPermissions(database.getPermissionsService().getMemberPermissions(target)))
        );
    }

    @Command(value = "permissions user edit", desc = "Bearbeitet die Berechtigungen von einem Nutzer. Hat keinen Einfluss auf die Rollen-Berechtigungen")
    @Permissions(MANAGE_USER_PERMISSIONS)
    public void onPermissionsUserEdit(CommandEvent event, Member member) {
        targetMember = member;

        var permissionsMap = BotPermissions.permissionsMapping();
        permissionsMap.put(NONE, 0);
        permissionsMap.remove(BOT_OWNER);

        List<SelectOption> options = permissionsMap.entrySet().stream()
                .map(it -> SelectOption.of(it.getKey(), String.valueOf(it.getValue())))
                .toList();
        event.with()
                .components(Component.stringSelect("onPermissionsUserSelect").selectOptions(options))
                .reply(embedCache.getEmbed("permissionsEdit").injectValue("target", targetMember.getEffectiveName()));
    }

    @StringSelectMenu(value = "Wähle eine oder mehrere Berechtigungen aus")
    @Permissions(MANAGE_USER_PERMISSIONS)
    public void onPermissionsUserSelect(ComponentEvent event, List<String> selection) {
        database.getPermissionsService().setUserPermissions(targetMember, BotPermissions.combine(selection.stream().map(Integer::valueOf).toList()));
        event.with().keepComponents(false).reply(embedCache.getEmbed("permissionsList")
                .injectValue("target", targetMember.getEffectiveName())
                .injectValue("permissions", BotPermissions.listPermissions(database.getPermissionsService().getUserPermissions(targetMember.getUser())))
        );
    }

    @Command(value = "permissions role edit", desc = "Bearbeitet die Berechtigungen von einer Rolle")
    @Permissions(MANAGE_USER_PERMISSIONS)
    public void onPermissionsRoleEdit(CommandEvent event, Role role) {
        targetRole = role;

        var permissionsMap = BotPermissions.permissionsMapping();
        permissionsMap.put(NONE, 0);
        permissionsMap.remove(USER);
        permissionsMap.remove(BOT_OWNER);

        List<SelectOption> options = permissionsMap.entrySet().stream()
                .map(it -> SelectOption.of(it.getKey(), String.valueOf(it.getValue())))
                .toList();
        event.with()
                .components(Component.stringSelect("onPermissionsRoleSelect").selectOptions(options))
                .reply(embedCache.getEmbed("permissionsEdit").injectValue("target", targetMember.getEffectiveName()));
    }

    @StringSelectMenu(value = "Wähle eine oder mehrere Berechtigungen aus")
    @Permissions(MANAGE_USER_PERMISSIONS)
    public void onPermissionsRoleSelect(ComponentEvent event, List<String> selection) {
        database.getPermissionsService().setRolePermissions(targetRole, BotPermissions.combine(selection.stream().map(Integer::valueOf).toList()));

        event.with().keepComponents(false).reply(embedCache.getEmbed("permissionsList")
                .injectValue("target", targetRole.getName())
                .injectValue("permissions", BotPermissions.listPermissions(database.getPermissionsService().getRolePermissions(List.of(targetRole))))
        );
    }
}
