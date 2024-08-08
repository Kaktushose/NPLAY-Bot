package com.github.kaktushose.nplaybot.permissions;

import com.github.kaktushose.nplaybot.Bot;
import net.dv8tion.jda.api.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class PermissionsService {

    private static final Logger log = LoggerFactory.getLogger(PermissionsService.class);
    private final DataSource dataSource;

    public PermissionsService(DataSource dataSource, Bot bot) {
        this.dataSource = dataSource;
    }

    public int getUserPermissions(User user) {
        log.debug("Querying permissions for user {}", user);
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT permissions
                    FROM users
                    WHERE user_id = ?
                    """
            );
            statement.setLong(1, user.getIdLong());

            var result = statement.executeQuery();
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getRolePermissions(List<Role> roles) {
        log.debug("Querying permissions for member roles");
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("SELECT * FROM get_role_permissions(?)");
            statement.setArray(1, connection.createArrayOf("BIGINT", roles.stream().map(ISnowflake::getIdLong).toArray()));
            var result = statement.executeQuery();
            result.next();
            var permissions = result.getInt(1);
            if (permissions >= BotPermissions.getPermissionValue(BotPermissions.BOT_OWNER)) {
                permissions = BotPermissions.getPermissionValue(BotPermissions.BOT_OWNER);
            }
            return permissions;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the combined permissions from {@link #getUserPermissions(User)} and {@link #getRolePermissions(List)} for
     * the given member
     *
     * @param member the {@link Member} to return the combined permissions for
     * @return the combined permissions
     */
    public int getMemberPermissions(Member member) {
        log.debug("Querying permissions for member {}", member);
        int permissions = getUserPermissions(member.getUser()) | getRolePermissions(member.getRoles());

        if (permissions >= BotPermissions.getPermissionValue(BotPermissions.BOT_OWNER)) {
            permissions = BotPermissions.getPermissionValue(BotPermissions.BOT_OWNER);
        }
        return permissions;
    }

    public void setUserPermissions(UserSnowflake user, int permissions) {
        log.info("Granting permission {} for user {}", permissions, user);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("""
                    UPDATE users
                    SET permissions = ?
                    WHERE user_id = ?
                    """
            );

            statement.setLong(1, permissions);
            statement.setLong(2, user.getIdLong());

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setRolePermissions(Role role, int permissions) {
        log.info("Granting permission {} for role {}", permissions, role);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO role_permissions
                    VALUES(?, ?)
                    ON CONFLICT (role_id) DO UPDATE SET permissions = EXCLUDED.permissions;
                    """
            );

            statement.setLong(1, role.getIdLong());
            statement.setLong(2, permissions);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasUserPermissions(Member member) {
        return hasPermissions(member, BotPermissions.USER);
    }

    public boolean hasPermissions(Member member, String... permissions) {
        log.debug("Checking permissions {} for member {}", permissions, member);
        return hasPermissions(member, Set.of(permissions));
    }

    public boolean hasPermissions(Member member, Set<String> permissions) {
        log.debug("Checking permissions {} for member {}", permissions, member);
        var hasPerms = BotPermissions.hasPermissions(permissions, getMemberPermissions(member));
        if (!hasPerms) {
            log.info("Denying access for user {} due to missing permissions", member);
        }
        return hasPerms;
    }

    public boolean hasPermissions(User user, Set<String> permissions) {
        log.debug("Checking permissions {} for user {}", permissions, user);
        var hasPerms = BotPermissions.hasPermissions(permissions, getUserPermissions(user));
        if (!hasPerms) {
            log.info("Denying access for user {} due to missing permissions", user);
        }
        return hasPerms;
    }

}
