package de.kaktushose.levelbot.database.repositories;

import de.kaktushose.levelbot.database.model.CollectEvent;
import org.springframework.data.repository.CrudRepository;

public interface CollectEventRepository extends CrudRepository<CollectEvent, Integer> {
}
