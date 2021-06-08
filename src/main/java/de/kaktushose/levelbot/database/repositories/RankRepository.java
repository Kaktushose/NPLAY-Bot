package de.kaktushose.levelbot.database.repositories;

import de.kaktushose.levelbot.database.model.Rank;
import org.springframework.data.repository.CrudRepository;

public interface RankRepository extends CrudRepository<Rank, Integer> {
}
