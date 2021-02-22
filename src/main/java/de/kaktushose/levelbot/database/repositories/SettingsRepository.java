package de.kaktushose.levelbot.database.repositories;

import de.kaktushose.levelbot.database.model.Config;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface SettingsRepository extends Repository<Config, Long> {

    @Query(value = "SELECT * FROM settings WHERE guild_id = :guildId", nativeQuery = true)
    Config getGuildSettings(@Param("guildId") long guildId);

}
