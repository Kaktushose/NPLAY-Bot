package de.kaktushose.levelbot.database.repositories;

import de.kaktushose.levelbot.database.model.GuildSettings;
import de.kaktushose.levelbot.database.model.Reward;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SettingsRepository extends CrudRepository<GuildSettings, Long> {

    @Query(value = "SELECT * FROM guild_settings WHERE guild_id = :guildId", nativeQuery = true)
    Optional<GuildSettings> getGuildSettings(@Param("guildId") long guildId);

    @Query(value = "SELECT * FROM ignored_channels", nativeQuery = true)
    List<Long> getIgnoredChannels();

}
