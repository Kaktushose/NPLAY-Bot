package de.kaktushose.levelbot.database.repositories;

import de.kaktushose.levelbot.database.model.BotUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends CrudRepository<BotUser, Long> {

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

}
