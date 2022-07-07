package de.kaktushose.levelbot.booster.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface NitroBoosterRepository extends CrudRepository<NitroBooster, Long> {

    @Query(value = "SELECT * FROM nitro_boosters where active = true", nativeQuery = true)
    List<NitroBooster> getActiveNitroBoosters();

}
