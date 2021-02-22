package de.kaktushose.levelbot.database.repositories;

import de.kaktushose.levelbot.database.model.MutedChannel;
import org.springframework.data.repository.CrudRepository;

public interface MutedChannelRepository extends CrudRepository<MutedChannel, Long> {
}
