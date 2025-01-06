package com.github.kaktushose.nplaybot.permissions;


import com.github.kaktushose.jda.commands.dispatching.context.InvocationContext;
import com.github.kaktushose.jda.commands.permissions.PermissionsProvider;
import com.github.kaktushose.nplaybot.Database;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class CustomPermissionsProvider implements PermissionsProvider {

    private final Database database;

    public CustomPermissionsProvider(Database database) {
        this.database = database;
    }

    @Override
    public boolean hasPermission(@NotNull User user, InvocationContext<?> context) {
        return database.getPermissionsService().hasPermissions(user, new HashSet<>(context.definition().permissions()));
    }

    @Override
    public boolean hasPermission(@NotNull Member member, InvocationContext<?> context) {
        return database.getPermissionsService().hasPermissions(member, new HashSet<>(context.definition().permissions()));
    }
}
