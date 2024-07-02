package com.github.kaktushose.nplaybot.permissions;

import com.github.kaktushose.jda.commands.dispatching.interactions.Context;
import com.github.kaktushose.jda.commands.permissions.PermissionsProvider;
import com.github.kaktushose.nplaybot.Database;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

public class CustomPermissionsProvider implements PermissionsProvider {

    private final Database database;

    public CustomPermissionsProvider(Database database) {
        this.database = database;
    }

    @Override
    public boolean hasPermission(@NotNull User user, @NotNull Context context) {
        return database.getPermissionsService().hasPermissions(user, context.getInteractionDefinition().getPermissions());
    }

    @Override
    public boolean hasPermission(@NotNull Member member, @NotNull Context context) {
        return database.getPermissionsService().hasPermissions(member, context.getInteractionDefinition().getPermissions());
    }
}
