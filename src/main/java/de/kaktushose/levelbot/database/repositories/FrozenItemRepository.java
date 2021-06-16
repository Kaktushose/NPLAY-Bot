package de.kaktushose.levelbot.database.repositories;

import de.kaktushose.levelbot.database.model.FrozenItem;
import org.springframework.data.repository.CrudRepository;

public interface FrozenItemRepository extends CrudRepository<FrozenItem, Long> {
}
