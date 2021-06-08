package de.kaktushose.levelbot.database.repositories;

import de.kaktushose.levelbot.database.model.NitroBooster;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NitroBoosterRepository extends CrudRepository<NitroBooster, Long> {

    @Query(value = "SELECT * FROM nitro_boosters where active = true", nativeQuery = true)
    List<NitroBooster> getActiveNitroBoosters();

}
