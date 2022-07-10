package de.kaktushose.levelbot.account.data;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface UserRepository extends CrudRepository<BotUser, Long> {

    @Query(value = "SELECT user_id FROM users", nativeQuery = true)
    List<Long> findAllIds();

    @Query(value = "SELECT user_id FROM users where permission_level <= 0", nativeQuery = true)
    List<Long> findMutedUsers();

    @Query(value = "SELECT user_id FROM users where permission_level >= :level", nativeQuery = true)
    List<Long> findByPermissionLevel(@Param("level") int permissionLevel);

    @Query(value = "SELECT * FROM users where daily = true", nativeQuery = true)
    List<BotUser> getAllWithDaily();

    @Query(value = "SELECT * FROM users order by xp desc", nativeQuery = true)
    List<BotUser> getXpLeaderboard();

    @Query(value = "SELECT * FROM users order by coins desc", nativeQuery = true)
    List<BotUser> getCoinsLeaderboard();

    @Query(value = "SELECT * FROM users order by diamonds desc", nativeQuery = true)
    List<BotUser> getDiamondsLeaderboard();

    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET event_points = 0", nativeQuery = true)
    void resetEventPoints();

    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET start_coins = coins, start_xp = xp, start_diamonds = diamonds", nativeQuery = true)
    void updateStatistics();

    @Query(value = "SELECT new java.lang.Boolean(permission_level < 1) FROM BotUser WHERE user_id = :userId")
    boolean isMuted(long userId);

    @Query(value = "SELECT new java.lang.Boolean(permission_level >= :permissionLevel) FROM BotUser WHERE user_id = :userId")
    boolean hasPermission(long userId, int permissionLevel);
}
