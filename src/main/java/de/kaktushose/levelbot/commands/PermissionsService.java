package de.kaktushose.levelbot.commands;

import com.github.kaktushose.jda.commands.annotations.Component;
import com.github.kaktushose.jda.commands.annotations.Inject;
import com.github.kaktushose.jda.commands.dispatching.CommandContext;
import com.github.kaktushose.jda.commands.permissions.PermissionsProvider;
import de.kaktushose.levelbot.database.services.UserService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

@Component
public class PermissionsService implements PermissionsProvider {

    @Inject
    private UserService userService;

    @Override
    public boolean isMuted(@NotNull User user, @NotNull CommandContext context) {
        return userService.getMutedUsers().contains(user.getIdLong());
    }

    @Override
    public boolean hasPermission(@NotNull User user, @NotNull CommandContext context) {
        for (String permission : context.getCommand().getPermissions()) {
            if (!userService.getUsersByPermission(getLevelByName(permission)).contains(user.getIdLong())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull Member member, @NotNull CommandContext context) {
        return hasPermission(member.getUser(), context);
    }

    private int getLevelByName(String name) {
        switch (name) {
            case "moderator":
                return 2;
            case "admin":
                return 3;
            default:
                //for security reasons, commands with an unknown permission string can only be executed by level owner
                return 4;
        }
    }

}
