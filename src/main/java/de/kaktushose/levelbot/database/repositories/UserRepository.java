package de.kaktushose.levelbot.database.repositories;

import de.kaktushose.levelbot.database.model.BotUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends CrudRepository<BotUser, Long> {

    @Query(value = "SELECT * FROM users WHERE permission_level = :permissionLevel", nativeQuery = true)
    List<BotUser> findByPermissionLevel(@Param("permissionLevel") int permissionLevel);

}
