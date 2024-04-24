package com.github.kaktushose.nplaybot.permissions;

import net.dv8tion.jda.api.entities.UserSnowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

public class PermissionsService {

    private static final Logger log = LoggerFactory.getLogger(PermissionsService.class);
    private final DataSource dataSource;

    public PermissionsService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int getPermissions(UserSnowflake user) {
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

    public void grantPermissions(UserSnowflake user, String permission) {
        log.warn("Granting permission {} for user {}", permission, user);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("""
                    UPDATE users
                    SET permissions = ?
                    WHERE user_id = ?
                    """
            );

            statement.setLong(1, BotPermissions.grant(getPermissions(user), permission));
            statement.setLong(2, user.getIdLong());

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void revokePermissions(UserSnowflake user, String permission) {
        log.warn("Revoking permission {} for user {}", permission, user);
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("""
                    UPDATE users
                    SET permissions = ?
                    WHERE user_id = ?
                    """
            );

            statement.setLong(1, BotPermissions.revoke(getPermissions(user), permission));
            statement.setLong(2, user.getIdLong());

            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasPermissions(UserSnowflake user, Set<String> permissions) {
        log.debug("Checking permissions for user {}", user);
        return BotPermissions.hasPermissions(permissions, getPermissions(user));
    }
}
