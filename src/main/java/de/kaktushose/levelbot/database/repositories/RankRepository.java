package de.kaktushose.levelbot.database.repositories;

import de.kaktushose.levelbot.database.model.Rank;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface RankRepository extends CrudRepository<Rank, Integer> {

    @Query(value = "SELECT * FROM ranks where bound < :xp order by bound desc limit 1", nativeQuery = true)
    Rank getRankByXp(@Param("xp") long xp);

}
