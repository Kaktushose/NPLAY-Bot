package de.kaktushose.levelbot.events.data.contest;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ContestRepository extends CrudRepository<ContestEntry, Long> {

    @Query(value = "SELECT * FROM contest_entries order by count desc", nativeQuery = true)
    List<ContestEntry> getContestResult();

    boolean existsByUserId(long userId);

}
