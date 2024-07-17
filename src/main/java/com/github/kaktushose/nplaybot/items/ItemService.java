package com.github.kaktushose.nplaybot.items;

import com.github.kaktushose.nplaybot.Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ItemService {

    private static final int PLAY_ACTIVITY_ITEM_ID = 7;
    public static final int PREMIUM_BASE_TYPE_ID = 2;
    public static final int PREMIUM_UNLIMITED_ITEM_ID = 9;
    private final DataSource dataSource;
    private final Guild guild;

    public ItemService(DataSource dataSource, Bot bot) {
        this.dataSource = dataSource;
        this.guild = bot.getGuild();
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
                    SELECT transaction_id, user_id, items.item_id, items.type_id, item_types.name, duration, expires_at, role_id, is_play_activity
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

    public Optional<Role> createTransaction(UserSnowflake user, int itemId) {
        return createTransaction(user, itemId, false);
    }

    public void addPlayActivity(UserSnowflake user) {
        createTransaction(user, PLAY_ACTIVITY_ITEM_ID, true).ifPresent(it ->
                guild.addRoleToMember(user, it).queue()
        );
    }

    private Optional<Role> createTransaction(UserSnowflake user, int itemId, boolean isPlayActivity) {
        try (Connection connection = dataSource.getConnection()) {
            var item = getItem(itemId);

            if (getTransactions(user).stream().map(Transaction::itemId).toList().contains(PREMIUM_UNLIMITED_ITEM_ID) && (item.typeId == PREMIUM_BASE_TYPE_ID || itemId == PREMIUM_UNLIMITED_ITEM_ID)) {
                return Optional.empty();
            }

            if (itemId == PREMIUM_UNLIMITED_ITEM_ID) {
                var statement = connection.prepareStatement("""
                        SELECT transaction_id FROM transactions
                        JOIN items ON items.item_id = transactions.item_id
                        JOIN item_types ON item_types.base_type_id = items.type_id
                        WHERE user_id = ? AND item_types.base_type_id = ?
                        """);
                statement.setLong(1, user.getIdLong());
                statement.setInt(2, PREMIUM_BASE_TYPE_ID);
                var result = statement.executeQuery();

                if (result.next()) {
                    statement = connection.prepareStatement("DELETE FROM transactions WHERE transaction_id = ?");
                    statement.setInt(1, result.getInt("transaction_id"));
                    statement.execute();
                }

                statement = connection.prepareStatement("INSERT INTO transactions (\"user_id\", \"item_id\", \"expires_at\") VALUES(?, ?, -1)");
                statement.setLong(1, user.getIdLong());
                statement.setInt(2, itemId);

                statement.execute();

                return Optional.ofNullable(guild.getRoleById(item.roleId));
            }

            // check if user already has item of this type
            var statement = connection.prepareStatement("""
                    SELECT transaction_id, expires_at FROM transactions
                    JOIN items ON items.item_id = transactions.item_id
                    JOIN item_types ON item_types.base_type_id = items.type_id
                    WHERE user_id = ? AND item_types.base_type_id = ?
                    """);
            statement.setLong(1, user.getIdLong());
            statement.setInt(2, item.typeId);
            var result = statement.executeQuery();
            // if so just increase the duration of the first item
            if (result.next()) {
                statement = connection.prepareStatement("UPDATE transactions SET expires_at = ? WHERE transaction_id = ?");
                statement.setLong(1, result.getLong("expires_at") + item.duration);
                statement.setLong(2, result.getInt("transaction_id"));
                statement.execute();
                return Optional.empty();
            }

            statement = connection.prepareStatement("INSERT INTO transactions (\"user_id\", \"item_id\", \"expires_at\", \"is_play_activity\") VALUES(?, ?, ?, ?)");
            statement.setLong(1, user.getIdLong());
            statement.setInt(2, itemId);
            statement.setLong(3, System.currentTimeMillis() + item.duration);
            statement.setBoolean(4, isPlayActivity);

            statement.execute();

            if (item.roleId > 0) {
                return Optional.ofNullable(guild.getRoleById(item.roleId));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteTransaction(UserSnowflake user, int transactionId) {
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

    public List<Transaction> getExpiringTransactions() {
        try (Connection connection = dataSource.getConnection()) {
            var transactions = new ArrayList<Transaction>();

            var statement = connection.prepareStatement("""
                    SELECT *
                    FROM transactions
                    JOIN items ON transactions.item_id = items.item_id
                    JOIN item_types ON item_types.base_type_id = items.type_id
                    WHERE expires_at < ? AND NOT transactions.item_id = ?
                    """
            );
            statement.setLong(1, System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24));
            statement.setInt(2, PREMIUM_UNLIMITED_ITEM_ID);
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

    private List<Role> getAllItemRoles() {
        try (Connection connection = dataSource.getConnection()) {
            var result = connection.prepareStatement("SELECT role_id FROM item_types").executeQuery();
            var roles = new ArrayList<Role>();
            while (result.next()) {
                roles.add(guild.getRoleById(result.getLong(1)));
            }
            return roles;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateItemRoles(Member member) {
        var transactions = getTransactions(member);

        member.getRoles().stream().filter(getAllItemRoles()::contains).forEach(role -> {
            if (!transactions.stream().map(Transaction::roleId).toList().contains(role.getIdLong())) {
                guild.removeRoleFromMember(member, role).queue();
            }
        });

        transactions.forEach(transaction -> {
            if (transaction.roleId > 0) {
                guild.addRoleToMember(member, guild.getRoleById(transaction.roleId)).queue();
            }
        });
    }

    public Transaction getTransactionById(long transactionId) {
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.prepareStatement("""
                    SELECT *
                    FROM transactions
                    JOIN items ON transactions.item_id = items.item_id
                    JOIN item_types ON item_types.base_type_id = items.type_id
                    WHERE transaction_id = ?
                    """
            );
            statement.setLong(1, transactionId);
            var result = statement.executeQuery();
            result.next();
            return new Transaction(
                    result.getInt("transaction_id"),
                    result.getLong("user_id"),
                    result.getInt("item_id"),
                    result.getInt("type_id"),
                    result.getString("name"),
                    result.getLong("duration"),
                    result.getLong("expires_at"),
                    result.getLong("role_id"),
                    result.getBoolean("is_play_activity")
            );
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
