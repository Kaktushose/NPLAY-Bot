package de.kaktushose.levelbot.database.repositories;

import de.kaktushose.levelbot.database.model.GuildSettings;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public interface SettingsRepository extends CrudRepository<GuildSettings, Long> {

    @Query(value = "SELECT * FROM guild_settings WHERE guild_id = :guildId", nativeQuery = true)
    Optional<GuildSettings> getGuildSettings(@Param("guildId") long guildId);

    @Query(value = "SELECT * FROM ignored_channels", nativeQuery = true)
    List<Long> getIgnoredChannels();

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO ignored_channels VALUES (:channelId)", nativeQuery = true)
    void addIgnoredChannel(@Param("channelId") long channelId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM ignored_channels where channel_id = :channelId", nativeQuery = true)
    void removeIgnoredChannel(@Param("channelId") long channelId);

}
