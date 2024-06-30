package com.github.kaktushose.nplaybot.items;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ItemService {

    private final DataSource dataSource;
    private static final int PLAY_ACTIVITY_ITEM_ID = 7;

    public ItemService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Item> getAllItems() {
        try (Connection connection = dataSource.getConnection()) {
            var items = new ArrayList<Item>();

            var statement = connection.prepareStatement("""
                    SELECT *
                    FROM items
                    JOIN item_types ON item_types.base_type_id = items.type_id
                    """
            );
            var result = statement.executeQuery();
            while (result.next()) {
                items.add(new Item(
                        result.getInt("item_id"),
                        result.getInt("type_id"),
                        result.getString("name"),
                        result.getLong("duration"),
                        result.getLong("role_id")
                ));
            }

            return items;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Item getItem(int itemId) {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT *
                    FROM items
                    JOIN item_types ON item_types.base_type_id = items.type_id
                    WHERE item_id = ?
                    """
            );
            statement.setLong(1, itemId);
            var result = statement.executeQuery();
            result.next();
            return new Item(
                    result.getInt("item_id"),
                    result.getInt("type_id"),
                    result.getString("name"),
                    result.getLong("duration"),
                    result.getLong("role_id")
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Transaction> getTransactions(UserSnowflake user) {
        try (Connection connection = dataSource.getConnection()) {
            var transactions = new ArrayList<Transaction>();

            var statement = connection.prepareStatement("""
                    SELECT *
                    FROM transactions
                    JOIN items ON transactions.item_id = items.item_id
                    JOIN item_types ON item_types.base_type_id = items.type_id
                    WHERE user_id = ?
                    """
            );
            statement.setLong(1, user.getIdLong());
            var result = statement.executeQuery();
            while (result.next()) {
                transactions.add(new Transaction(
                        result.getInt("transaction_id"),
                        result.getLong("user_id"),
                        result.getInt("item_id"),
                        result.getInt("type_id"),
                        result.getString("name"),
                        result.getLong("duration"),
                        result.getLong("expires_at"),
                        result.getLong("role_id"),
                        result.getBoolean("is_play_activity")
                ));
            }

            return transactions;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createTransaction(UserSnowflake user, int itemId, Guild guild) {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("INSERT INTO transactions (\"user_id\", \"item_id\", \"expires_at\") VALUES(?, ?, ?)");
            statement.setLong(1, user.getIdLong());
            statement.setInt(2, itemId);
            var item = getItem(itemId);
            statement.setLong(3, System.currentTimeMillis() + item.duration);
            if (item.roleId > 0) {
                guild.addRoleToMember(user, guild.getRoleById(item.roleId())).queue();
            }
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteTransaction(UserSnowflake user, int transactionId, Guild guild) {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("SELECT item_id FROM transactions WHERE transaction_id = ?");
            statement.setInt(1, transactionId);
            var result = statement.executeQuery();
            result.next();
            var item = getItem(result.getInt(1));
            if (item.roleId > 0) {
                guild.removeRoleFromMember(user, guild.getRoleById(item.roleId())).queue();
            }

            statement = connection.prepareStatement("DELETE FROM transactions WHERE transaction_id = ?");
            statement.setLong(1, transactionId);
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getTypeEmoji(int typeId) {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("SELECT emoji FROM item_types where base_type_id = ?");
            statement.setInt(1, typeId);
            var result = statement.executeQuery();
            result.next();
            return result.getString(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateLastKarma(UserSnowflake user) {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("UPDATE users SET last_karma = karma_points WHERE user_id = ?");
            statement.setLong(1, user.getIdLong());
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void addPlayActivity(UserSnowflake user, Guild guild) {
        try (Connection connection = dataSource.getConnection()) {
            var item = getItem(PLAY_ACTIVITY_ITEM_ID);
            var statement = connection.prepareStatement("INSERT INTO transactions (\"user_id\", \"item_id\", \"expires_at\", \"is_play_activity\") VALUES(?, ?, ?, true)");
            statement.setLong(1, user.getIdLong());
            statement.setInt(2, PLAY_ACTIVITY_ITEM_ID);
            statement.setLong(3, System.currentTimeMillis() + item.duration);
            statement.execute();

            if (item.roleId > 0) {
                guild.addRoleToMember(user, guild.getRoleById(item.roleId())).queue();
            }

            updateLastKarma(user);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Transaction> getExpiringTransactions() {
        try (Connection connection = dataSource.getConnection()) {
            var transactions = new ArrayList<Transaction>();

            var statement = connection.prepareStatement("""
                    SELECT *
                    FROM transactions
                    JOIN items ON transactions.item_id = items.item_id
                    JOIN item_types ON item_types.base_type_id = items.type_id
                    WHERE expires_at < ?
                    """
            );
            statement.setLong(1, System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24));
            var result = statement.executeQuery();
            while (result.next()) {
                transactions.add(new Transaction(
                        result.getInt("transaction_id"),
                        result.getLong("user_id"),
                        result.getInt("item_id"),
                        result.getInt("type_id"),
                        result.getString("name"),
                        result.getLong("duration"),
                        result.getLong("expires_at"),
                        result.getLong("role_id"),
                        result.getBoolean("is_play_activity")
                ));
            }

            return transactions;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public record Item(int itemId, int typeId, String name, long duration, long roleId) {
    }

    public record Transaction(int transactionId, long userId, int itemId, int typeId, String name, long duration,
                              long expiresAt,
                              long roleId, boolean isPlayActivity) {
    }

}
