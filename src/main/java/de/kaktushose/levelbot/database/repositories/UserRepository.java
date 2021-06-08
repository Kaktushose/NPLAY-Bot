package de.kaktushose.levelbot.database.repositories;

import de.kaktushose.levelbot.database.model.BotUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<BotUser, Long> {

    List<BotUser> findByPermissionLevel(int permissionLevel);

    @Query(value = "SELECT * FROM users where daily = true", nativeQuery = true)
    List<BotUser> getAllWithDaily();

    @Query(value = "SELECT * FROM users order by xp desc", nativeQuery = true)
    List<BotUser> getXpLeaderboard();

    @Query(value = "SELECT * FROM users order by coins desc", nativeQuery = true)
    List<BotUser> getCoinsLeaderboard();

    @Query(value = "SELECT * FROM users order by diamonds desc", nativeQuery = true)
    List<BotUser> getDiamondsLeaderboard();

}
